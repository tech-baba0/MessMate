package com.messmate.android.data.auth

data class AuthResponse(
    val token: String,
    val id: String,
    val email: String,
    val roles: List<String>
)
