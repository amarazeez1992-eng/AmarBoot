package com.personal.gridbot.amaros.agent

import com.google.mlkit.genai.prompt.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.flow.collect

/**
 * On-device generation provider backed by Android AICore / Gemini Nano.
 *
 * It is provider-local: no API key and no hosted model are required.
 * The Agent Core remains provider-neutral and retains fail-closed execution boundaries.
 */
class AmarGeminiNanoModelProvider : AmarModelProvider {
    override val id: String = "android-aicore-gemini-nano"

    private val model = Generation.getClient()

    override suspend fun load(modelPath: String): AmarModelInfo {
        return when (model.checkStatus()) {
            FeatureStatus.AVAILABLE -> AmarModelInfo(
                id = id,
                format = "AICore/Gemini-Nano",
                contextTokens = 0,
                estimatedRamMb = 0,
                loaded = true
            )
            FeatureStatus.DOWNLOADABLE -> {
                model.download().collect { }
                AmarModelInfo(
                    id = id,
                    format = "AICore/Gemini-Nano",
                    contextTokens = 0,
                    estimatedRamMb = 0,
                    loaded = model.checkStatus() == FeatureStatus.AVAILABLE
                )
            }
            else -> AmarModelInfo(
                id = id,
                format = "AICore/Gemini-Nano",
                contextTokens = 0,
                estimatedRamMb = 0,
                loaded = false
            )
        }
    }

    override suspend fun generate(request: AmarGenerationRequest): AmarGenerationResult {
        val prompt = buildString {
            append(request.systemPrompt.trim())
            if (request.context.isNotBlank()) {
                append("\n\nContext:\n")
                append(request.context.trim())
            }
            append("\n\nUser request:\n")
            append(request.userPrompt.trim())
        }

        val started = System.currentTimeMillis()
        val response = model.generateContent(
            generateContentRequest(TextPart(prompt)) {
                temperature = request.temperature.toFloat()
                maxOutputTokens = request.maxTokens
                candidateCount = 1
            }
        )
        val text = response.candidates.firstOrNull()?.text?.trim().orEmpty()
        require(text.isNotBlank()) { "Gemini Nano returned an empty response" }

        return AmarGenerationResult(
            text = text,
            elapsedMs = System.currentTimeMillis() - started
        )
    }

    override suspend fun unload() = Unit
}
