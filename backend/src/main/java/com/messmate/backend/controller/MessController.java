package com.messmate.backend.controller;

import com.messmate.backend.dto.request.MessCreateRequest;
import com.messmate.backend.dto.request.MessJoinRequest;
import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.entity.Mess;
import com.messmate.backend.entity.MessMember;
import com.messmate.backend.security.services.UserDetailsImpl;
import com.messmate.backend.service.MessService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messes")
public class MessController {

    @Autowired
    private MessService messService;

    @PostMapping
    public ResponseEntity<?> createMess(@Valid @RequestBody MessCreateRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            Mess mess = messService.createMess(request, userId);
            return ResponseEntity.ok(mess);
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated properly"));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinMess(@Valid @RequestBody MessJoinRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            try {
                MessMember member = messService.joinMess(request.getInviteCode(), userId);
                return ResponseEntity.ok(member);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated properly"));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyMesses() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            java.util.List<Mess> messes = messService.getUserMesses(userId);
            return ResponseEntity.ok(messes);
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }
}
