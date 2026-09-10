package com.personal.gridbot.amaros.core

import com.personal.gridbot.amaros.intelligence.DecisionEngine
import com.personal.gridbot.amaros.intelligence.DecisionValidation

/**
 * B8 execution boundary. The application can describe an action, but this contract
 * deliberately contains no broker/network/trading implementation in Demo mode.
 */
object AmarExecutionBoundary {
    data class ExecutionRequest(
        val proposal: DecisionEngine.DecisionProposal,
        val validation: DecisionValidation,
        val mode: AmarOperatingMode = AmarOperatingMode.DEMO
    )

    data class ExecutionResult(
        val accepted: Boolean,
        val executed: Boolean,
        val message: String
    )

    fun submit(request: ExecutionRequest): ExecutionResult {
        if (request.mode != AmarOperatingMode.LIVE || !request.validation.allowed) {
            return ExecutionResult(
                accepted = false,
                executed = false,
                message = "تم حظر التنفيذ: AMAR في وضع DEMO/الحماية"
            )
        }
        return ExecutionResult(
            accepted = true,
            executed = false,
            message = "لا يوجد منفذ وساطة موصول في B8 Demo"
        )
    }
}
