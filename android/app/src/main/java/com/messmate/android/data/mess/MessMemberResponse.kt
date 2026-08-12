package com.messmate.android.data.mess

data class MessMemberResponse(
    val id: String,
    val userId: String,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val joinDate: String?
)
