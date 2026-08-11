package com.messmate.backend.controller;

import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messes/{messId}/settlements")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateSettlement(@PathVariable String messId, @RequestParam String monthYear) {
        try {
            return ResponseEntity.ok(settlementService.generateSettlement(messId, monthYear));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> closeSettlement(@PathVariable String messId, @PathVariable String id,
            @RequestParam String monthYear) {
        try {
            return ResponseEntity.ok(settlementService.closeSettlement(messId, monthYear));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reopenSettlement(@PathVariable String messId, @PathVariable String id,
            @RequestParam String monthYear) {
        try {
            return ResponseEntity.ok(settlementService.reopenSettlement(messId, monthYear));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
        }
    }
}
