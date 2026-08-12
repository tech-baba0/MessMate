package com.messmate.android.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.menu.Menu
import com.messmate.android.data.menu.MenuRequest
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminMenuState {
    object Idle : AdminMenuState()
    object Loading : AdminMenuState()
    data class Success(val menus: List<Menu>) : AdminMenuState()
    data class Error(val message: String) : AdminMenuState()
}

class AdminMenuViewModel : ViewModel() {
    private val _state = MutableStateFlow<AdminMenuState>(AdminMenuState.Idle)
    val state: StateFlow<AdminMenuState> = _state.asStateFlow()

    private val _saveState = MutableStateFlow<AdminMenuState>(AdminMenuState.Idle)
    val saveState: StateFlow<AdminMenuState> = _saveState.asStateFlow()

    init {
        loadMenus()
    }

    fun loadMenus() {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _state.value = AdminMenuState.Loading
            try {
                val menus = ApiClient.apiService.getAllMenusAdmin(messId)
                _state.value = AdminMenuState.Success(menus)
            } catch (e: Exception) {
                _state.value = AdminMenuState.Error("Failed to fetch menus: ${e.message}")
            }
        }
    }

    fun upsertMenu(dayOfWeek: Int, lunchRaw: String, dinnerRaw: String, isPublished: Boolean) {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _saveState.value = AdminMenuState.Loading
            try {
                val lunchList = lunchRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val dinnerList = dinnerRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                
                val req = MenuRequest(dayOfWeek, lunchList, dinnerList, isPublished)
                ApiClient.apiService.upsertMenu(messId, req) // Wait, it returns MessageResponse
                _saveState.value = AdminMenuState.Success(emptyList()) // Success signal
                loadMenus()
            } catch (e: Exception) {
                _saveState.value = AdminMenuState.Error("Failed to save menu: ${e.message}")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = AdminMenuState.Idle
    }
}
