package com.messmate.android.service

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object FcmEventBus {
    // Using a SharedFlow with buffer to ensure no events are missed even if multiple arrive rapidly
    private val _events = MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    fun emitEvent(type: String) {
        _events.tryEmit(type)
    }
}
