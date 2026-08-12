package com.messmate.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.auth.LoginRequest
import com.messmate.android.data.auth.SignupRequest
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = com.messmate.android.data.auth.GoogleLoginRequest(idToken = idToken)
                val response = ApiClient.apiService.googleLogin(request)
                
                ApiClient.tokenManager.saveToken(response.token)
                _authState.value = AuthState.Success(response.token)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Unknown error occurred"
                _authState.value = AuthState.Error("Google Login Failed: $errorMsg")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
