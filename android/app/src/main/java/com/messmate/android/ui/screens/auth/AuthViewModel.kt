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
                uploadFcmToken()
                _authState.value = AuthState.Success(response.token)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Unknown error occurred"
                _authState.value = AuthState.Error("Google Login Failed: $errorMsg")
            }
        }
    }

    fun register(name: String, phone: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = SignupRequest(name, phone, email, password)
                val response = ApiClient.apiService.register(request)
                
                ApiClient.tokenManager.saveToken(response.token)
                uploadFcmToken()
                _authState.value = AuthState.Success(response.token)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Registration failed"
                _authState.value = AuthState.Error(errorMsg)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = LoginRequest(email, password)
                val response = ApiClient.apiService.login(request)
                
                ApiClient.tokenManager.saveToken(response.token)
                uploadFcmToken()
                _authState.value = AuthState.Success(response.token)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Login failed"
                _authState.value = AuthState.Error(errorMsg)
            }
        }
    }

    private fun uploadFcmToken() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (token != null) {
                        viewModelScope.launch {
                            try {
                                ApiClient.apiService.updateFcmToken(com.messmate.android.data.auth.FcmTokenRequest(token))
                            } catch (e: Exception) {
                                // Ignore failure, token will be re-sent later if needed or on next login
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Firebase might not be initialized properly
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
    
    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }
}
