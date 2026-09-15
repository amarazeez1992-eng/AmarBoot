package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.agent.AmarAgentContext
import com.personal.gridbot.amaros.agent.AmarLocalReasoning

/** Stable UI-to-AMAR boundary. The UI talks to AMAR local engines first; external providers are optional adapters. */
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
        val external = externalProviderEnabled && apiKey.isNotBlank() && model.isNotBlank()
        if (!external) {
            return askLocal(mesh, request)
        }

        val agent = AmarAiAgentEngine(
            context = context,
            gemini = externalProvider ?: AmarGeminiClient(),
            mesh = mesh
        )
        return runCatching {
            val result = agent.ask(apiKey, model, request)
            Response(
                answer = result.answer,
                provider = "AMAR+OPTIONAL_EXTERNAL_SYNTHESIS",
                evidence = result.toolEvidence,
                engineIds = mesh.connectedEngineIds()
            )
        }.getOrElse { failure ->
            val local = askLocal(mesh, request)
            local.copy(
                provider = "AMAR_LOCAL_FAIL_CLOSED",
                answer = local.answer + "\n\nحالة المزود الخارجي: DATA_UNAVAILABLE / fail-closed (${failure::class.simpleName})."
            )
        }
    }

    private suspend fun askLocal(mesh: AmarAiEngineMesh, request: String): Response {
        val q = request.lowercase()
        val evidence = buildList {
            if (listOf("سوق", "market", "xau", "gold", "ذهب", "تحليل").any { q.contains(it) }) {
                runCatching { AmarAiEngineBinding.market() }.getOrNull()?.let(::add)
            }
            if (listOf("مخاطر", "risk", "دقة", "precision", "ثقة").any { q.contains(it) }) {
                runCatching { AmarAiEngineBinding.riskGate() }.getOrNull()?.let(::add)
            }
        }.distinct()
        val contextText = request + if (evidence.isEmpty()) "" else "\n\nLOCAL_ENGINE_EVIDENCE:\n" + evidence.joinToString("\n")
        val local = runCatching {
            AmarLocalReasoning().respond(
                AmarAgentContext(
                    userText = contextText,
                    tools = emptyList(),
                    executionAllowed = false,
                    brokerAccessAllowed = false
                )
            )
        }.getOrNull()

        return if (local != null) {
            Response(
                answer = local.answer,
                provider = "AMAR_LOCAL",
                evidence = evidence,
                engineIds = mesh.connectedEngineIds()
            )
        } else {
            Response(
                answer = "AMAR AI: التحليل المحلي غير متاح حالياً. تم إيقاف التنفيذ بأمان دون تجاوز الصلاحيات.",
                provider = "AMAR_LOCAL_FAIL_CLOSED",
                evidence = evidence,
                engineIds = mesh.connectedEngineIds()
            )
        }
    }
}
