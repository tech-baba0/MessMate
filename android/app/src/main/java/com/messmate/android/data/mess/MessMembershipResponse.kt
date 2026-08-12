package com.messmate.android.data.mess

data class MessMembershipResponse(
    val mess: MessResponse,
    val role: String,
    val status: String,
    val joinDate: String?
)
