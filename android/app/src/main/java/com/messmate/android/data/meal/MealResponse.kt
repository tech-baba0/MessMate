package com.messmate.android.data.meal

data class MealResponse(
    val id: String?,
    val date: String,
    val lunch: Boolean,
    val dinner: Boolean,
    val mealUnits: Double?
)
