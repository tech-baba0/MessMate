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

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = LoginRequest(email = email, password = password)
                val response = ApiClient.apiService.login(request)
                
                // Save the token globally
                ApiClient.tokenManager.saveToken(response.token)
                
                _authState.value = AuthState.Success(response.token)
            } catch (e: Exception) {
                // TODO: Better error parsing
                val errorMsg = e.localizedMessage ?: "Unknown error occurred"
                _authState.value = AuthState.Error("Login Failed: $errorMsg")
            }
        }
    }

    fun register(name: String, phone: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = SignupRequest(name = name, phone = phone, email = email, password = password)
                ApiClient.apiService.register(request)
                // Use token literal to signify success
                _authState.value = AuthState.Success("REGISTER_SUCCESS")
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Unknown error occurred"
                _authState.value = AuthState.Error("Registration Failed: $errorMsg")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
