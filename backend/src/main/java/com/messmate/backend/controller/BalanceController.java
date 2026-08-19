package com.messmate.backend.controller;

import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.security.services.UserDetailsImpl;
import com.messmate.backend.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messes/{messId}/balance")
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyBalance(@PathVariable String messId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            try {
                return ResponseEntity.ok(balanceService.getBalanceForUser(messId, userId));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }

    @GetMapping("/group")
    public ResponseEntity<?> getGroupBalances(@PathVariable String messId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            try {
                return ResponseEntity.ok(balanceService.getGroupBalances(messId));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }
}
