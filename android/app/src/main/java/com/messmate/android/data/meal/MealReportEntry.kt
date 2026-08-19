package com.messmate.android.data.meal

data class MealReportEntry(
    val userId: String,
    val userName: String,
    val date: String,
    val lunch: Boolean?,
    val dinner: Boolean?,
    val mealUnits: Double?,
    val updatedAt: String?
)
