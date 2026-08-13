package com.messmate.backend.service;

import com.messmate.backend.dto.request.MessCreateRequest;
import com.messmate.backend.entity.Mess;
import com.messmate.backend.entity.MessMember;
import com.messmate.backend.entity.Role;
import com.messmate.backend.repository.MessMemberRepository;
import com.messmate.backend.repository.MessRepository;
import com.messmate.backend.repository.UserRepository;
import com.messmate.backend.entity.User;
import com.messmate.backend.dto.response.MessMemberResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.messmate.backend.dto.response.MessMembershipResponse;

@Service
public class MessService {
    @Autowired
    private MessRepository messRepository;

    @Autowired
    private MessMemberRepository messMemberRepository;

    @Autowired
    private UserRepository userRepository;

    public Mess createMess(MessCreateRequest request, String userId) {
        String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Mess mess = Mess.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .accountingStartDate(request.getAccountingStartDate())
                .defaultLunchAvailability(request.getDefaultLunchAvailability())
                .defaultDinnerAvailability(request.getDefaultDinnerAvailability())
                .lunchVotingDeadline(request.getLunchVotingDeadline())
                .dinnerVotingDeadline(request.getDinnerVotingDeadline())
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
                .status("ACTIVE")
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

    public List<MessMembershipResponse> getUserMesses(String userId) {
        List<MessMember> memberships = messMemberRepository.findByUserId(userId);
        return memberships.stream().map(member -> {
            Mess mess = messRepository.findById(member.getMessId()).orElse(null);
            return MessMembershipResponse.builder()
                    .mess(mess)
                    .role(member.getRole())
                    .status(member.getStatus())
                    .joinDate(member.getJoinDate())
                    .build();
        }).filter(r -> r.getMess() != null).collect(Collectors.toList());
    }

    private void verifyAdmin(String messId, String adminId) {
        MessMember admin = messMemberRepository.findByMessIdAndUserId(messId, adminId)
                .orElseThrow(() -> new RuntimeException("Admin member not found in mess"));
        if (admin.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Not authorized: Must be an ADMIN of this mess");
        }
    }

    public MessMember changeMemberStatus(String messId, String targetUserId, String adminId, String status) {
        verifyAdmin(messId, adminId);
        MessMember member = messMemberRepository.findByMessIdAndUserId(messId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setStatus(status);
        return messMemberRepository.save(member);
    }

    public List<MessMemberResponse> getMessMembers(String messId) {
        List<MessMember> members = messMemberRepository.findByMessId(messId);
        return members.stream().map(member -> {
            User user = userRepository.findById(member.getUserId()).orElse(null);
            return MessMemberResponse.builder()
                    .id(member.getId())
                    .userId(member.getUserId())
                    .name(user != null ? user.getName() : "Unknown")
                    .email(user != null ? user.getEmail() : "Unknown")
                    .role(member.getRole())
                    .status(member.getStatus())
                    .joinDate(member.getJoinDate())
                    .build();
        }).collect(Collectors.toList());
    }

    public MessMember changeMemberRole(String messId, String targetUserId, String adminId, String roleStr) {
        verifyAdmin(messId, adminId);
        MessMember member = messMemberRepository.findByMessIdAndUserId(messId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        try {
            Role role = Role.valueOf(roleStr);
            member.setRole(role);
            return messMemberRepository.save(member);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role. Must be ROLE_USER or ROLE_ADMIN");
        }
    }
}
