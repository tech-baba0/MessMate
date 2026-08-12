package com.messmate.backend.dto.response;

import com.messmate.backend.entity.Mess;
import com.messmate.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessMembershipResponse {
    private Mess mess;
    private Role role;
    private String status;
    private LocalDateTime joinDate;
}
