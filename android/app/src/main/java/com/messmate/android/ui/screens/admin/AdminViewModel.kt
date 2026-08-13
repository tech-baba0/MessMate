package com.messmate.android.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.meal.AdminMealDashboardResponse
import com.messmate.android.data.mess.MessMemberResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminState {
    object Loading : AdminState()
    data class Success(
        val members: List<MessMemberResponse>,
        val mealDashboard: AdminMealDashboardResponse
    ) : AdminState()
    data class Error(val message: String) : AdminState()
}

class AdminViewModel : ViewModel() {
    private val _state = MutableStateFlow<AdminState>(AdminState.Loading)
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _state.value = AdminState.Loading
            try {
                // Fetch both API endpoints concurrently
                val membersDeferred = async { ApiClient.apiService.getMessMembers(messId) }
                val statsDeferred = async { ApiClient.apiService.getAdminMealDashboard(messId) }
                
                val members = membersDeferred.await()
                val stats = statsDeferred.await()
                
                _state.value = AdminState.Success(members, stats)
            } catch (e: Exception) {
                _state.value = AdminState.Error("Failed to fetch admin data: ${e.localizedMessage}")
            }
        }
    }

    fun approveMember(memberId: String) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.approveMember(messId, memberId)
                loadDashboard()
            } catch (e: Exception) {
                // Ignore for now or handle locally
            }
        }
    }

    fun rejectMember(memberId: String) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.rejectMember(messId, memberId)
                loadDashboard()
            } catch (e: Exception) {
                // Ignore for now or handle locally
            }
        }
    }

    fun changeRole(memberId: String, newRole: String) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.changeMemberRole(messId, memberId, newRole)
                loadDashboard()
            } catch (e: Exception) {
                // Ignore for now or handle locally
            }
        }
    }
}
