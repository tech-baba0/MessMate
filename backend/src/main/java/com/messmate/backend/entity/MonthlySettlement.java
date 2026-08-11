package com.messmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "monthly_settlements")
@CompoundIndex(def = "{'messId': 1, 'monthYear': 1}", unique = true)
public class MonthlySettlement {
    @Id
    private String id;

    private String messId;
    private String monthYear; // e.g. "2026-08"

    private Double totalExpenses;
    private Double totalMeals;

    private String status; // GENERATED, CLOSED

    private LocalDate generatedAt;
    private LocalDate closedAt;

    private List<MemberSettlementSummary> memberSummaries;
}
