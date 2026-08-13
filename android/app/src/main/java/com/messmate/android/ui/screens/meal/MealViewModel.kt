package com.messmate.android.ui.screens.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.meal.MealToggleRequest
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class MealState {
    object Loading : MealState()
    data class Success(
        val dateStr: String,
        val lunchActive: Boolean,
        val dinnerActive: Boolean,
        val isSaving: Boolean = false
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
        loadTodayMeal()
    }

    fun loadTodayMeal() {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _state.value = MealState.Loading
            try {
                val status = ApiClient.apiService.getTodayMealStatus(messId)
                _state.value = MealState.Success(
                    dateStr = status.date,
                    lunchActive = status.lunch,
                    dinnerActive = status.dinner
                )
            } catch (e: Exception) {
                _state.value = MealState.Error("Failed to fetch today's meal status")
            }
        }
    }

    fun toggleMeal(isLunch: Boolean) {
        val messId = MessRepository.currentMessId.value ?: return
        val currentState = _state.value
        if (currentState !is MealState.Success) return

        val newLunch = if (isLunch) !currentState.lunchActive else currentState.lunchActive
        val newDinner = if (!isLunch) !currentState.dinnerActive else currentState.dinnerActive
        
        updateMeals(newLunch, newDinner)
    }

    fun updateMeals(lunch: Boolean, dinner: Boolean) {
        val messId = MessRepository.currentMessId.value ?: return
        val currentState = _state.value
        if (currentState !is MealState.Success) return

        val dateStr = getTodayDateString()

        viewModelScope.launch {
            _state.value = currentState.copy(isSaving = true)
            try {
                ApiClient.apiService.toggleMeal(
                    messId,
                    MealToggleRequest(date = dateStr, lunch = lunch, dinner = dinner)
                )
                _state.value = currentState.copy(
                    lunchActive = lunch,
                    dinnerActive = dinner,
                    isSaving = false
                )
            } catch (e: Exception) {
                _state.value = currentState.copy(isSaving = false)
                // Reload from backend to revert because it likely failed due to deadline
                loadTodayMeal()
            }
        }
    }
}
