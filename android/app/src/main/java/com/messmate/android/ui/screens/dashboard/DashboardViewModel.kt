package com.messmate.android.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.balance.BalanceResponse
import com.messmate.android.network.ApiClient
import com.messmate.android.data.mess.MessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val balance: BalanceResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _state.value = DashboardState.Loading
            try {
                val messes = ApiClient.apiService.getMyMesses()
                if (messes.isEmpty()) {
                    _state.value = DashboardState.Error("You are not part of any mess yet. Please ask your admin for an invite code.")
                    return@launch
                }
                
                val firstMessId = messes.first().id
                MessRepository.setCurrentMessId(firstMessId)
                
                val balance = ApiClient.apiService.getMyBalance(firstMessId)
                _state.value = DashboardState.Success(balance)
            } catch (e: Exception) {
                _state.value = DashboardState.Error("Failed to load dashboard: ${e.localizedMessage}")
            }
        }
    }
}
