package com.personal.gridbot.amaros.agent

/** Rejects malformed provider output. It never repairs or fabricates a result. */
data class AmarProviderIntegrityResult(val accepted: Boolean, val reason: String)

class AmarProviderResultIntegrity {
    fun validate(
        providerId: String,
        request: AmarGenerationRequest,
        result: AmarGenerationResult
    ): AmarProviderIntegrityResult = when {
        providerId.isBlank() -> AmarProviderIntegrityResult(false, "PROVIDER_ID_BLANK")
        result.text.isBlank() -> AmarProviderIntegrityResult(false, "RESULT_TEXT_BLANK")
        result.inputTokens < 0 -> AmarProviderIntegrityResult(false, "INPUT_TOKENS_NEGATIVE")
        result.outputTokens < 0 -> AmarProviderIntegrityResult(false, "OUTPUT_TOKENS_NEGATIVE")
        result.outputTokens > request.maxTokens ->
            AmarProviderIntegrityResult(false, "OUTPUT_TOKENS_EXCEED_REQUEST")
        result.elapsedMs < 0 -> AmarProviderIntegrityResult(false, "ELAPSED_MS_NEGATIVE")
        else -> AmarProviderIntegrityResult(true, "VALID")
    }
}
