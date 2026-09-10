package com.personal.gridbot.amaros.core

import com.personal.gridbot.amaros.data.AmarDataSnapshot
import com.personal.gridbot.amaros.intelligence.DecisionEngine
import com.personal.gridbot.amaros.intelligence.DecisionValidation

/**
 * B8 execution boundary. It accepts a validated command contract only; this layer
 * contains no broker/network implementation and Demo mode can never execute trades.
 */
object AmarExecutionBoundary {
    data class ExecutionRequest(
        val proposal: DecisionEngine.DecisionProposal,
        val validation: DecisionValidation,
        val data: AmarDataSnapshot,
        val mode: AmarOperatingMode = AmarOperatingMode.DEMO,
        val requestId: String = ""
    )

    data class ExecutionResult(
        val accepted: Boolean,
        val executed: Boolean,
        val requestId: String,
        val message: String
    )

    fun submit(request: ExecutionRequest): ExecutionResult {
        val id = request.requestId.ifBlank { "B8-${request.data.generatedAtEpochMs}" }
        if (request.mode != AmarOperatingMode.LIVE || request.data.demoOnly) {
            return ExecutionResult(false, false, id, "تم حظر التنفيذ: AMAR في وضع DEMO")
        }
        if (!request.validation.allowed) {
            return ExecutionResult(false, false, id, "تم حظر التنفيذ: فشل التحقق — ${request.validation.reason}")
        }
        val risk = AmarRiskGate.validate(request.data, request.proposal)
        if (!risk.allowed) {
            return ExecutionResult(false, false, id, "تم حظر التنفيذ: ${risk.reason}")
        }
        return ExecutionResult(true, false, id, "اجتازت العملية البوابات؛ لا يوجد Broker Adapter موصول في B8")
    }
}
