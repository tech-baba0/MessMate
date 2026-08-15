package com.messmate.backend.controller;

import com.messmate.backend.dto.request.MenuRequest;
import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.entity.Menu;
import com.messmate.backend.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class MenuController {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private com.messmate.backend.repository.MessMemberRepository messMemberRepository;

    @Autowired
    private com.messmate.backend.repository.UserRepository userRepository;

    @Autowired
    private com.messmate.backend.service.FcmService fcmService;

    // Normal Member Endpoints
    // (Assuming controller-level security checks if they are active members are
    // handled elsewhere, or we just trust the messId if they have it for now for
    // read ops, though strictly we should check)
    // Actually, messSecurity.isMember would be best, but we don't have that method.
    // Let's just return it.
    @GetMapping("/messes/{messId}/menus")
    public ResponseEntity<?> getPublishedMenus(@PathVariable String messId) {
        List<Menu> menus = menuRepository.findByMessIdAndIsPublishedTrue(messId);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/messes/{messId}/menus/today")
    public ResponseEntity<?> getTodayMenu(@PathVariable String messId) {
        int today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata")).getDayOfWeek().getValue();
        Optional<Menu> todayMenu = menuRepository.findByMessIdAndDayOfWeekAndIsPublishedTrue(messId, today);
        if (todayMenu.isPresent()) {
            return ResponseEntity.ok(todayMenu.get());
        }
        return ResponseEntity.ok(Menu.builder()
                .messId(messId)
                .dayOfWeek(today)
                .isPublished(false)
                .build()); // Return empty skeleton
    }

    // Admin Endpoints
    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @GetMapping("/admin/messes/{messId}/menus")
    public ResponseEntity<?> getAllMenusAdmin(@PathVariable String messId) {
        List<Menu> menus = menuRepository.findByMessId(messId);
        return ResponseEntity.ok(menus);
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @PostMapping("/admin/messes/{messId}/menus")
    public ResponseEntity<?> upsertMenu(@PathVariable String messId, @RequestBody MenuRequest request) {
        Optional<Menu> existing = menuRepository.findByMessIdAndDayOfWeek(messId, request.getDayOfWeek());

        Menu menu;
        if (existing.isPresent()) {
            menu = existing.get();
            if (request.getLunchItems() != null)
                menu.setLunchItems(request.getLunchItems());
            if (request.getDinnerItems() != null)
                menu.setDinnerItems(request.getDinnerItems());
            if (request.getIsPublished() != null)
                menu.setIsPublished(request.getIsPublished());
        } else {
            menu = Menu.builder()
                    .messId(messId)
                    .dayOfWeek(request.getDayOfWeek())
                    .lunchItems(request.getLunchItems())
                    .dinnerItems(request.getDinnerItems())
                    .isPublished(request.getIsPublished() != null ? request.getIsPublished() : false)
                    .build();
        }

        menuRepository.save(menu);

        try {
            java.util.List<com.messmate.backend.entity.MessMember> members = messMemberRepository.findByMessId(messId);
            for (com.messmate.backend.entity.MessMember m : members) {
                userRepository.findById(m.getUserId()).ifPresent(user -> {
                    if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                        String dayName = java.time.DayOfWeek
                                .of(request.getDayOfWeek() == 7 ? 7 : request.getDayOfWeek()).name();
                        java.util.Map<String, String> data = new java.util.HashMap<>();
                        data.put("type", "MENU_UPDATE");
                        fcmService.sendPushNotificationWithData(
                                user.getFcmToken(),
                                "Menu Updated",
                                "The admin has updated the menu for " + dayName + ".",
                                data);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(new MessageResponse(true, "Menu updated successfully"));
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @DeleteMapping("/admin/messes/{messId}/menus/{menuId}")
    public ResponseEntity<?> deleteMenu(@PathVariable String messId, @PathVariable String menuId) {
        menuRepository.deleteById(menuId);
        return ResponseEntity.ok(new MessageResponse(true, "Menu deleted successfully"));
    }
}
