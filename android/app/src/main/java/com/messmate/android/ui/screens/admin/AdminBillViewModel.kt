package com.messmate.android.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.mess.MonthlySettlementResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import org.json.JSONObject

sealed class AdminBillState {
    object Idle : AdminBillState()
    object Loading : AdminBillState()
    data class Success(val settlement: MonthlySettlementResponse, val message: String) : AdminBillState()
    data class Error(val message: String) : AdminBillState()
}

class AdminBillViewModel : ViewModel() {
    private val _state = MutableStateFlow<AdminBillState>(AdminBillState.Idle)
    val state: StateFlow<AdminBillState> = _state.asStateFlow()

    fun generateBill(monthYear: String) {
        val messId = MessRepository.currentMessId.value ?: return
        
        viewModelScope.launch {
            _state.value = AdminBillState.Loading
            try {
                val response = ApiClient.apiService.generateSettlement(messId, monthYear)
                _state.value = AdminBillState.Success(response, "Bill generated successfully!")
            } catch (e: HttpException) {
                val errorMsg = try {
                    val errorBody = e.response()?.errorBody()?.string()
                    JSONObject(errorBody ?: "{}").optString("message", "Invalid request (400)")
                } catch (ex: Exception) {
                    "Error: ${e.message()}"
                }
                _state.value = AdminBillState.Error(errorMsg)
            } catch (e: Exception) {
                _state.value = AdminBillState.Error("Connection failed: ${e.localizedMessage}")
            }
        }
    }

    fun closeBill(monthYear: String, id: String) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _state.value = AdminBillState.Loading
            try {
                val response = ApiClient.apiService.closeSettlement(messId, id, monthYear)
                _state.value = AdminBillState.Success(response, "Bill period closed.")
            } catch (e: HttpException) {
                _state.value = AdminBillState.Error("Failed to close: ${e.message()}")
            } catch (e: Exception) {
                _state.value = AdminBillState.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}
