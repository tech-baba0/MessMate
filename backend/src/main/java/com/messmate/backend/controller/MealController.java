package com.messmate.backend.controller;

import com.messmate.backend.dto.request.MealToggleRequest;
import com.messmate.backend.dto.response.AdminMealDashboardResponse;
import com.messmate.backend.dto.response.MealHistorySummaryResponse;
import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.security.services.UserDetailsImpl;
import com.messmate.backend.service.MealService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messes/{messId}/meals")
public class MealController {

    @Autowired
    private MealService mealService;

    @PostMapping
    public ResponseEntity<?> toggleMeal(@PathVariable String messId, @Valid @RequestBody MealToggleRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            try {
                return ResponseEntity.ok(mealService.toggleMeal(messId, userId, request));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getMealSelectionDashboard(@PathVariable String messId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            return ResponseEntity.ok(mealService.getMealSelectionDashboard(messId, userId));
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayMealStatus(@PathVariable String messId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            return ResponseEntity.ok(mealService.getUserMealStatus(messId, userId, LocalDate.now()));
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getMealStatusForDate(
            @PathVariable String messId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            return ResponseEntity.ok(mealService.getUserMealStatus(messId, userId, date));
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getMealHistory(
            @PathVariable String messId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            return ResponseEntity.ok(mealService.getMealHistory(messId, userId, startDate, endDate));
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getAdminDashboard(
            @PathVariable String messId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        return ResponseEntity.ok(mealService.getAdminMealDashboard(messId, targetDate));
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @GetMapping("/admin/report")
    public ResponseEntity<?> getMealReport(
            @PathVariable String messId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            return ResponseEntity.ok(mealService.getMealReport(messId, startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
        }
    }
}
