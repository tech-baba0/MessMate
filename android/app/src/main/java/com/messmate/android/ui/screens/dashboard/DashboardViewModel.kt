package com.messmate.android.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.balance.BalanceResponse
import com.messmate.android.network.ApiClient
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.data.menu.Menu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val balance: BalanceResponse, val role: String) : DashboardState()
    data class PendingApproval(val messName: String) : DashboardState()
    data class Rejected(val messName: String) : DashboardState()
    data class Inactive(val messName: String) : DashboardState()
    object NoMess : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _todayMenu = MutableStateFlow<Menu?>(null)
    val todayMenu: StateFlow<Menu?> = _todayMenu.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            try {
                val memberships = ApiClient.apiService.getMyMesses()
                if (memberships.isEmpty()) {
                    _state.value = DashboardState.NoMess
                    return@launch
                }
                
                val firstMember = memberships.first()
                val mess = firstMember.mess
                val status = firstMember.status
                
                when (status) {
                    "PENDING" -> {
                        _state.value = DashboardState.PendingApproval(mess.name)
                        return@launch
                    }
                    "REJECTED" -> {
                        _state.value = DashboardState.Rejected(mess.name)
                        return@launch
                    }
                    "INACTIVE" -> {
                        _state.value = DashboardState.Inactive(mess.name)
                        return@launch
                    }
                }
                
                MessRepository.setCurrentMessId(mess.id)
                
                val balance = ApiClient.apiService.getMyBalance(mess.id)
                _state.value = DashboardState.Success(balance, firstMember.role)
                fetchTodayMenu(mess.id)
                
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        viewModelScope.launch {
                            try {
                                ApiClient.apiService.updateFcmToken(com.messmate.android.data.auth.FcmTokenRequest(token))
                            } catch (e: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = DashboardState.Error("Failed to load dashboard: ${e.localizedMessage}")
            }
        }
    }

    fun joinMess(inviteCode: String) {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            try {
                val request = com.messmate.android.data.mess.JoinMessRequest(inviteCode)
                ApiClient.apiService.joinMess(request)
                fetchDashboardData() // Refresh status
            } catch (e: Exception) {
                _state.value = DashboardState.Error("Failed to join mess: ${e.localizedMessage}")
            }
        }
    }

    private fun fetchTodayMenu(messId: String) {
        viewModelScope.launch {
            try {
                val menu = ApiClient.apiService.getTodayMenu(messId)
                if (menu.isPublished) {
                    _todayMenu.value = menu
                } else {
                    _todayMenu.value = null
                }
            } catch (e: Exception) {
                _todayMenu.value = null
            }
        }
    }
}
