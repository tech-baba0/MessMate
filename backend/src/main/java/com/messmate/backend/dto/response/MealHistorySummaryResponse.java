package com.messmate.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealHistorySummaryResponse {
    private List<MealStatusResponse> meals;
    private int totalLunch;
    private int totalDinner;
    private int totalMeals;
}
