package com.personal.gridbot.amaros.agent

/** Provider-neutral local model boundary. No hosted AI service is required by the core. */
interface AmarModelProvider {
    val id: String
    suspend fun load(modelPath: String): AmarModelInfo
    suspend fun generate(request: AmarGenerationRequest): AmarGenerationResult
    suspend fun unload()
}

data class AmarGenerationRequest(
    val systemPrompt: String,
    val userPrompt: String,
    val context: String = "",
    val maxTokens: Int = 512,
    val temperature: Double = 0.2
) {
    init {
        require(maxTokens in 1..8192)
        require(temperature in 0.0..2.0)
    }
}

data class AmarGenerationResult(
    val text: String,
    val inputTokens: Int = 0,
    val outputTokens: Int = 0,
    val elapsedMs: Long = 0L
)

data class AmarModelInfo(
    val id: String,
    val format: String,
    val contextTokens: Int,
    val estimatedRamMb: Int,
    val loaded: Boolean
)

/** Explicit fallback for devices before native local inference is installed. */
class AmarUnavailableLocalModelProvider : AmarModelProvider {
    override val id: String = "local-unavailable"
    override suspend fun load(modelPath: String) = AmarModelInfo(id, "none", 0, 0, false)
    override suspend fun generate(request: AmarGenerationRequest) =
        AmarGenerationResult("لا يوجد نموذج محلي محمّل حاليًا؛ سيتم استخدام طبقة AMAR الآمنة الاحتياطية.")
    override suspend fun unload() = Unit
}
