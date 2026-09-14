package com.personal.gridbot.amaros.agent

/**
 * Immutable safety budget for the AMAR AI agent.
 *
 * Budgets are resource controls, not intelligence ceilings: the Agent can use
 * any admitted indicator/source capability while each run remains bounded.
 */
data class AmarAgentBudget(
    val maxSteps: Int = DEFAULT_MAX_STEPS,
    val maxToolCalls: Int = DEFAULT_MAX_TOOL_CALLS,
    val maxSources: Int = DEFAULT_MAX_SOURCES,
    val targetIndependentSources: Int = DEFAULT_TARGET_INDEPENDENT_SOURCES,
    val maxContextTokens: Int = DEFAULT_MAX_CONTEXT_TOKENS,
    val timeoutMs: Long = DEFAULT_TIMEOUT_MS
) {
    fun normalized(): AmarAgentBudget {
        val safeMaxSteps = maxSteps.coerceIn(MIN_STEPS, MAX_STEPS)
        val safeMaxToolCalls = maxToolCalls.coerceIn(MIN_TOOL_CALLS, MAX_TOOL_CALLS)
        val safeMaxSources = maxSources.coerceIn(MIN_SOURCES, MAX_SOURCES)
        val safeTargetIndependentSources = targetIndependentSources.coerceIn(
            MIN_TARGET_INDEPENDENT_SOURCES,
            safeMaxSources
        )
        val safeMaxContextTokens = maxContextTokens.coerceIn(MIN_CONTEXT_TOKENS, MAX_CONTEXT_TOKENS)
        val safeTimeoutMs = timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS)

        return AmarAgentBudget(
            maxSteps = safeMaxSteps,
            maxToolCalls = safeMaxToolCalls,
            maxSources = safeMaxSources,
            targetIndependentSources = safeTargetIndependentSources,
            maxContextTokens = safeMaxContextTokens,
            timeoutMs = safeTimeoutMs
        )
    }

    companion object {
        private const val MIN_STEPS = 1
        private const val MAX_STEPS = 200
        private const val DEFAULT_MAX_STEPS = 48

        private const val MIN_TOOL_CALLS = 1
        private const val MAX_TOOL_CALLS = 2_000
        private const val DEFAULT_MAX_TOOL_CALLS = 160

        private const val MIN_SOURCES = 1
        private const val MAX_SOURCES = 1_000
        private const val DEFAULT_MAX_SOURCES = 200

        private const val MIN_TARGET_INDEPENDENT_SOURCES = 1
        private const val DEFAULT_TARGET_INDEPENDENT_SOURCES = 80

        private const val MIN_CONTEXT_TOKENS = 1_024
        private const val MAX_CONTEXT_TOKENS = 262_144
        private const val DEFAULT_MAX_CONTEXT_TOKENS = 65_536

        private const val MIN_TIMEOUT_MS = 1_000L
        private const val MAX_TIMEOUT_MS = 300_000L
        private const val DEFAULT_TIMEOUT_MS = 60_000L
    }
}
