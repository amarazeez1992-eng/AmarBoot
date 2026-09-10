package com.personal.gridbot.amaros.core

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Monotonic sequence source for deterministic event ordering and audit correlation. */
object AmarEventSequence {
    private val counter = AtomicLong(0L)
    fun next(): Long = counter.incrementAndGet()
}

/** Typed internal event bus with stable correlation and monotonic sequence metadata. */
sealed interface AmarEvent {
    val sequence: Long
    val correlationId: String

    data class RoomSelected(
        val room: String,
        override val sequence: Long = AmarEventSequence.next(),
        override val correlationId: String = UUID.randomUUID().toString()
    ) : AmarEvent

    data class SystemMessage(
        val message: String,
        override val sequence: Long = AmarEventSequence.next(),
        override val correlationId: String = UUID.randomUUID().toString()
    ) : AmarEvent

    data object EmergencyStop : AmarEvent {
        override val sequence: Long = AmarEventSequence.next()
        override val correlationId: String = UUID.randomUUID().toString()
    }

    data class RuntimeCycleStarted(
        val cycleNumber: Long,
        val epochMs: Long,
        override val sequence: Long = AmarEventSequence.next(),
        override val correlationId: String = UUID.randomUUID().toString()
    ) : AmarEvent

    data class RuntimeCycleCompleted(
        val cycleNumber: Long,
        val durationMs: Long,
        val symbol: String,
        val timeframe: String,
        val decision: String,
        val confidence: Double,
        val riskAllowed: Boolean,
        val executionMode: String,
        override val sequence: Long = AmarEventSequence.next(),
        override val correlationId: String = UUID.randomUUID().toString()
    ) : AmarEvent

    data class RuntimeCycleFailed(
        val cycleNumber: Long,
        val durationMs: Long,
        val error: String,
        override val sequence: Long = AmarEventSequence.next(),
        override val correlationId: String = UUID.randomUUID().toString()
    ) : AmarEvent

    data class RuntimeHealthChanged(
        val status: AmarRuntimeHealthStatus,
        val consecutiveFailures: Int,
        val totalFailures: Long,
        override val sequence: Long = AmarEventSequence.next(),
        override val correlationId: String = UUID.randomUUID().toString()
    ) : AmarEvent
}

object AmarEventBus {
    private val _events = MutableSharedFlow<AmarEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AmarEvent> = _events.asSharedFlow()

    fun publish(event: AmarEvent) {
        _events.tryEmit(event)
    }
}
