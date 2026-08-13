package com.messmate.android.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.expense.ExpenseRequest
import com.messmate.android.data.expense.ExpenseResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminExpenseState {
    object Loading : AdminExpenseState()
    data class Success(val expenses: List<ExpenseResponse>) : AdminExpenseState()
    data class Error(val message: String) : AdminExpenseState()
}

class AdminExpenseViewModel : ViewModel() {
    private val _state = MutableStateFlow<AdminExpenseState>(AdminExpenseState.Loading)
    val state: StateFlow<AdminExpenseState> = _state.asStateFlow()

    init {
        fetchExpenses()
    }

    fun fetchExpenses() {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _state.value = AdminExpenseState.Loading
            try {
                val expenses = ApiClient.apiService.getAllExpenses(messId)
                _state.value = AdminExpenseState.Success(expenses)
            } catch (e: Exception) {
                _state.value = AdminExpenseState.Error("Failed to fetch expenses: ${e.localizedMessage}")
            }
        }
    }

    fun cancelExpense(expenseId: String) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.cancelExpense(messId, expenseId)
                fetchExpenses()
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }
}
