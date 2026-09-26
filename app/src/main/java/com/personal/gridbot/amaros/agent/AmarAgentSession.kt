package com.personal.gridbot.amaros.agent

import java.util.UUID

class AmarAgentSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val traceId: String = UUID.randomUUID().toString(),
    val budget: AmarAgentBudget = AmarAgentBudget().normalized()
) {
    private val events = mutableListOf<AmarAgentEvent>()
    private val stateReducer = OrchestrationStateReducer()
    private var orchestrationSnapshot = OrchestrationStateSnapshot()

    fun record(stage: AmarAgentStage, message: String) {
        if (events.size < budget.maxSteps) events += AmarAgentEvent(stage, message)
    }

    fun state(taskState: AgentTaskState, message: String) {
        if (events.size < budget.maxSteps) events += AmarAgentEvent(AmarAgentStage.TASK_STATE, taskState.name + ": " + message)
    }

    fun transitionOrchestration(next: OrchestrationState, taskId: String? = null, reason: String? = null) {
        orchestrationSnapshot = stateReducer.transition(orchestrationSnapshot, next, taskId, reason)
        record(AmarAgentStage.TASK_STATE, "ORCHESTRATION_STATE=" + next.name + (reason?.let { ":$it" } ?: ""))
    }

    fun orchestrationState(): OrchestrationStateSnapshot = orchestrationSnapshot
    fun events(): List<AmarAgentEvent> = events.toList()
}

data class AmarAgentEvent(val stage: AmarAgentStage, val message: String)

enum class AmarAgentStage {
    INTAKE, PLAN, RETRIEVE, VERIFY, REASON, CHALLENGE, SIMULATE, VALIDATE, RISK_GATE, SYNTHESIZE, APPROVAL, TASK_STATE, COMPLETE, BLOCKED
}

enum class AgentTaskState {
    IDLE, UNDERSTANDING, PLANNING, RESEARCHING, VERIFYING, REASONING, RESPONDING,
    TIMEOUT_REACHED, INSUFFICIENT_DATA, ERROR
}

data class AmarAgentProgress(
    val state: AgentTaskState,
    val message: String,
    val sourcesSearched: Int = 0,
    val sourcesAccepted: Int = 0,
    val elapsedMs: Long = 0L
)
