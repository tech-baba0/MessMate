package com.messmate.android.ui.screens.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.meal.MealResponse
import com.messmate.android.data.meal.MealToggleRequest
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class MealState {
    object Loading : MealState()
    data class Success(val meal: MealResponse?) : MealState() // Null if no meal selected yet today
    data class Error(val message: String) : MealState()
}

class MealViewModel : ViewModel() {
    private val _state = MutableStateFlow<MealState>(MealState.Loading)
    val state: StateFlow<MealState> = _state.asStateFlow()

    private val todayString = LocalDate.now().toString()

    init {
        loadTodayMeal()
    }

    private fun loadTodayMeal() {
        val messId = MessRepository.getMessId()
        if (messId == null) {
            _state.value = MealState.Error("No active mess found. Please return to Dashboard.")
            return
        }

        viewModelScope.launch {
            _state.value = MealState.Loading
            try {
                val history = ApiClient.apiService.getMealHistory(messId, todayString, todayString)
                if (history.isNotEmpty()) {
                    _state.value = MealState.Success(history.first())
                } else {
                    _state.value = MealState.Success(null)
                }
            } catch (e: Exception) {
                _state.value = MealState.Error("Failed to load today's meal: ${e.localizedMessage}")
            }
        }
    }

    fun toggleMeal(lunch: Boolean, dinner: Boolean) {
        val messId = MessRepository.getMessId() ?: return
        
        viewModelScope.launch {
            _state.value = MealState.Loading
            try {
                val request = MealToggleRequest(
                    date = todayString,
                    lunch = lunch,
                    dinner = dinner
                )
                val response = ApiClient.apiService.toggleMeal(messId, request)
                _state.value = MealState.Success(response)
            } catch (e: Exception) {
                _state.value = MealState.Error("Failed to update meal: ${e.localizedMessage}")
                loadTodayMeal() // Reload original state on error
            }
        }
    }
}
