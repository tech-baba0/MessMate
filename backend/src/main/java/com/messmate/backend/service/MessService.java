package com.messmate.backend.service;

import com.messmate.backend.dto.request.MessCreateRequest;
import com.messmate.backend.entity.Mess;
import com.messmate.backend.entity.MessMember;
import com.messmate.backend.entity.Role;
import com.messmate.backend.repository.MessMemberRepository;
import com.messmate.backend.repository.MessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessService {
    @Autowired
    private MessRepository messRepository;

    @Autowired
    private MessMemberRepository messMemberRepository;

    public Mess createMess(MessCreateRequest request, String userId) {
        String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Mess mess = Mess.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .accountingStartDate(request.getAccountingStartDate())
                .defaultLunchAvailability(request.getDefaultLunchAvailability())
                .defaultDinnerAvailability(request.getDefaultDinnerAvailability())
                .mealSelectionCutoffTime(request.getMealSelectionCutoffTime())
                .currency(request.getCurrency())
                .expenseSplitMethod(request.getExpenseSplitMethod())
                .inviteCode(inviteCode)
                .build();

        Mess savedMess = messRepository.save(mess);

        // Add creator as ADMIN member implicitly
        MessMember adminMember = MessMember.builder()
                .messId(savedMess.getId())
                .userId(userId)
                .role(Role.ROLE_ADMIN)
                .status("APPROVED")
                .joinDate(LocalDateTime.now())
                .build();

        messMemberRepository.save(adminMember);

        return savedMess;
    }

    public MessMember joinMess(String inviteCode, String userId) {
        Mess mess = messRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Mess not found with invite code: " + inviteCode));

        Optional<MessMember> existingMember = messMemberRepository.findByMessIdAndUserId(mess.getId(), userId);
        if (existingMember.isPresent()) {
            throw new RuntimeException("User is already a member of this mess.");
        }

        MessMember member = MessMember.builder()
                .messId(mess.getId())
                .userId(userId)
                .role(Role.ROLE_USER)
                .status("PENDING") // Requires admin approval
                .joinDate(LocalDateTime.now())
                .build();

        return messMemberRepository.save(member);
    }

    public java.util.List<Mess> getUserMesses(String userId) {
        java.util.List<MessMember> memberships = messMemberRepository.findByUserId(userId);
        java.util.List<String> messIds = memberships.stream()
                .map(MessMember::getMessId)
                .collect(java.util.stream.Collectors.toList());
        return (java.util.List<Mess>) messRepository.findAllById(messIds);
    }
}
