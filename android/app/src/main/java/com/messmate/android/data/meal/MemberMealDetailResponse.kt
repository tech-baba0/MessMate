package com.messmate.android.data.meal

data class MemberMealDetailResponse(
    val userName: String,
    val lunch: Boolean,
    val dinner: Boolean,
    val lunchUpdatedAt: String,
    val dinnerUpdatedAt: String
)
