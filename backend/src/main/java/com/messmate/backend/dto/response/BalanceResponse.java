package com.messmate.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BalanceResponse {
    private String userId;
    private String name;

    private Double totalExpenseShare;
    private Double totalPaidForBazar;

    private Double paymentsMade;
    private Double paymentsReceived;

    private Double netBalance; // > 0 means receive, < 0 means owe

    private Double pendingPaymentsMade;

    private String balanceMessage;
}
