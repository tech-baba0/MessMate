package com.messmate.android.data.expense

data class ExpenseShare(
    val id: String?,
    val expenseId: String,
    val messId: String,
    val userId: String,
    val shareAmount: Double
)

data class ExpenseResponse(
    val id: String,
    val messId: String,
    val title: String,
    val description: String?,
    val category: String?,
    val mealScope: String?,
    val purchasedById: String?,
    val totalAmount: Double,
    val date: String,
    val status: String,
    val splitMethod: String?,
    val receiptUrl: String?
)
