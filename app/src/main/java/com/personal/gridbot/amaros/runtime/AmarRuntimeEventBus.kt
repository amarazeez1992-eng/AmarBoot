package com.personal.gridbot.amaros.runtime

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Transport-independent in-process event bus for runtime status updates. */
class AmarRuntimeEventBus {
    private val _events = MutableSharedFlow<AmarRuntimeStatusEvent>(
        replay = 0,
        extraBufferCapacity = 64,
    )

    val events: SharedFlow<AmarRuntimeStatusEvent> = _events.asSharedFlow()

    suspend fun emit(event: AmarRuntimeStatusEvent) {
        _events.emit(event)
    }

    fun tryEmit(event: AmarRuntimeStatusEvent): Boolean = _events.tryEmit(event)
}
