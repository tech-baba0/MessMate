package com.messmate.android.data.meal

data class MealToggleRequest(
    val date: String,
    val lunch: Boolean,
    val dinner: Boolean
)
