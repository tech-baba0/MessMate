package com.messmate.backend.controller;

import com.messmate.backend.dto.request.PaymentRequest;
import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.entity.Payment;
import com.messmate.backend.repository.PaymentRepository;
import com.messmate.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messes/{messId}/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @PostMapping
    public ResponseEntity<?> addPayment(@PathVariable String messId, @Valid @RequestBody PaymentRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            String userId = ((UserDetailsImpl) principal).getId();

            Payment payment = Payment.builder()
                    .messId(messId)
                    .paidById(userId)
                    .paidToId(request.getPaidToId() != null ? request.getPaidToId() : "MESS")
                    .amount(request.getAmount())
                    .date(request.getDate())
                    .method(request.getMethod())
                    .note(request.getNote())
                    .status("COMPLETED")
                    .build();

            paymentRepository.save(payment);
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }
}
