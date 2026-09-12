package com.personal.gridbot.amaros.agent

import java.util.UUID

class AmarAgentSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val traceId: String = UUID.randomUUID().toString(),
    val budget: AmarAgentBudget = AmarAgentBudget().normalized()
) {
    private val events = mutableListOf<AmarAgentEvent>()

    fun record(stage: AmarAgentStage, message: String) {
        if (events.size < budget.maxSteps) events += AmarAgentEvent(stage, message)
    }

    fun events(): List<AmarAgentEvent> = events.toList()
}

data class AmarAgentEvent(val stage: AmarAgentStage, val message: String)

enum class AmarAgentStage {
    INTAKE, PLAN, RETRIEVE, VERIFY, REASON, CHALLENGE, SIMULATE, VALIDATE, RISK_GATE, SYNTHESIZE, APPROVAL, COMPLETE, BLOCKED
}
