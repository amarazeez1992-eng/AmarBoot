package com.personal.gridbot.amaros.core

import com.personal.gridbot.amaros.data.AmarDataProvider
import com.personal.gridbot.amaros.data.DemoDataProvider
import com.personal.gridbot.amaros.intelligence.DecisionEngine
import com.personal.gridbot.amaros.intelligence.MarketContext
import com.personal.gridbot.amaros.intelligence.MarketAnalyzer
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * B9 runtime integration plus B11/B12 state, event and health expansion.
 * Owns one read-only foundation cycle and exposes the latest result as state
 * for any presentation engine. It never executes trades.
 */
class AmarRuntimeController(
    private val provider: AmarDataProvider = DemoDataProvider(),
    private val monitor: AmarRuntimeMonitor = AmarRuntimeMonitor(),
    private val clock: AmarClock = SystemAmarClock
) {
    data class RuntimeState(
        val cycleNumber: Long = 0L,
        val telemetry: AmarUiContract.Telemetry = AmarUiContract.Telemetry(),
        val context: MarketContext? = null,
        val decision: DecisionEngine.DecisionProposal? = null,
        val risk: AmarRiskGate.Result? = null,
        val execution: AmarExecutionBoundary.ExecutionResult? = null,
        val lastUpdateEpochMs: Long = 0L,
        val health: AmarRuntimeHealth = AmarRuntimeHealth()
    )

    private val cycle = AmarFoundationCycle(provider)
    private val _state = MutableStateFlow(RuntimeState())
    val state: StateFlow<RuntimeState> = _state.asStateFlow()
    val health: StateFlow<AmarRuntimeHealth> = monitor.state

    fun onRuntimeStarted() {
        val now = clock.nowEpochMs()
        monitor.markStarted(now)
        publishHealth()
    }

    fun onRuntimeStopped() {
        val now = clock.nowEpochMs()
        monitor.markStopped(now)
        _state.value = _state.value.copy(health = monitor.state.value)
        publishHealth()
    }

    fun advance(): RuntimeState {
        val cycleNumber = _state.value.cycleNumber + 1L
        val correlationId = UUID.randomUUID().toString()
        val startedAt = clock.nowEpochMs()
        AmarEventBus.publish(AmarEvent.RuntimeCycleStarted(cycleNumber, startedAt, correlationId = correlationId))

        return try {
            val result = cycle.runDemoCycle()
            val context = result.data.let { MarketAnalyzer().analyze(it) }
            val finishedAt = clock.nowEpochMs()
            monitor.recordSuccess(finishedAt - startedAt, finishedAt)
            val next = RuntimeState(
                cycleNumber = cycleNumber,
                telemetry = result.telemetry,
                context = context,
                decision = result.decision,
                risk = result.risk,
                execution = result.execution,
                lastUpdateEpochMs = result.data.generatedAtEpochMs,
                health = monitor.state.value
            )
            _state.value = next
            AmarEventBus.publish(
                AmarEvent.RuntimeCycleCompleted(
                    cycleNumber = cycleNumber,
                    durationMs = finishedAt - startedAt,
                    symbol = result.telemetry.symbol,
                    timeframe = result.telemetry.timeframe,
                    decision = result.decision.direction.name,
                    confidence = result.decision.confidence,
                    riskAllowed = result.risk.allowed,
                    executionMode = if (result.execution.executed) "EXECUTED" else "DEMO_GUARDED",
                    correlationId = correlationId
                )
            )
            publishHealth(correlationId)
            next
        } catch (error: Throwable) {
            val finishedAt = clock.nowEpochMs()
            monitor.recordFailure(error, finishedAt - startedAt, finishedAt)
            val next = _state.value.copy(
                cycleNumber = cycleNumber,
                lastUpdateEpochMs = finishedAt,
                health = monitor.state.value
            )
            _state.value = next
            AmarEventBus.publish(
                AmarEvent.RuntimeCycleFailed(
                    cycleNumber = cycleNumber,
                    durationMs = finishedAt - startedAt,
                    error = error.message ?: error::class.simpleName ?: "Runtime error",
                    correlationId = correlationId
                )
            )
            publishHealth(correlationId)
            next
        }
    }

    private fun publishHealth(correlationId: String = UUID.randomUUID().toString()) {
        val snapshot = monitor.state.value
        AmarEventBus.publish(
            AmarEvent.RuntimeHealthChanged(
                status = snapshot.status,
                consecutiveFailures = snapshot.consecutiveFailures,
                totalFailures = snapshot.totalFailures,
                correlationId = correlationId
            )
        )
    }
}
