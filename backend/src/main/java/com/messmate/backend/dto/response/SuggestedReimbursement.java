package com.messmate.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuggestedReimbursement {
    private String fromUserId;
    private String fromUserName;
    private String toUserId;
    private String toUserName;
    private Double amount;
}
