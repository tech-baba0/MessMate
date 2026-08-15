package com.messmate.android.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.meal.AdminMealDashboardResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminMealState {
    object Loading : AdminMealState()
    data class Success(val dashboard: AdminMealDashboardResponse) : AdminMealState()
    data class Error(val message: String) : AdminMealState()
}

class AdminMealDashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow<AdminMealState>(AdminMealState.Loading)
    val state: StateFlow<AdminMealState> = _state.asStateFlow()

    init {
        loadDashboard()
        viewModelScope.launch {
            com.messmate.android.service.FcmEventBus.events.collect { type ->
                if (type == "MEAL_UPDATE") {
                    loadDashboard()
                }
            }
        }
    }

    fun loadDashboard() {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _state.value = AdminMealState.Loading
            try {
                val dashboard = ApiClient.apiService.getAdminMealDashboard(messId)
                _state.value = AdminMealState.Success(dashboard)
            } catch (e: Exception) {
                _state.value = AdminMealState.Error("Failed to fetch admin dashboard: ${e.message}")
            }
        }
    }
}
