package com.messmate.android.ui.screens.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.meal.MealToggleRequest
import com.messmate.android.data.meal.MealSelectionDashboardResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.messmate.android.service.FcmEventBus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class MealState {
    object Loading : MealState()
    data class Success(
        val dashboardData: MealSelectionDashboardResponse,
        val selectedDateStr: String,
        val lunchActive: Boolean,
        val dinnerActive: Boolean,
        val isSaving: Boolean = false,
        val saveSuccess: Boolean = false
    ) : MealState()
    data class Error(val message: String) : MealState()
}

class MealViewModel : ViewModel() {
    private val _state = MutableStateFlow<MealState>(MealState.Loading)
    val state: StateFlow<MealState> = _state.asStateFlow()

    private fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    init {
        loadDashboard()

        // Background refresh when FCM events arrive (Live update for user)
        viewModelScope.launch {
            FcmEventBus.events.collect { type ->
                if (type.equals("MEAL_UPDATE", ignoreCase = true) || 
                    type.equals("MEAL_VOTE", ignoreCase = true)) {
                    loadDashboard()
                }
            }
        }
    }

    fun loadDashboard() {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _state.value = MealState.Loading
            try {
                val data = ApiClient.apiService.getMealSelectionDashboard(messId)
                val today = getTodayDateString()
                val todaySelection = data.futureSelections.find { it.date == today }
                
                _state.value = MealState.Success(
                    dashboardData = data,
                    selectedDateStr = today,
                    lunchActive = todaySelection?.lunch ?: true,
                    dinnerActive = todaySelection?.dinner ?: true
                )
            } catch (e: Exception) {
                _state.value = MealState.Error("Failed to load meal dashboard: ${e.message}")
            }
        }
    }

    fun selectDate(dateStr: String) {
        val currentState = _state.value as? MealState.Success ?: return
        val data = currentState.dashboardData
        val selection = data.futureSelections.find { it.date == dateStr }
             ?: data.recentHistory.find { it.date == dateStr }
             
        _state.value = currentState.copy(
            selectedDateStr = dateStr,
            lunchActive = selection?.lunch ?: true,
            dinnerActive = selection?.dinner ?: true
        )
    }

    fun updateMeals(lunch: Boolean, dinner: Boolean) {
        val messId = MessRepository.currentMessId.value ?: return
        val currentState = _state.value as? MealState.Success ?: return

        viewModelScope.launch {
            _state.value = currentState.copy(isSaving = true)
            try {
                ApiClient.apiService.toggleMeal(
                    messId,
                    MealToggleRequest(date = currentState.selectedDateStr, lunch = lunch, dinner = dinner)
                )
                // Reload dashboard to get fresh server data
                val data = ApiClient.apiService.getMealSelectionDashboard(messId)

                _state.value = currentState.copy(
                    dashboardData = data,
                    lunchActive = lunch,
                    dinnerActive = dinner,
                    isSaving = false,
                    saveSuccess = true
                )

                // 🔑 Emit local live-update event so AdminViewModel refreshes
                // immediately on the same device — no FCM required
                FcmEventBus.emitEvent("MEAL_UPDATE")

                // Clear success banner after 2 seconds
                kotlinx.coroutines.delay(2000)
                if (_state.value is MealState.Success) {
                    _state.value = (_state.value as MealState.Success).copy(saveSuccess = false)
                }
            } catch (e: Exception) {
                _state.value = currentState.copy(isSaving = false)
                loadDashboard()
            }
        }
    }
}
