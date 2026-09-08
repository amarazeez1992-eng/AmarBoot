package com.personal.gridbot.amaros.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** ناقل أحداث داخلي يفصل الوحدات عن بعضها. */
sealed interface AmarEvent {
    data class RoomSelected(val room: String) : AmarEvent
    data class SystemMessage(val message: String) : AmarEvent
    data object EmergencyStop : AmarEvent
}

object AmarEventBus {
    private val _events = MutableSharedFlow<AmarEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<AmarEvent> = _events.asSharedFlow()

    fun publish(event: AmarEvent) {
        _events.tryEmit(event)
    }
}
