package com.personal.gridbot.amaros.agent

/**
 * Immutable safety budget for the AMAR AI agent.
 *
 * Normalization is deliberately performed in dependency order: values that
 * depend on another limit are clamped against the already-normalized limit,
 * never against the caller's unsafe raw value.
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
        private const val MAX_STEPS = 100
        private const val DEFAULT_MAX_STEPS = 24

        private const val MIN_TOOL_CALLS = 1
        private const val MAX_TOOL_CALLS = 500
        private const val DEFAULT_MAX_TOOL_CALLS = 80

        private const val MIN_SOURCES = 1
        private const val MAX_SOURCES = 100
        private const val DEFAULT_MAX_SOURCES = 100

        private const val MIN_TARGET_INDEPENDENT_SOURCES = 1
        private const val DEFAULT_TARGET_INDEPENDENT_SOURCES = 40

        private const val MIN_CONTEXT_TOKENS = 1_024
        private const val MAX_CONTEXT_TOKENS = 131_072
        private const val DEFAULT_MAX_CONTEXT_TOKENS = 32_768

        private const val MIN_TIMEOUT_MS = 1_000L
        private const val MAX_TIMEOUT_MS = 120_000L
        private const val DEFAULT_TIMEOUT_MS = 30_000L
    }
}
