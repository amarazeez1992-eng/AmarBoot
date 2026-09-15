package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.agent.AmarLocalReasoning
import com.personal.gridbot.amaros.agent.AmarAgentContext

/** Stable UI-to-AMAR boundary. The UI talks to the AMAR agent first; external providers are optional adapters. */
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
        return runCatching {
            val result = agent.ask(apiKey, model, request)
            Response(
                answer = result.answer,
                provider = if (externalProviderEnabled && apiKey.isNotBlank() && model.isNotBlank()) "AMAR+OPTIONAL_EXTERNAL_SYNTHESIS" else "AMAR_LOCAL",
                evidence = result.toolEvidence,
                engineIds = mesh.connectedEngineIds()
            )
        }.getOrElse { failure ->
            // Fail closed: if an optional/unstable engine path throws, keep the UI alive and expose
            // only deterministic evidence that can be read directly from existing AMAR engines.
            val evidence = buildList {
                val q = request.lowercase()
                if (listOf("سوق", "market", "xau", "gold", "ذهب", "تحليل").any { q.contains(it) }) {
                    runCatching { AmarAiEngineBinding.market() }.getOrNull()?.let(::add)
                }
                if (listOf("مخاطر", "risk", "دقة", "precision", "ثقة").any { q.contains(it) }) {
                    runCatching { AmarAiEngineBinding.riskGate() }.getOrNull()?.let(::add)
                }
            }.distinct()
            val fallbackContext = AmarAgentContext(
                userText = request,
                tools = emptyList(),
                executionAllowed = false,
                brokerAccessAllowed = false
            )
            val fallback = AmarLocalReasoning().respond(fallbackContext)
            Response(
                answer = fallback.answer + if (evidence.isEmpty()) "\n\nحالة الوكيل: DATA_UNAVAILABLE / fail-closed (${failure::class.simpleName})." else "\n\n" + evidence.joinToString("\n"),
                provider = "AMAR_LOCAL_FAIL_CLOSED",
                evidence = evidence,
                engineIds = mesh.connectedEngineIds()
            )
        }
    }
}
