package com.messmate.backend.controller;

import com.messmate.backend.dto.request.MessSettingsUpdateRequest;
import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.entity.Mess;
import com.messmate.backend.repository.MessRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/messes/{messId}/settings")
public class MessSettingsController {

    @Autowired
    private MessRepository messRepository;

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @PutMapping
    public ResponseEntity<?> updateSettings(@PathVariable String messId,
            @Valid @RequestBody MessSettingsUpdateRequest request) {
        Mess mess = messRepository.findById(messId)
                .orElseThrow(() -> new RuntimeException("Mess not found"));

        if (request.getLunchVotingDeadline() != null) {
            mess.setLunchVotingDeadline(request.getLunchVotingDeadline());
        }
        if (request.getDinnerVotingDeadline() != null) {
            mess.setDinnerVotingDeadline(request.getDinnerVotingDeadline());
        }

        messRepository.save(mess);
        return ResponseEntity.ok(new MessageResponse(true, "Settings updated successfully"));
    }
}
