package com.personal.gridbot.amaros.agent

/** Selects the bounded capability responsible for each orchestration task. */
data class EngineSelection(
    val taskId: String,
    val engineId: String
)

class EngineSelector {
    fun select(task: AmarTaskUnit): EngineSelection {
        val engine = when (task.kind) {
            AmarTaskKind.NORMALIZE -> "INTENT_NORMALIZER"
            AmarTaskKind.UNDERSTAND -> "INTENT_UNDERSTANDING"
            AmarTaskKind.CONTEXT -> "CONTEXT_ENGINE"
            AmarTaskKind.CONSTRAINT -> "CONSTRAINT_ENGINE"
            AmarTaskKind.EVIDENCE -> "RESEARCH_ENGINE"
            AmarTaskKind.REASON -> "REASONING_PROVIDER"
            AmarTaskKind.CHALLENGE -> "CRITIC"
            AmarTaskKind.VALIDATE -> "VERIFICATION_LAYER"
            AmarTaskKind.RESPONSE -> "RESPONSE_ENGINE"
            AmarTaskKind.AUDIT -> "DECISION_VERIFIER"
        }
        require(engine.isNotBlank()) { "No engine selected for task: " + task.id }
        return EngineSelection(task.id, engine)
    }
}
