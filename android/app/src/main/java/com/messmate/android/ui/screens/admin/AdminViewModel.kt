package com.messmate.android.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.mess.MessMemberResponse
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminState {
    object Loading : AdminState()
    data class Success(val members: List<MessMemberResponse>) : AdminState()
    data class Error(val message: String) : AdminState()
}

class AdminViewModel : ViewModel() {
    private val _state = MutableStateFlow<AdminState>(AdminState.Loading)
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        fetchMembers()
    }

    fun fetchMembers() {
        val messId = MessRepository.currentMessId ?: return
        viewModelScope.launch {
            _state.value = AdminState.Loading
            try {
                val members = ApiClient.apiService.getMessMembers(messId)
                _state.value = AdminState.Success(members)
            } catch (e: Exception) {
                _state.value = AdminState.Error("Failed to fetch members: ${e.localizedMessage}")
            }
        }
    }

    fun approveMember(memberId: String) {
        val messId = MessRepository.currentMessId ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.approveMember(messId, memberId)
                fetchMembers()
            } catch (e: Exception) {
                // Ignore for now or handle
            }
        }
    }

    fun rejectMember(memberId: String) {
        val messId = MessRepository.currentMessId ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.rejectMember(messId, memberId)
                fetchMembers()
            } catch (e: Exception) {
                // Ignore for now or handle
            }
        }
    }

    fun changeRole(memberId: String, newRole: String) {
        val messId = MessRepository.currentMessId ?: return
        viewModelScope.launch {
            try {
                ApiClient.apiService.changeMemberRole(messId, memberId, newRole)
                fetchMembers()
            } catch (e: Exception) {
                // Ignore for now or handle
            }
        }
    }
}
