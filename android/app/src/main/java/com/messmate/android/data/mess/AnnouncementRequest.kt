package com.messmate.android.data.mess

data class AnnouncementRequest(
    val title: String,
    val message: String,
    val targetUserId: String? = null
)
