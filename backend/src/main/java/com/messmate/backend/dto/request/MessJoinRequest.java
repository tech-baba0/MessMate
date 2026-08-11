package com.messmate.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessJoinRequest {
    @NotBlank
    private String inviteCode;
}
