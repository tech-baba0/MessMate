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
public class MealSelectionDashboardResponse {
    private Integer advanceBookingDays;
    private String lunchVotingDeadline;
    private String dinnerVotingDeadline;
    private String currentServerTime;

    // Month Summary
    private int currentMonthTotalMeals;
    private int currentMonthLunchCount;
    private int currentMonthDinnerCount;

    // History & Future Data
    private List<MealStatusResponse> recentHistory; // Last 5 days
    private List<MealStatusResponse> futureSelections; // Today to Today + limit
}
