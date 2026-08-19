package com.messmate.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GroupBalanceResponse {
    private List<BalanceResponse> userBalances;
    private List<SuggestedReimbursement> suggestedReimbursements;
}
