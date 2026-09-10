package com.personal.gridbot.amaros.core

import com.personal.gridbot.amaros.data.AmarDataProvider
import com.personal.gridbot.amaros.data.AmarDataSnapshot
import com.personal.gridbot.amaros.data.AmarUiDataAdapter
import com.personal.gridbot.amaros.intelligence.DecisionEngine
import com.personal.gridbot.amaros.intelligence.DecisionPipeline

/**
 * B1-B8 integration facade. It exposes one read-only cycle for the UI and future
 * services while keeping every trading decision outside the presentation layer.
 */
class AmarFoundationCycle(
    private val provider: AmarDataProvider
) {
    data class Cycle(
        val data: AmarDataSnapshot,
        val decision: DecisionEngine.DecisionProposal,
        val risk: AmarRiskGate.Result,
        val execution: AmarExecutionBoundary.ExecutionResult,
        val telemetry: AmarUiContract.Telemetry
    )

    fun runDemoCycle(): Cycle {
        val result = DecisionPipeline(provider).evaluate()
        val data = result.intelligence.sourceData
        val risk = AmarRiskGate.validate(data, result.proposal)
        val execution = AmarExecutionBoundary.submit(
            AmarExecutionBoundary.ExecutionRequest(
                proposal = result.proposal,
                validation = result.intelligence.validation,
                data = data,
                mode = AmarOperatingMode.DEMO,
                requestId = "DEMO-${data.generatedAtEpochMs}"
            )
        )
        return Cycle(
            data = data,
            decision = result.proposal,
            risk = risk,
            execution = execution,
            telemetry = AmarUiDataAdapter.toTelemetry(data)
        )
    }
}
