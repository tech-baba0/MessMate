package com.messmate.backend.controller;

import com.messmate.backend.dto.request.PaymentRequest;
import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.entity.Payment;
import com.messmate.backend.repository.PaymentRepository;
import com.messmate.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

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
                    .status("PENDING")
                    .build();

            paymentRepository.save(payment);
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.badRequest().body(new MessageResponse(false, "User not authenticated"));
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @GetMapping
    public ResponseEntity<?> getPayments(@PathVariable String messId, @RequestParam(required = false) String status) {
        List<Payment> payments = paymentRepository.findByMessId(messId);
        if (status != null) {
            payments = payments.stream().filter(p -> status.equals(p.getStatus())).collect(Collectors.toList());
        }
        return ResponseEntity.ok(payments);
    }

    @PreAuthorize("@messSecurity.isAdmin(authentication, #messId)")
    @PutMapping("/{paymentId}/verify")
    public ResponseEntity<?> verifyPayment(
            @PathVariable String messId,
            @PathVariable String paymentId,
            @RequestParam String status) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getMessId().equals(messId)) {
            return ResponseEntity.badRequest().body(new MessageResponse(false, "Payment does not belong to this mess"));
        }

        if (!"COMPLETED".equals(status) && !"REJECTED".equals(status)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Invalid status. Must be COMPLETED or REJECTED."));
        }

        payment.setStatus(status);
        paymentRepository.save(payment);
        return ResponseEntity.ok(payment);
    }
}
