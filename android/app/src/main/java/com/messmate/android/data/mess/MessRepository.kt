package com.messmate.android.data.mess

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MessRepository {
    private val _currentMessId = MutableStateFlow<String?>(null)
    val currentMessId: StateFlow<String?> = _currentMessId.asStateFlow()

    fun setCurrentMessId(id: String) {
        _currentMessId.value = id
    }
    
    fun getMessId(): String? = _currentMessId.value
}
