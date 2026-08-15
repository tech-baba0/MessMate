package com.messmate.android.ui.screens.bazar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.expense.ExpenseItem
import com.messmate.android.data.expense.ExpenseRequest
import com.messmate.android.data.expense.ExpenseResponse
import com.messmate.android.data.expense.ExpenseShare
import com.messmate.android.data.mess.MessMemberResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BazarState {
    object Idle : BazarState()
    object Loading : BazarState()
    data class PreviewReady(val shares: List<ExpenseShare>, val request: ExpenseRequest) : BazarState()
    data class Success(val response: ExpenseResponse) : BazarState()
    data class Error(val message: String) : BazarState()
}

class BazarViewModel : ViewModel() {
    private val _state = MutableStateFlow<BazarState>(BazarState.Idle)
    val state: StateFlow<BazarState> = _state.asStateFlow()
    
    private val _members = MutableStateFlow<List<MessMemberResponse>>(emptyList())
    val members: StateFlow<List<MessMemberResponse>> = _members.asStateFlow()
    
    private val _history = MutableStateFlow<List<ExpenseResponse>>(emptyList())
    val history: StateFlow<List<ExpenseResponse>> = _history.asStateFlow()

    init {
        loadMembers()
        fetchExpenses()
    }

    fun fetchExpenses() {
        val messId = MessRepository.getMessId() ?: return
        viewModelScope.launch {
            try {
                val list = ApiClient.apiService.getAllExpenses(messId)
                _history.value = list
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    private fun loadMembers() {
        val messId = MessRepository.getMessId() ?: return
        viewModelScope.launch {
            try {
                val list = ApiClient.apiService.getMessMembers(messId)
                _members.value = list.filter { it.status == "APPROVED" && (it.role == "ROLE_USER" || it.role == "USER") }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun calculatePreview(request: ExpenseRequest) {
        val messId = MessRepository.getMessId()
        if (messId == null) {
            _state.value = BazarState.Error("No active mess selected.")
            return
        }

        viewModelScope.launch {
            _state.value = BazarState.Loading
            try {
                val shares = ApiClient.apiService.calculateSplit(messId, request)
                _state.value = BazarState.PreviewReady(shares, request)
            } catch (e: Exception) {
                _state.value = BazarState.Error("Preview calculation failed: ${e.message}")
            }
        }
    }

    fun submitExpense(request: ExpenseRequest) {
        val messId = MessRepository.getMessId()
        if (messId == null) {
            _state.value = BazarState.Error("No active mess selected.")
            return
        }

        viewModelScope.launch {
            _state.value = BazarState.Loading
            try {
                val response = ApiClient.apiService.addExpense(messId, request)
                _state.value = BazarState.Success(response)
                fetchExpenses() // Refresh history upon success
            } catch (e: Exception) {
                _state.value = BazarState.Error("Failed to save expense: ${e.message}")
            }
        }
    }
    
    fun resetState() {
        _state.value = BazarState.Idle
    }
}

