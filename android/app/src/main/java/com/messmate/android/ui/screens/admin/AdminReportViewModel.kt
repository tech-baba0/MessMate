package com.messmate.android.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.meal.MealReportEntry
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReportState {
    object Idle : ReportState()
    object Loading : ReportState()
    data class Success(val entries: List<MealReportEntry>) : ReportState()
    data class Error(val message: String) : ReportState()
}

class AdminReportViewModel : ViewModel() {
    private val _state = MutableStateFlow<ReportState>(ReportState.Idle)
    val state: StateFlow<ReportState> = _state.asStateFlow()

    fun fetchReport(startDate: String, endDate: String) {
        val messId = MessRepository.getMessId() ?: return
        viewModelScope.launch {
            _state.value = ReportState.Loading
            try {
                val entries = ApiClient.apiService.getMealReport(messId, startDate, endDate)
                _state.value = ReportState.Success(entries)
            } catch (e: Exception) {
                _state.value = ReportState.Error("Failed to load report: ${e.message}")
            }
        }
    }
}
