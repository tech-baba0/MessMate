package com.messmate.android.data.auth

data class SignupRequest(
    val name: String,
    val phone: String,
    val email: String,
    val password: String
)
