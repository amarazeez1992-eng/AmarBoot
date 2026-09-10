package com.personal.gridbot.amaros.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** ناقل أحداث داخلي typed يفصل الوحدات عن بعضها. */
sealed interface AmarEvent {
    data class RoomSelected(val room: String) : AmarEvent
    data class SystemMessage(val message: String) : AmarEvent
    data object EmergencyStop : AmarEvent

    data class RuntimeCycleStarted(val cycleNumber: Long, val epochMs: Long) : AmarEvent
    data class RuntimeCycleCompleted(
        val cycleNumber: Long,
        val durationMs: Long,
        val symbol: String,
        val timeframe: String,
        val decision: String,
        val confidence: Double,
        val riskAllowed: Boolean,
        val executionMode: String
    ) : AmarEvent

    data class RuntimeCycleFailed(
        val cycleNumber: Long,
        val durationMs: Long,
        val error: String
    ) : AmarEvent

    data class RuntimeHealthChanged(
        val status: AmarRuntimeHealthStatus,
        val consecutiveFailures: Int,
        val totalFailures: Long
    ) : AmarEvent
}

object AmarEventBus {
    private val _events = MutableSharedFlow<AmarEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AmarEvent> = _events.asSharedFlow()

    fun publish(event: AmarEvent) {
        _events.tryEmit(event)
    }
}
