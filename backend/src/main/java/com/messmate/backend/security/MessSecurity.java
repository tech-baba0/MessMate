package com.messmate.backend.security;

import com.messmate.backend.entity.MessMember;
import com.messmate.backend.entity.Role;
import com.messmate.backend.repository.MessMemberRepository;
import com.messmate.backend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("messSecurity")
public class MessSecurity {

    @Autowired
    private MessMemberRepository messMemberRepository;

    public boolean isAdmin(Authentication authentication, String messId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return false;
        }

        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        Optional<MessMember> memberOpt = messMemberRepository.findByMessIdAndUserId(messId, userId);

        return memberOpt.isPresent() &&
                memberOpt.get().getRole() == Role.ROLE_ADMIN &&
                ("ACTIVE".equals(memberOpt.get().getStatus()) || "APPROVED".equals(memberOpt.get().getStatus()));
    }

    public boolean isActiveMember(Authentication authentication, String messId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return false;
        }

        String userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
        Optional<MessMember> memberOpt = messMemberRepository.findByMessIdAndUserId(messId, userId);

        return memberOpt.isPresent() &&
                ("ACTIVE".equals(memberOpt.get().getStatus()) || "APPROVED".equals(memberOpt.get().getStatus()));
    }
}
