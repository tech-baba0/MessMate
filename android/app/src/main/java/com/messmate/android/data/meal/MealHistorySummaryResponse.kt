package com.messmate.android.data.meal

data class MealHistorySummaryResponse(
    val meals: List<MealStatusResponse>,
    val totalLunch: Int,
    val totalDinner: Int,
    val totalMeals: Int
)
