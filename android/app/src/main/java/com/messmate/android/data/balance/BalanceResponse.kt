package com.messmate.android.data.balance

data class BalanceResponse(
    val userId: String,
    val name: String,
    val totalExpenseShare: Double?,
    val totalPaidForBazar: Double?,
    val paymentsMade: Double?,
    val paymentsReceived: Double?,
    val netBalance: Double?,
    val balanceMessage: String?
)
