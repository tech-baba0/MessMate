package com.messmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSettlementSummary {
    private String userId;
    private String name;

    private Double mealsTaken;
    private Double expenseShare;
    private Double paidAmount;

    private Double netBalance;
}
