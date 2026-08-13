package com.messmate.android.data.meal

data class MealSelectionDashboardResponse(
    val advanceBookingDays: Int,
    val lunchVotingDeadline: String,
    val dinnerVotingDeadline: String,
    val currentServerTime: String,
    val currentMonthTotalMeals: Int,
    val currentMonthLunchCount: Int,
    val currentMonthDinnerCount: Int,
    val recentHistory: List<MealStatusResponse>,
    val futureSelections: List<MealStatusResponse>
)
