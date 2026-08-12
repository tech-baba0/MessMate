package com.messmate.android.data.menu

data class Menu(
    val id: String?,
    val messId: String,
    val dayOfWeek: Int,
    val lunchItems: List<String>?,
    val dinnerItems: List<String>?,
    val isPublished: Boolean
)

data class MenuRequest(
    val dayOfWeek: Int,
    val lunchItems: List<String>?,
    val dinnerItems: List<String>?,
    val isPublished: Boolean
)
