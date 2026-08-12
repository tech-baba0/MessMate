package com.messmate.backend.controller;

import com.messmate.backend.dto.request.MessCreateRequest;
import com.messmate.backend.dto.request.MessJoinRequest;
import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.dto.response.MessMembershipResponse;
import com.messmate.backend.entity.Mess;
import com.messmate.backend.entity.MessMember;
import com.messmate.backend.security.services.UserDetailsImpl;
import com.messmate.backend.service.MessService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            List<MessMembershipResponse> memberships = messService.getUserMesses(userId);
            return ResponseEntity.ok(memberships);
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @PutMapping("/{messId}/members/{memberId}/approve")
    public ResponseEntity<?> approveMember(@PathVariable String messId, @PathVariable String memberId) {
        return updateMemberStatus(messId, memberId, "ACTIVE");
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @PutMapping("/{messId}/members/{memberId}/reject")
    public ResponseEntity<?> rejectMember(@PathVariable String messId, @PathVariable String memberId) {
        return updateMemberStatus(messId, memberId, "REJECTED");
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @PutMapping("/{messId}/members/{memberId}/remove")
    public ResponseEntity<?> removeMember(@PathVariable String messId, @PathVariable String memberId) {
        return updateMemberStatus(messId, memberId, "INACTIVE");
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @GetMapping("/{messId}/members")
    public ResponseEntity<?> getMessMembers(@PathVariable String messId) {
        // Implement get all members
        java.util.List<com.messmate.backend.dto.response.MessMemberResponse> members = messService
                .getMessMembers(messId);
        return ResponseEntity.ok(members);
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @PutMapping("/{messId}/members/{memberId}/role")
    public ResponseEntity<?> changeMemberRole(@PathVariable String messId, @PathVariable String memberId,
            @RequestParam String role) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String adminId = ((UserDetailsImpl) principal).getId();
            try {
                MessMember member = messService.changeMemberRole(messId, memberId, adminId, role);
                return ResponseEntity.ok(member);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }

    private ResponseEntity<?> updateMemberStatus(String messId, String memberId, String status) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String adminId = ((UserDetailsImpl) principal).getId();
            try {
                MessMember member = messService.changeMemberStatus(messId, memberId, adminId, status);
                return ResponseEntity.ok(member);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }
}
