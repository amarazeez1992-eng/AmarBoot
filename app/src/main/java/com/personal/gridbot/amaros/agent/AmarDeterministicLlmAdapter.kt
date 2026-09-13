package com.personal.gridbot.amaros.agent

/**
 * Safe deterministic fallback used when no local model is available.
 * It deliberately does not pretend to be an LLM or execute actions.
 */
class AmarDeterministicLlmAdapter : AmarLlmRuntimeAdapter {
    override val runtimeId: String = "deterministic-fallback"
    override val modelId: String? = null
    override val isReady: Boolean = true

    override suspend fun generate(request: AmarLlmGenerationRequest): AmarLlmGenerationResult {
        val normalized = request.prompt.trim()
        return AmarLlmGenerationResult.Success(
            "Deterministic fallback: request received without a local LLM model. " +
                "No broker action was executed. Request length=${normalized.length}."
        )
    }
}
