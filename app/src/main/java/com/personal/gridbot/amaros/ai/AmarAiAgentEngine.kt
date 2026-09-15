package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.ai.core.AmarAiApprovalLedger
import com.personal.gridbot.amaros.agent.AmarAgentContext
import com.personal.gridbot.amaros.agent.AmarLocalReasoning
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingIntelligenceRegistry
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingKnowledgeLibrary
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingPrecisionEngine
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingSourceMesh

/** AMAR AI supervisor: Arabic-native, multilingual, code-capable, and GitHub-aware. */
class AmarAiAgentEngine(
    private val context: Context? = null,
    private val research: AmarAiExternalResearch = AmarAiExternalResearch(),
    private val mesh: AmarAiEngineMesh = AmarAiEngineMesh()
) {
    data class Result(val answer: String, val proposedActions: List<String>, val toolEvidence: List<String>)

    suspend fun ask(apiKey: String, model: String, request: String): Result {
        val command = AmarAiActionEngine.route(request)
        if (command.handled) return Result(command.response, emptyList(), listOf("APP_COMMAND|LOCAL_AUTHORITY"))

        val codePlan = if (looksLikeCode(request)) AmarAiCodeAnalysisEngine.plan(request) else null
        val actions = selectActions(request)
        val evidence = actions.mapNotNull { executeTool(it.first, it.second) }.distinct()
        val prompt = buildPrompt(request, evidence, codePlan, isCodeGenerationRequest(request))
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
            ?: if (codePlan != null) codePlan.toArabicReport()
            else if (evidence.isNotEmpty()) evidence.joinToString("\n")
            else "AMAR AI: لا توجد أدلة محلية كافية لإجابة مؤكدة. تم الإغلاق الآمن."
        return Result(
            answer = if (evidence.isEmpty()) answer else "$answer\n\n${evidence.joinToString("\n")}",
            proposedActions = actions.map { "${it.first}: ${it.second}" },
            toolEvidence = evidence
        )
    }

    private fun looksLikeCode(request: String): Boolean {
        val q = request.lowercase()
        return listOf("//@version=", "<html", "<!doctype", "fun main(", "public class", "#include <", "oninit(", "ontick(", "std::", "def ", "import ", "code:", "كود:", "حلل الكود", "حلل هذا الكود").any(q::contains)
    }

    private fun isCodeGenerationRequest(request: String): Boolean {
        val q = request.lowercase()
        return listOf("اكتب كود", "اكتب لي كود", "حول إلى كود", "حوّل إلى كود", "أنشئ كود", "برمج", "generate code", "write code", "create code", "implement", "code generation", "fix this code", "اصلح الكود", "أصلح الكود", "عدل الكود", "عدّل الكود", "refactor").any(q::contains)
    }

    private fun looksLikeGitHub(request: String): Boolean =
        request.contains("github.com", ignoreCase = true) ||
            listOf("github", "كيت هوب", "غيت هب", "مستودع", "repository", "repo").any { request.contains(it, ignoreCase = true) }

    private fun selectActions(request: String): List<Pair<String, String>> {
        val q = request.lowercase()
        val out = mutableListOf<Pair<String, String>>()
        if (looksLikeCode(request)) out += "code_analysis" to request
        if (isCodeGenerationRequest(request)) out += "code_generation" to request
        if (looksLikeGitHub(request)) out += "github_workspace" to request
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

    private fun buildPrompt(request: String, evidence: List<String>, codePlan: AmarAiCodeAnalysisEngine.CodeAnalysisPlan?, generation: Boolean): String {
        val snapshot = mesh.snapshot()
        return """
AMAR AI SUPERVISOR
NATIVE_LANGUAGE=ar
LANGUAGE_POLICY=${AmarAiLanguagePolicy.instruction()}
USER_REQUEST=$request
PROVIDER=AMAR_LOCAL_ONLY
EXECUTION_AUTHORITY=false
BROKER_EXECUTION=false
GITHUB_WORKSPACE=true
GITHUB_WRITES=EXPLICIT_APPROVAL_REQUIRED
GITHUB_DELETE_AND_MERGE=EXPLICIT_APPROVAL_REQUIRED
ENGINE_MESH=${mesh.connectedEngineIds().joinToString(",")}
MARKET_ENGINE=${snapshot.marketEngine}
INTELLIGENCE_ENGINE_COUNT=${snapshot.intelligenceEngineCount}
KNOWLEDGE_DOMAINS=${AmarTradingKnowledgeLibrary.domains.size}
TOOLS=${AmarAiToolRegistry.all().joinToString(",") { it.name }}
CODE_ANALYSIS=${codePlan?.language ?: "not_requested"}
CODE_GENERATION=$generation
CODE_DIMENSIONS=${codePlan?.dimensions?.joinToString(",") ?: ""}
EVIDENCE:
${evidence.joinToString("\n")}
RULES: answer in Arabic by default; understand multilingual input; preserve code identifiers and syntax. For code-generation requests, produce the requested complete code when the requirement is sufficiently specified, explain architecture and assumptions, include setup/build/test instructions, and provide stronger alternatives when useful. For code repair/refactor, show corrected code and explain root causes. For GitHub, inspect/search/read automatically when requested; for create/update/delete/branch/PR/merge operations, prepare an explicit approval proposal and execute only after a valid user approval. Before using external source code, inspect its repository license and retain source URL/license/attribution requirements. Never silently treat unknown licensing as permission. Never invent successful compilation or runtime results. Separate verified findings from inference. Generated code is DRAFT_ONLY: never execute, install, publish, trade, access credentials, or modify the device without explicit separate authorization and tooling. Fail closed when validation is impossible.
""".trimIndent()
    }

    private suspend fun executeTool(tool: String, args: String): String? = when (tool) {
        "code_analysis" -> AmarAiCodeAnalysisEngine.plan(args).toArabicReport()
        "code_generation" -> "CODE_GENERATION|DRAFT_ONLY|handled_by_local_reasoning"
        "github_workspace" -> {
            val c = context ?: return "GITHUB|FAILED=no_context"
            val parsed = AmarAiGitHubIntentParser.parse(args) ?: return "GITHUB|FAILED=لم أفهم المستودع أو العملية المطلوبة"
            val gateway = AmarAiGitHubToolGateway(c)
            val operation = parsed.optString("operation")
            val readOperation = operation in setOf("repo_search", "repo_inspect", "code_search", "file_read", "license_inspect")
            val result = if (readOperation) gateway.inspect(parsed) else gateway.proposeWrite(parsed)
            "GITHUB|operation=$operation|ok=${result.ok}|status=${result.status}|message=${result.message}|data=${result.data.take(12000)}"
        }
        "inspect_app", "engine_market", "tracking", "candle" -> AmarAiDeterministicToolGateway.execute(tool, args)
        "analyze_market" -> AmarAiEngineBinding.market()
        "precision_audit" -> {
            val gate = AmarTradingPrecisionEngine.defaultResearchGate()
            val result = AmarTradingPrecisionEngine.evaluate(AmarTradingPrecisionEngine.Input(gate.scorePct / 100.0, gate.confidencePct / 100.0, gate.scorePct / 100.0, .25, .40, .20, .35, gate.uncertaintyPct / 100.0))
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
        "multi_source_research" -> runCatching { AmarTradingSourceMesh.research(context, args, research, 12) }.map { r -> "MULTI_SOURCE|query=${r.query}|evidence=${r.evidence.size}|supporting=${r.supportingChannels}|conflicts=${r.conflictChannels}|independent=${r.independentChannels}|authority=${r.authorityGrade}|confidence=${"%.1f".format(r.authorityConfidencePct)}|consensus=${"%.1f".format(r.consensusPct)}|caveat=${r.caveat}" }.getOrElse { "MULTI_SOURCE|FAILED=${it.message ?: "unknown"}" }
        "research_external" -> runCatching { research.search(args, 8) }.map { results -> results.joinToString("\n") { "SOURCE|${it.source}|${it.title}|${it.url}|${it.excerpt}" }.ifBlank { "EXTERNAL_RESEARCH|NO_PUBLIC_RESULTS" } }.getOrElse { "EXTERNAL_RESEARCH|FAILED=${it.message ?: "unknown"}" }
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

private fun AmarAiCodeAnalysisEngine.CodeAnalysisPlan.toArabicReport(): String =
    "تحليل الكود | اللغة المكتشفة: $language\n" +
        "المحاور: ${dimensions.joinToString("، ")}\n" +
        "الفحوص: ${checks.joinToString("؛ ")}\n" +
        "منهج التحسين: ${recommendations.joinToString("؛ ")}\n" +
        "ملاحظة: اكتشاف العيوب الدقيقة يتطلب محلل/مترجم اللغة وأدوات المشروع عند توفرها؛ لا يتم ادعاء نتيجة لم تُتحقق منها."
