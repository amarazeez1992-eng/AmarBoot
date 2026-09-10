package com.personal.gridbot.amaros.core

import com.personal.gridbot.amaros.data.AmarDataProvider
import com.personal.gridbot.amaros.data.DemoDataProvider
import com.personal.gridbot.amaros.intelligence.DecisionEngine
import com.personal.gridbot.amaros.intelligence.MarketContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * B9 runtime integration. Owns one read-only foundation cycle and exposes the
 * latest result as state for any presentation engine. It never executes trades.
 */
class AmarRuntimeController(
    private val provider: AmarDataProvider = DemoDataProvider()
) {
    data class RuntimeState(
        val cycleNumber: Long = 0L,
        val telemetry: AmarUiContract.Telemetry = AmarUiContract.Telemetry(),
        val context: MarketContext? = null,
        val decision: DecisionEngine.DecisionProposal? = null,
        val risk: AmarRiskGate.Result? = null,
        val execution: AmarExecutionBoundary.ExecutionResult? = null,
        val lastUpdateEpochMs: Long = 0L
    )

    private val cycle = AmarFoundationCycle(provider)
    private val _state = MutableStateFlow(RuntimeState())
    val state: StateFlow<RuntimeState> = _state.asStateFlow()

    fun advance(): RuntimeState {
        val result = cycle.runDemoCycle()
        val next = RuntimeState(
            cycleNumber = _state.value.cycleNumber + 1L,
            telemetry = result.telemetry,
            context = result.data.let { com.personal.gridbot.amaros.intelligence.MarketAnalyzer().analyze(it) },
            decision = result.decision,
            risk = result.risk,
            execution = result.execution,
            lastUpdateEpochMs = result.data.generatedAtEpochMs
        )
        _state.value = next
        return next
    }
}
