package com.messmate.backend.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MessSettingsUpdateRequest {
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format (HH:mm)")
    private String lunchVotingDeadline;

    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Invalid time format (HH:mm)")
    private String dinnerVotingDeadline;
}
