package com.personal.gridbot.amaros.explainability

import com.personal.gridbot.amaros.core.AmarRuntimeController

/** B16 immutable provenance record: context -> decision -> risk -> execution boundary. */
data class AmarDecisionExplanation(
    val traceId: String,
    val cycleNumber: Long,
    val symbol: String,
    val timeframe: String,
    val decision: String,
    val score: Double,
    val confidence: Double,
    val rationale: String,
    val riskAllowed: Boolean,
    val riskReason: String,
    val executionAttempted: Boolean,
    val executionMode: String,
    val demoOnly: Boolean,
    val generatedAtEpochMs: Long
)

class AmarExplainabilityService {
    fun explain(state: AmarRuntimeController.RuntimeState, traceId: String = "cycle-${state.cycleNumber}"): AmarDecisionExplanation {
        val telemetry = state.telemetry
        val decision = state.decision
        val risk = state.risk
        return AmarDecisionExplanation(
            traceId = traceId,
            cycleNumber = state.cycleNumber,
            symbol = telemetry.symbol,
            timeframe = telemetry.timeframe,
            decision = decision?.direction?.name ?: "UNKNOWN",
            score = decision?.score ?: 0.0,
            confidence = decision?.confidence ?: 0.0,
            rationale = decision?.rationale ?: "لا يوجد قرار مكتمل لهذه الدورة",
            riskAllowed = risk?.allowed == true,
            riskReason = risk?.reason ?: "لا توجد نتيجة بوابة مخاطر",
            executionAttempted = state.execution?.executed == true,
            executionMode = if (state.execution?.executed == true) "EXECUTED" else "DEMO_GUARDED",
            demoOnly = true,
            generatedAtEpochMs = state.lastUpdateEpochMs
        )
    }
}
