package com.messmate.android.ui.screens.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.meal.MealHistorySummaryResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class MealHistoryState {
    object Loading : MealHistoryState()
    data class Success(val summary: MealHistorySummaryResponse) : MealHistoryState()
    data class Error(val message: String) : MealHistoryState()
}

class MealHistoryViewModel : ViewModel() {
    private val _state = MutableStateFlow<MealHistoryState>(MealHistoryState.Loading)
    val state: StateFlow<MealHistoryState> = _state.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadHistoryForFilter("MONTH")
    }

    fun loadHistoryForFilter(filter: String) {
        val endDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = endDate

        when (filter) {
            "WEEK" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "MONTH" -> calendar.set(Calendar.DAY_OF_MONTH, 1)
            else -> calendar.set(Calendar.DAY_OF_MONTH, 1) // default to this month
        }
        val startDate = calendar.time

        loadHistory(dateFormat.format(startDate), dateFormat.format(endDate))
    }

    private fun loadHistory(startDate: String, endDate: String) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _state.value = MealHistoryState.Loading
            try {
                val response = ApiClient.apiService.getMealHistory(messId, startDate, endDate)
                _state.value = MealHistoryState.Success(response)
            } catch (e: Exception) {
                _state.value = MealHistoryState.Error("Failed to fetch history: ${e.message}")
            }
        }
    }
}
