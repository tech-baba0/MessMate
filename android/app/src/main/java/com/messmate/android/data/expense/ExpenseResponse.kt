package com.messmate.android.data.expense

data class ExpenseResponse(
    val id: String,
    val title: String,
    val description: String?,
    val totalAmount: Double,
    val date: String,
    val status: String
)
