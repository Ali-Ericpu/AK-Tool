package com.rainccup.aktool.core.message

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class MessageBus {
    val messages: SharedFlow<String>
        field = MutableSharedFlow<String>(
            extraBufferCapacity = 8,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    fun publish(message: String) {
        messages.tryEmit(message)
    }
}
