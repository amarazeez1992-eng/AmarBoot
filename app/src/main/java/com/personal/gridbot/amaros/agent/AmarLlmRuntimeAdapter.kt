package com.personal.gridbot.amaros.agent

/**
 * Vendor-neutral local LLM boundary.
 * Implementations may later bind to GGUF/llama.cpp or another on-device runtime.
 * This interface has no broker or execution authority.
 */
interface AmarLlmRuntimeAdapter : AmarReasoningProvider {
    val runtimeId: String
    val modelId: String?
    val isReady: Boolean

    suspend fun generate(request: AmarLlmGenerationRequest): AmarLlmGenerationResult

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
        if (!isReady) return AmarAgentResponse(
            answer = "Local LLM runtime is not ready.",
            status = AmarAgentResponse.Status.ERROR
        )
        val result = generate(
            AmarLlmGenerationRequest(
                prompt = context.userText,
                maxTokens = 512
            )
        )
        return when (result) {
            is AmarLlmGenerationResult.Success -> AmarAgentResponse(result.text)
            is AmarLlmGenerationResult.Failure -> AmarAgentResponse(
                answer = result.message,
                status = AmarAgentResponse.Status.ERROR
            )
        }
    }
}

data class AmarLlmGenerationRequest(
    val prompt: String,
    val maxTokens: Int = 512,
    val temperature: Double = 0.2
) {
    init {
        require(prompt.isNotBlank()) { "prompt must not be blank" }
        require(maxTokens in 1..8192) { "maxTokens must be between 1 and 8192" }
        require(temperature in 0.0..2.0) { "temperature must be between 0.0 and 2.0" }
    }
}

sealed interface AmarLlmGenerationResult {
    data class Success(val text: String) : AmarLlmGenerationResult {
        init { require(text.isNotBlank()) { "generated text must not be blank" } }
    }

    data class Failure(val message: String) : AmarLlmGenerationResult {
        init { require(message.isNotBlank()) { "failure message must not be blank" } }
    }
}
