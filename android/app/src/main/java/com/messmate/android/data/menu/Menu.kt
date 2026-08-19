package com.messmate.android.data.menu

import com.google.gson.annotations.SerializedName

data class Menu(
    val id: String?,
    val messId: String,
    val dayOfWeek: Int,
    val lunchItems: List<String>?,
    val dinnerItems: List<String>?,
    // Lombok @Data on Boolean isPublished generates getIsPublished() → Jackson serialises as "published"
    @SerializedName(value = "published", alternate = ["isPublished"])
    val isPublished: Boolean
)

data class MenuRequest(
    val dayOfWeek: Int,
    val lunchItems: List<String>?,
    val dinnerItems: List<String>?,
    // Ensure we send "isPublished" so Spring's setter setIsPublished() picks it up
    @SerializedName("isPublished")
    val isPublished: Boolean
)
