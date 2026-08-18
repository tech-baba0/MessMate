package com.messmate.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMealDashboardResponse {
    private LocalDate targetDate;
    private int totalActiveMembers;

    private int todayLunchYes;
    private int todayLunchNo;
    private int todayDinnerYes;
    private int todayDinnerNo;
    private int totalLunchMeals;
    private int totalDinnerMeals;
    private int totalMealUnits;

    private String lunchVotingStatus; // "OPEN" or "CLOSED"
    private String dinnerVotingStatus; // "OPEN" or "CLOSED"

    private java.util.List<MemberMealDetailResponse> memberDetails;
}
