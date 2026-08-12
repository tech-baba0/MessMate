package com.messmate.android.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.messmate.android.data.menu.Menu
import com.messmate.android.data.mess.MessRepository
import com.messmate.android.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MenuState {
    object Loading : MenuState()
    data class Success(val menus: List<Menu>) : MenuState()
    data class Error(val message: String) : MenuState()
}

class MenuViewModel : ViewModel() {
    private val _state = MutableStateFlow<MenuState>(MenuState.Loading)
    val state: StateFlow<MenuState> = _state.asStateFlow()

    init {
        loadMenus()
    }

    fun loadMenus() {
        val messId = MessRepository.currentMessId.value ?: return
        viewModelScope.launch {
            _state.value = MenuState.Loading
            try {
                // ApiClient.apiService.getPublishedMenus(messId)
                // Wait, I didn't add getPublishedMenus to ApiService!
                // I will add it shortly, but first let's map it.
                val response = ApiClient.apiService.getPublishedMenus(messId)
                _state.value = MenuState.Success(response)
            } catch (e: Exception) {
                _state.value = MenuState.Error("Failed to fetch menus: ${e.message}")
            }
        }
    }
}
