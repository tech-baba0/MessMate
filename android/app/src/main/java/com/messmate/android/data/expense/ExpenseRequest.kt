package com.messmate.android.data.expense

data class ExpenseItem(
    val name: String,
    val amount: Double
)

data class ExpenseRequest(
    val title: String,
    val description: String?,
    val date: String,
    val totalAmount: Double,
    val splitMethod: String = "MEAL_BASED",
    val items: List<ExpenseItem>? = null
)
