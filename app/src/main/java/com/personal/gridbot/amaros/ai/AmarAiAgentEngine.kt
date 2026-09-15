package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.ai.core.AmarAiApprovalLedger
import com.personal.gridbot.amaros.agent.AmarAgentContext
import com.personal.gridbot.amaros.agent.AmarLocalReasoning
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingIntelligenceRegistry
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingKnowledgeLibrary
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingPrecisionEngine
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingSourceMesh
import org.json.JSONObject

/**
 * AMAR AI Supervisor. Provider-neutral by construction: no Gemini client, key store,
 * network LLM or external synthesis adapter exists on this authority path.
 * The supervisor orchestrates deterministic AMAR engines and local reasoning only.
 */
class AmarAiAgentEngine(
    private val context: Context? = null,
    private val research: AmarAiExternalResearch = AmarAiExternalResearch(),
    private val mesh: AmarAiEngineMesh = AmarAiEngineMesh()
) {
    data class Result(val answer: String, val proposedActions: List<String>, val toolEvidence: List<String>)

    suspend fun ask(apiKey: String, model: String, request: String): Result {
        val command = AmarAiActionEngine.route(request)
        if (command.handled) return Result(command.response, emptyList(), listOf("APP_COMMAND|LOCAL_AUTHORITY"))

        val actions = selectActions(request)
        val evidence = actions.mapNotNull { executeTool(it.first, it.second) }.distinct()
        val prompt = buildPrompt(request, evidence)
        val local = runCatching {
            mesh.reasoning.respond(
                AmarAgentContext(
                    userText = prompt,
                    tools = AmarAiToolRegistry.all().map { it.name },
                    executionAllowed = false,
                    brokerAccessAllowed = false
                )
            )
        }.getOrNull()

        val answer = local?.answer?.takeIf { it.isNotBlank() }
            ?: if (evidence.isNotEmpty()) evidence.joinToString("\n") else "AMAR AI: لا توجد أدلة محلية كافية لإجابة مؤكدة. تم الإغلاق الآمن."
        return Result(
            answer = if (evidence.isEmpty()) answer else "$answer\n\n${evidence.joinToString("\n")}",
            proposedActions = actions.map { "${it.first}: ${it.second}" },
            toolEvidence = evidence
        )
    }

    private fun selectActions(request: String): List<Pair<String, String>> {
        val q = request.lowercase()
        val out = mutableListOf<Pair<String, String>>()
        if (listOf("سوق", "market", "xau", "gold", "ذهب", "تحليل").any(q::contains)) out += "analyze_market" to ""
        if (listOf("مخاطر", "risk", "دقة", "precision", "ثقة").any(q::contains)) out += "precision_audit" to ""
        if (listOf("استراتيجية", "strategy", "اختبار", "backtest", "باك").any(q::contains)) out += "strategy_quality" to request
        if (listOf("مصادر", "بحث", "research", "ويب", "مصدر").any(q::contains)) out += "multi_source_research" to request
        if (listOf("ذاكرة", "memory", "معرفة", "knowledge").any(q::contains)) out += "library_search" to request
        if (listOf("عدم اليقين", "uncertainty", "drift", "انحراف").any(q::contains)) out += "uncertainty_audit" to "0,0.5,100,20,0.1"
        if (listOf("تحسين", "improve", "audit", "تدقيق").any(q::contains)) out += "self_audit" to ""
        if (listOf("التطبيق", "app", "حالة التطبيق").any(q::contains)) out += "inspect_app" to ""
        if (listOf("تتبع", "tracking", "صفقات").any(q::contains)) out += "tracking" to ""
        return out.distinctBy { it.first }
    }

    private fun buildPrompt(request: String, evidence: List<String>): String {
        val snapshot = AmarAiEngineMesh().snapshot()
        return """
AMAR AI SUPERVISOR
USER_REQUEST=$request
PROVIDER=AMAR_LOCAL_ONLY
EXECUTION_AUTHORITY=false
BROKER_EXECUTION=false
ENGINE_MESH=${mesh.connectedEngineIds().joinToString(",")}
MARKET_ENGINE=${snapshot.marketEngine}
INTELLIGENCE_ENGINE_COUNT=${snapshot.intelligenceEngineCount}
KNOWLEDGE_DOMAINS=${AmarTradingKnowledgeLibrary.domains.size}
TOOLS=${AmarAiToolRegistry.all().joinToString(",") { it.name }}
EVIDENCE:
${evidence.joinToString("\n")}
RULES: no invented facts; distinguish evidence/inference; fail closed on missing data; draft-only proposals; human approval required for sensitive actions.
""".trimIndent()
    }

    private suspend fun executeTool(tool: String, args: String): String? = when (tool) {
        "inspect_app", "engine_market", "tracking", "candle" -> AmarAiDeterministicToolGateway.execute(tool, args)
        "analyze_market" -> AmarAiEngineBinding.market()
        "precision_audit" -> {
            val gate = AmarTradingPrecisionEngine.defaultResearchGate()
            val result = AmarTradingPrecisionEngine.evaluate(
                AmarTradingPrecisionEngine.Input(gate.scorePct / 100.0, gate.confidencePct / 100.0, gate.scorePct / 100.0, .25, .40, .20, .35, gate.uncertaintyPct / 100.0)
            )
            "PRECISION|score=${"%.1f".format(result.scorePct)}|confidence=${"%.1f".format(result.confidencePct)}|uncertainty=${"%.1f".format(result.uncertaintyPct)}|gate=${result.gate}|reasons=${result.reasons.joinToString(" || ")}"
        }
        "strategy_quality" -> {
            val a = AmarStrategyQualityEngine.audit(args)
            "STRATEGY_QUALITY|level=${a.level}/7|score=${"%.1f".format(a.scorePct)}|verdict=${a.verdict}|missing=${a.missing.joinToString(",")}|redFlags=${a.redFlags.joinToString(",")}"
        }
        "validate_results" -> {
            val values = args.split(',', '|', ';', ' ').mapNotNull { it.toDoubleOrNull() }
            if (values.size < 2) "VALIDATION|ERROR=need_2_R_values" else AmarAiEngineBinding.validate(values)
        }
        "library_search" -> {
            val q = args.lowercase()
            val hits = AmarTradingIntelligenceRegistry.intelligenceEngines.filter { q.isBlank() || it.lowercase().contains(q) }
            "INTELLIGENCE_LIBRARY|${hits.joinToString(" | ")}"
        }
        "multi_source_research" -> runCatching { AmarTradingSourceMesh.research(context, args, research, 12) }
            .map { r -> "MULTI_SOURCE|query=${r.query}|evidence=${r.evidence.size}|supporting=${r.supportingChannels}|conflicts=${r.conflictChannels}|independent=${r.independentChannels}|authority=${r.authorityGrade}|confidence=${"%.1f".format(r.authorityConfidencePct)}|consensus=${"%.1f".format(r.consensusPct)}|caveat=${r.caveat}" }
            .getOrElse { "MULTI_SOURCE|FAILED=${it.message ?: "unknown"}" }
        "research_external" -> runCatching { research.search(args, 8) }
            .map { results -> results.joinToString("\n") { "SOURCE|${it.source}|${it.title}|${it.url}|${it.excerpt}" }.ifBlank { "EXTERNAL_RESEARCH|NO_PUBLIC_RESULTS" } }
            .getOrElse { "EXTERNAL_RESEARCH|FAILED=${it.message ?: "unknown"}" }
        "self_audit" -> {
            val c = context ?: return "SELF_AUDIT|ERROR=no_context"
            val audit = AmarAiSelfImprovementEngine(c).audit()
            "SELF_AUDIT|score=${"%.1f".format(audit.score)}/10|proposals=${audit.proposals.size}|warnings=${audit.warnings.joinToString(" || ")}"
        }
        "approval_proposal" -> {
            val c = context ?: return "APPROVAL_PROPOSAL|ERROR=no_context"
            val p = AmarAiApprovalLedger(c).propose(args)
            "APPROVAL_PROPOSAL|id=${p.id}|status=${p.status}|fingerprint=${p.fingerprint}|HUMAN_APPROVAL_REQUIRED"
        }
        else -> AmarAiDeterministicToolGateway.execute(tool, args)
    }
}
