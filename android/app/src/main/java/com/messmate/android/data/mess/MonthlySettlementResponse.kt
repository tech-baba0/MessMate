package com.messmate.android.data.mess

data class MonthlySettlementResponse(
    val id: String,
    val messId: String,
    val monthYear: String,
    val totalExpenses: Double,
    val totalMeals: Double,
    val mealRate: Double,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
