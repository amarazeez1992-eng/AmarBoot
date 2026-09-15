package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.agent.AmarAgentContext

/** Stable UI-to-AMAR boundary. The UI talks to AMAR first; external providers are optional adapters. */
class AmarAiUiEngineBridge(
    private val context: Context? = null,
    private val externalProviderEnabled: Boolean = false,
    private val externalProvider: AmarGeminiClient? = null
) {
    data class Response(
        val answer: String,
        val provider: String,
        val evidence: List<String>,
        val engineIds: List<String>
    )

    suspend fun ask(apiKey: String, model: String, request: String): Response {
        val mesh = AmarAiEngineMesh()
        val agent = AmarAiAgentEngine(
            context = context,
            gemini = if (externalProviderEnabled && apiKey.isNotBlank() && model.isNotBlank()) externalProvider ?: AmarGeminiClient() else null,
            mesh = mesh
        )
        val result = agent.ask(apiKey, model, request)
        return Response(
            answer = result.answer,
            provider = if (externalProviderEnabled && apiKey.isNotBlank() && model.isNotBlank()) "AMAR+OPTIONAL_EXTERNAL_SYNTHESIS" else "AMAR_LOCAL",
            evidence = result.toolEvidence,
            engineIds = mesh.connectedEngineIds()
        )
    }
}
