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

        // Fallback polling for real-time feel (every 5 seconds)
        viewModelScope.launch {
            while (isActive) {
                delay(5000)
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

    // ─── FCM Notification health ──────────────────────────────────────────────

    private val _fcmStatus = MutableStateFlow("unknown") // "unknown" | "ready" | "disabled"
    val fcmStatus: StateFlow<String> = _fcmStatus.asStateFlow()

    private val _testNotificationResult = MutableStateFlow("")
    val testNotificationResult: StateFlow<String> = _testNotificationResult.asStateFlow()

    private val _isTestingFcm = MutableStateFlow(false)
    val isTestingFcm: StateFlow<Boolean> = _isTestingFcm.asStateFlow()

    fun checkFcmStatus() {
        viewModelScope.launch {
            try {
                val result = ApiClient.apiService.getFcmStatus()
                val ready = result["fcmReady"] as? Boolean ?: false
                _fcmStatus.value = if (ready) "ready" else "disabled"
                // Always keep token fresh when FCM is live
                if (ready) refreshFcmToken()
            } catch (e: Exception) {
                _fcmStatus.value = "unknown"
            }
        }
    }

    /** Grabs the current Firebase token and saves it to the backend. */
    private fun refreshFcmToken() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        viewModelScope.launch {
                            try {
                                ApiClient.apiService.updateFcmToken(
                                    com.messmate.android.data.auth.FcmTokenRequest(task.result!!)
                                )
                            } catch (e: Exception) { /* silent */ }
                        }
                    }
                }
        } catch (e: Exception) { /* Firebase not available */ }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            _isTestingFcm.value = true
            _testNotificationResult.value = "Registering device token…"

            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    viewModelScope.launch {
                        try {
                            // Step 1: Upload token if successful
                            if (task.isSuccessful && task.result != null) {
                                try {
                                    ApiClient.apiService.updateFcmToken(
                                        com.messmate.android.data.auth.FcmTokenRequest(task.result!!)
                                    )
                                } catch (e: Exception) {
                                    // Error uploading token, but continue to try test
                                }
                            }

                            // Step 2: Send test notification AFTER token is guaranteed uploaded
                            val result = ApiClient.apiService.sendTestNotification()
                            val msg = result["message"] as? String ?: "Done"
                            _testNotificationResult.value = msg
                            checkFcmStatus()
                        } catch (e: Exception) {
                            _testNotificationResult.value = "❌ Error: ${e.localizedMessage}"
                        } finally {
                            _isTestingFcm.value = false
                        }
                    }
                }
        }
        }
    }

    fun sendAnnouncement(title: String, message: String, targetUserId: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.sendAnnouncement(
                    messId,
                    com.messmate.android.data.mess.AnnouncementRequest(title, message, targetUserId)
                )
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to send announcement: ${e.localizedMessage}")
            }
        }
    }
}

