package com.personal.gridbot.amaros.agent

/** Specialized role tasks coordinated by the main AmarAgentHierarchy. */
enum class AmarSpecializedRole {
    PLANNER,
    RESEARCHER,
    QUANT_ANALYST,
    RISK_ANALYST,
    ADVERSARIAL_CRITIC,
    VERIFIER,
    SYNTHESIZER
}

data class AmarRoleTask(
    val role: AmarSpecializedRole,
    val objective: String,
    val requiredEvidence: List<String> = emptyList(),
    val allowedScopes: Set<AmarToolScope> = setOf(AmarToolScope.READ_ONLY)
)
