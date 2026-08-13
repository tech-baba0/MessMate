package com.messmate.android.data.expense

data class ExpenseItem(
    val name: String,
    val amount: Double
)

data class CustomSplit(
    val memberId: String,
    val amount: Double? = null,
    val percentage: Double? = null
)

data class ExpenseRequest(
    val title: String,
    val description: String?,
    val date: String,
    val category: String? = null,
    val mealScope: String? = null,
    val paidBy: String? = null,
    val receiptUrl: String? = null,
    val totalAmount: Double,
    val splitMethod: String = "AUTO_MEAL",
    val items: List<ExpenseItem>? = null,
    val customSplits: List<CustomSplit>? = null
)
