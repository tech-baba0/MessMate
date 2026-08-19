package com.messmate.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MealReportEntry {
    private String userId;
    private String userName;
    private String date; // ISO: 2025-08-20
    private Boolean lunch;
    private Boolean dinner;
    private Double mealUnits;
    private String updatedAt; // last updated timestamp
}
