package com.personal.gridbot.amaros.agent

/** Hard limits keep broad research fast, predictable and resistant to runaway loops. */
data class AmarAgentBudget(
    val maxSteps: Int = 24,
    val maxToolCalls: Int = 80,
    val maxSources: Int = 100,
    val targetIndependentSources: Int = 40,
    val maxContextTokens: Int = 32768,
    val timeoutMs: Long = 30_000L
) {
    fun normalized(): AmarAgentBudget = copy(
        maxSteps = maxSteps.coerceIn(1, 100),
        maxToolCalls = maxToolCalls.coerceIn(1, 500),
        maxSources = maxSources.coerceIn(1, 100),
        targetIndependentSources = targetIndependentSources.coerceIn(1, maxSources),
        maxContextTokens = maxContextTokens.coerceIn(1024, 131072),
        timeoutMs = timeoutMs.coerceIn(1_000L, 120_000L)
    )
}
