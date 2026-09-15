package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.agent.AmarAgentContext
import com.personal.gridbot.amaros.agent.AmarLocalReasoning

/** UI boundary for the provider-neutral AMAR Agent. Gemini is deliberately absent from this path. */
class AmarAiUiEngineBridge(
    private val context: Context? = null
) {
    data class Response(
        val answer: String,
        val provider: String,
        val evidence: List<String>,
        val engineIds: List<String>
    )

    suspend fun ask(apiKey: String, model: String, request: String): Response {
        val mesh = AmarAiEngineMesh()
        val evidence = buildList {
            val q = request.lowercase()
            if (listOf("سوق", "market", "xau", "gold", "ذهب", "تحليل").any { q.contains(it) }) {
                runCatching { AmarAiEngineBinding.market() }.getOrNull()?.let(::add)
            }
            if (listOf("مخاطر", "risk", "دقة", "precision", "ثقة").any { q.contains(it) }) {
                runCatching { AmarAiEngineBinding.riskGate() }.getOrNull()?.let(::add)
            }
        }.distinct()
        val contextText = request + if (evidence.isEmpty()) "" else "\n\nLOCAL_ENGINE_EVIDENCE:\n" + evidence.joinToString("\n")
        val local = runCatching {
            mesh.reasoning.respond(
                AmarAgentContext(
                    userText = contextText,
                    tools = emptyList(),
                    executionAllowed = false,
                    brokerAccessAllowed = false
                )
            )
        }.getOrNull()
        return if (local != null) {
            Response(local.answer, "AMAR_LOCAL", evidence, mesh.connectedEngineIds())
        } else {
            Response("AMAR AI: المحرك المحلي غير متاح حالياً. تم الإيقاف بأمان دون مزود خارجي.", "AMAR_LOCAL_FAIL_CLOSED", evidence, mesh.connectedEngineIds())
        }
    }
}
