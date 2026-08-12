package com.messmate.android.data.meal

data class AdminMealDashboardResponse(
    val todayLunchYes: Int,
    val todayLunchNo: Int,
    val todayDinnerYes: Int,
    val todayDinnerNo: Int,
    val totalLunchMeals: Int,
    val totalDinnerMeals: Int,
    val totalMealUnits: Int,
    val lunchVotingStatus: String,
    val dinnerVotingStatus: String
)
