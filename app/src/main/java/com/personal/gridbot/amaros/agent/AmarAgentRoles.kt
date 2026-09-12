package com.personal.gridbot.amaros.agent

/** Specialized roles remain bounded contracts coordinated by the central policy engine. */
enum class AmarAgentRole {
    PLANNER,
    RESEARCHER,
    QUANT_ANALYST,
    RISK_ANALYST,
    ADVERSARIAL_CRITIC,
    VERIFIER,
    SYNTHESIZER
}

data class AmarRoleTask(
    val role: AmarAgentRole,
    val objective: String,
    val requiredEvidence: List<String> = emptyList(),
    val allowedScopes: Set<AmarToolScope> = setOf(AmarToolScope.READ_ONLY)
)
