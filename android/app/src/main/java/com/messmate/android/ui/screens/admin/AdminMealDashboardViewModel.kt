package com.messmate.android.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.meal.AdminMealDashboardResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import com.messmate.android.service.FcmEventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class AdminMealState {
    object Loading : AdminMealState()
    data class Success(val dashboard: AdminMealDashboardResponse) : AdminMealState()
    data class Error(val message: String) : AdminMealState()
}

class AdminMealDashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow<AdminMealState>(AdminMealState.Loading)
    val state: StateFlow<AdminMealState> = _state.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboard()
        
        // Background refresh when FCM events arrive (Live update)
        viewModelScope.launch {
            FcmEventBus.events.collect { type ->
                if (type.equals("MEAL_UPDATE", ignoreCase = true) || 
                    type.equals("MEAL_VOTE", ignoreCase = true)) {
                    loadDashboard(isSilent = true)
                }
            }
        }

        // Periodic polling: only poll live when viewing today's data (in IST)
        viewModelScope.launch {
            while (isActive) {
                delay(10000)
                val todayIndia = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"))
                if (_selectedDate.value == todayIndia) {
                    loadDashboard(isSilent = true)
                }
            }
        }
    }

    fun loadDashboard(isSilent: Boolean = false) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            if (!isSilent) {
                _isRefreshing.value = true
                if (_state.value !is AdminMealState.Success) {
                    _state.value = AdminMealState.Loading
                }
            }
            try {
                val dateString = _selectedDate.value.toString()
                val dashboard = ApiClient.apiService.getAdminMealDashboard(messId, dateString)
                _state.value = AdminMealState.Success(dashboard)
            } catch (e: Exception) {
                if (!isSilent) {
                    _state.value = AdminMealState.Error("Failed to fetch dashboard: ${e.message}")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun changeDate(offsetDays: Long) {
        _selectedDate.value = _selectedDate.value.plusDays(offsetDays)
        loadDashboard()
    }
}
