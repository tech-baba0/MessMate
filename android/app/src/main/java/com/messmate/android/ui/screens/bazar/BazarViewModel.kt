package com.messmate.android.ui.screens.bazar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.expense.ExpenseItem
import com.messmate.android.data.expense.ExpenseRequest
import com.messmate.android.data.expense.ExpenseResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class BazarState {
    object Idle : BazarState()
    object Loading : BazarState()
    data class Success(val response: ExpenseResponse) : BazarState()
    data class Error(val message: String) : BazarState()
}

class BazarViewModel : ViewModel() {
    private val _state = MutableStateFlow<BazarState>(BazarState.Idle)
    val state: StateFlow<BazarState> = _state.asStateFlow()

    fun addExpense(title: String, amount: Double, description: String = "") {
        val messId = MessRepository.getMessId()
        if (messId == null) {
            _state.value = BazarState.Error("No active mess selected.")
            return
        }

        if (title.isBlank() || amount <= 0) {
            _state.value = BazarState.Error("Please provide a valid title and amount.")
            return
        }

        viewModelScope.launch {
            _state.value = BazarState.Loading
            try {
                val request = ExpenseRequest(
                    title = title,
                    description = description.ifBlank { null },
                    date = LocalDate.now().toString(),
                    totalAmount = amount,
                    splitMethod = "MEAL_BASED",
                    items = listOf(ExpenseItem(name = title, amount = amount))
                )
                val response = ApiClient.apiService.addExpense(messId, request)
                _state.value = BazarState.Success(response)
            } catch (e: Exception) {
                _state.value = BazarState.Error("Failed to save expense: ${e.localizedMessage}")
            }
        }
    }
    
    fun resetState() {
        _state.value = BazarState.Idle
    }
}
