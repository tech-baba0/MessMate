package com.messmate.android.data.expense

data class BalanceResponse(
    val userId: String,
    val name: String,
    val totalExpenseShare: Double,
    val totalPaidForBazar: Double,
    val paymentsMade: Double,
    val paymentsReceived: Double,
    val pendingPaymentsMade: Double,
    val netBalance: Double,
    val balanceMessage: String
)

data class SuggestedReimbursement(
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val amount: Double
)

data class GroupBalanceResponse(
    val userBalances: List<BalanceResponse>,
    val suggestedReimbursements: List<SuggestedReimbursement>
)
