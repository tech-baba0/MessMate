package com.messmate.android.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.meal.AdminMealDashboardResponse
import com.messmate.android.data.mess.MessMemberResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import com.messmate.android.service.FcmEventBus
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboard()
        
        // Listen for live updates via FCM
        viewModelScope.launch {
            FcmEventBus.events.collect { type ->
                // Refresh on any relevant update
                if (type.contains("MEAL", ignoreCase = true) || 
                    type.contains("MEMBER", ignoreCase = true) || 
                    type.contains("EXPENSE", ignoreCase = true)) {
                    loadDashboard(isSilent = true)
                }
            }
        }

        // Fallback polling for real-time feel (every 10 seconds)
        viewModelScope.launch {
            while (isActive) {
                delay(10000)
                loadDashboard(isSilent = true)
            }
        }
    }

    fun loadDashboard(isSilent: Boolean = false) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            if (!isSilent && _state.value !is AdminState.Success) {
                _state.value = AdminState.Loading
            }
            _isRefreshing.value = true
            try {
                supervisorScope {
                    val membersDeferred = async { ApiClient.apiService.getMessMembers(messId) }
                    val statsDeferred = async { ApiClient.apiService.getAdminMealDashboard(messId) }
                    
                    val members = try {
                        membersDeferred.await()
                    } catch (e: Exception) {
                        if (_state.value is AdminState.Success) (_state.value as AdminState.Success).members else emptyList()
                    }
                    
                    val stats = try {
                        statsDeferred.await()
                    } catch (e: Exception) {
                        if (_state.value is AdminState.Success) {
                            (_state.value as AdminState.Success).mealDashboard
                        } else {
                            AdminMealDashboardResponse(
                                todayLunchYes = 0, todayLunchNo = 0,
                                todayDinnerYes = 0, todayDinnerNo = 0,
                                totalLunchMeals = 0, totalDinnerMeals = 0,
                                totalMealUnits = 0,
                                lunchVotingStatus = "UNKNOWN",
                                dinnerVotingStatus = "UNKNOWN"
                            )
                        }
                    }
                    
                    _state.value = AdminState.Success(members, stats)
                }
            } catch (e: Exception) {
                if (!isSilent) {
                    _state.value = AdminState.Error("Failed to fetch admin data: ${e.localizedMessage}")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun approveMember(memberId: String) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.approveMember(messId, memberId)
                loadDashboard(isSilent = true)
            } catch (e: Exception) {}
        }
    }

    fun rejectMember(memberId: String) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.rejectMember(messId, memberId)
                loadDashboard(isSilent = true)
            } catch (e: Exception) {}
        }
    }

    fun changeRole(memberId: String, newRole: String) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.changeMemberRole(messId, memberId, newRole)
                loadDashboard(isSilent = true)
            } catch (e: Exception) {}
        }
    }
}
