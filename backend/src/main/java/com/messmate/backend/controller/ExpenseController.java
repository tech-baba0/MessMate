package com.messmate.backend.controller;

import com.messmate.backend.dto.request.ExpenseRequest;
import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.security.services.UserDetailsImpl;
import com.messmate.backend.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messes/{messId}/expenses")
public class ExpenseController {
    
    @Autowired
    private ExpenseService expenseService;
    
    @PostMapping
    public ResponseEntity<?> addExpense(@PathVariable String messId, @Valid @RequestBody ExpenseRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();
            try {
                return ResponseEntity.ok(expenseService.createExpense(messId, userId, request));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }
}
