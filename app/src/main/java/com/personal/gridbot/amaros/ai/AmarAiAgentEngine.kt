package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.ai.core.AmarAiApprovalLedger
import com.personal.gridbot.amaros.ai.core.AmarAiControlCenter
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.intelligence.advanced.AmarDriftAndUncertaintyEngine
import com.personal.gridbot.amaros.intelligence.advanced.AmarStrategyEvolutionEngine
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingIntelligenceRegistry
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingKnowledgeLibrary
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingPrecisionEngine
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingSourceMesh
import org.json.JSONArray
import org.json.JSONObject

/** AI Supervisor: evidence-driven, precision-gated, approval-controlled and fail-closed. */
class AmarAiAgentEngine(
    private val context: Context? = null,
    private val gemini: AmarGeminiClient = AmarGeminiClient(),
    private val research: AmarAiExternalResearch = AmarAiExternalResearch()
) {
    data class Result(val answer: String, val proposedActions: List<String>, val toolEvidence: List<String>)
    private data class Plan(val answer: String, val actions: List<Pair<String, String>>)

    suspend fun ask(apiKey: String, model: String, request: String): Result {
        val first = gemini.generate(apiKey, model, systemPrompt(), buildPrompt(request))
        val plan = parsePlan(first.text)
        val evidence = plan.actions.mapNotNull { executeTool(it.first, it.second) }
        if (evidence.isEmpty()) return Result(plan.answer, plan.actions.map { "${it.first}: ${it.second}" }, emptyList())
        val finalPrompt = buildPrompt(request) + "\n\nEVIDENCE:\n" + evidence.joinToString("\n") +
            "\n\nFINAL REVIEW: separate SOURCE/VERIFIED/HYPOTHESIS/INFERENCE; detect leakage, overfit, repainting, costs, slippage, regime mismatch, drift and uncertainty; never invent results; finish with the exact approval step required from the user."
        val second = gemini.generate(apiKey, model, systemPrompt(), finalPrompt)
        val finalPlan = parsePlan(second.text)
        return Result(finalPlan.answer, finalPlan.actions.map { "${it.first}: ${it.second}" }, evidence)
    }

    private fun buildPrompt(request: String): String {
        val s = AmarMarketStateStore.snapshot
        val market = JSONObject()
            .put("symbol", s.symbol).put("timeframe", s.timeframe.name).put("bid", s.bid).put("ask", s.ask)
            .put("spread", s.spread).put("direction", s.direction.name).put("strength", s.strength)
            .put("candleOpen", s.candleOpen).put("candleHigh", s.candleHigh).put("candleLow", s.candleLow).put("candleClose", s.candleClose)
            .put("session", s.session).put("source", s.source.name).put("quality", s.quality.name)
        val control = context?.let { AmarAiControlCenter(it) }
        val authority = JSONObject()
            .put("aiEnabled", control?.aiEnabled ?: false)
            .put("emergencyStopped", control?.emergencyStopped ?: true)
            .put("executionAuthorized", control?.executionAuthorized ?: false)
            .put("brokerExecution", "DISABLED")
        val contextJson = JSONObject()
            .put("market", market)
            .put("aiAuthority", authority)
            .put("capabilities", AmarTradingIntelligenceRegistry.intelligenceEngines.joinToString(", "))
            .put("knowledgeDomains", AmarTradingKnowledgeLibrary.domains.size)
            .put("knowledgeGovernance", AmarTradingKnowledgeLibrary.governance())
            .put("sourceMesh", AmarTradingSourceMesh.sourceCatalogText())
            .put("openSourceBots", AmarTradingSourceMesh.botCatalogText())
            .put("researchCatalog", AmarTradingIntelligenceRegistry.catalogText())
            .put("researchGovernance", AmarTradingIntelligenceRegistry.governance())
            .put("researchHierarchy", AmarTradingResearchRegistry.catalogText())
            .put("researchProtocol", AmarTradingResearchRegistry.protocol())
            .put("executionPolicy", "CURRENT BUILD: advisory/local strategy management; broker execution transport remains disabled until MT5 bridge phase")
            .put("authority", "User is final decision maker. Never silently change an approved strategy.")
            .put("approvalBoundary", "AI can create DRAFT/CHALLENGER proposals. Only explicit human approval may promote a strategy to APPROVED.")
            .put("precisionPolicy", "Precision means evidence quality, validation and uncertainty control, never promised profitability.")
        return "USER REQUEST:\n$request\n\nAPP CONTEXT:\n$contextJson"
    }

    private fun systemPrompt() = """
You are AMAR AI Supervisor inside AmarBoot.
Act as a senior global trading research, strategy engineering, quantitative validation, risk and decision-intelligence system with a deep internal trading knowledge library.
For important research, do not stop at the first source. Use multi-source research and independent evidence aggregation.
Search official/primary sources, open-source code repositories, research frameworks, TradingView/Pine ecosystems and established trading-engineering projects when relevant.
Never invent market data, broker state, source claims, backtest statistics or execution results.
Use SOURCE for external material, VERIFIED for measured reproducible evidence, HYPOTHESIS for untested ideas, and INFERENCE for reasoning.
Challenge look-ahead leakage, repainting, overfitting, data snooping, hidden exposure, unrealistic fills, spread/slippage omission, regime mismatch, distribution drift and undefined failure modes.
TradingView public scripts are discovery inputs, not automatically reusable. Protected, invite-only, paid or credentialed proprietary content is never copied.
GitHub is a discovery and code-evidence source. Inspect repository provenance and license before recommending reuse.
LuxAlgo is a reference/engineering ecosystem, never proof of profitability. Respect Vela/PineTS and addon licenses.
Never convert source convergence into a fabricated profitability percentage.
A numerical success claim requires reproducible AMAR backtest/OOS/stress evidence with dataset, sample size, assumptions and costs.
Strategy lifecycle: Idea -> Draft -> Discuss -> Evaluate -> Test -> OOS -> Stress -> Compare -> Risk Gate -> User Approval -> Adopt. No autonomous adoption.
AI may save DRAFT/CHALLENGER notes only. It may never call an approval operation or silently promote a strategy.
Current broker execution is disabled; never claim an order was sent, opened, closed or modified.
Emergency stop outranks all AI authority.
Return valid JSON: {"answer":"Arabic answer","actions":[{"tool":"trading_library_search|library_search|analyze_market|research_external|multi_source_research|bot_discovery|strategy_quality|test_strategy|validate_results|inspect_bot|strategy_save|strategy_load|precision_audit|evolution_gate|champion_challenger|counterfactual|uncertainty_audit|approval_proposal","args":"short description"}],"approvalRequired":true}
""".trimIndent()

    private suspend fun executeTool(tool: String, args: String): String? {
        return when (tool) {
            "trading_library_search" -> {
                val hits = AmarTradingKnowledgeLibrary.search(args, 12)
                if (hits.isEmpty()) "trading_library_search($args) => NO_INTERNAL_DOMAIN_MATCH"
                else "trading_library_search($args) => " + hits.joinToString(" | ") { "${it.name}: ${it.concepts.joinToString(", ")}" }
            }
            "library_search" -> {
                val catalog = AmarTradingIntelligenceRegistry.intelligenceEngines
                val q = args.lowercase()
                val matches = catalog.filter { q.isBlank() || it.lowercase().contains(q) }
                "library_search($args) => ${if (matches.isEmpty()) catalog.joinToString(" | ") else matches.joinToString(" | ")}"
            }
            "analyze_market" -> {
                val s = AmarMarketStateStore.snapshot
                "analyze_market => symbol=${s.symbol}, timeframe=${s.timeframe}, bid=${s.bid}, ask=${s.ask}, spread=${s.spread}, direction=${s.direction}, strength=${s.strength}, source=${s.source}, quality=${s.quality}"
            }
            "research_external" -> {
                val plan = AmarTradingIntelligenceRegistry.recommendedResearchPlan(args)
                val results = runCatching { research.search(args, 6) }.getOrElse { return "research_external => $plan\nFETCH_FAILED=${it.message ?: "unknown error"}" }
                if (results.isEmpty()) "$plan\nNO_PUBLIC_RESULTS" else "$plan\n" + results.joinToString("\n") { "EXTERNAL|${it.source}|${it.title}|${it.url}|${it.excerpt}" }
            }
            "multi_source_research" -> {
                val report = runCatching { AmarTradingSourceMesh.research(context, args, research, 12) }
                    .getOrElse { return "multi_source_research => FAILED=${it.message ?: "unknown error"}" }
                val fresh = if (report.newItems.isEmpty()) "none" else report.newItems.take(8).joinToString(" | ") { it.title }
                "MULTI_SOURCE|query=${report.query}|channels=${report.searchedChannels}|evidence=${report.evidence.size}|supportingChannels=${report.supportingChannels}|conflictChannels=${report.conflictChannels}|convergence=${"%.1f".format(report.consensusPct)}%|authority=${report.authorityGrade}|authorityConfidence=${"%.1f".format(report.authorityConfidencePct)}%|new=$fresh|caveat=${report.caveat}\n" + report.evidence.take(30).joinToString("\n") { "SOURCE|${it.source}|${it.title}|${it.url}|${it.excerpt}" }
            }
            "bot_discovery" -> {
                val q = args.lowercase()
                val internal = AmarTradingSourceMesh.bots.filter { q.isBlank() || (it.name + " " + it.focus).lowercase().contains(q) }
                val report = runCatching { AmarTradingSourceMesh.research(context, "$args open source trading bot", research, 10) }.getOrNull()
                "BOT_DISCOVERY|internal=${internal.joinToString(" | ") { "${it.name}:${it.repo}:${it.url}" }}|externalEvidence=${report?.evidence?.size ?: 0}|license=CHECK_BEFORE_REUSE"
            }
            "strategy_quality" -> {
                val audit = AmarStrategyQualityEngine.audit(args)
                "strategy_quality => level=${audit.level}/7 score=${"%.1f".format(audit.scorePct)}% verdict=${audit.verdict} strengths=${audit.strengths.joinToString(",")} missing=${audit.missing.joinToString(",")} redFlags=${audit.redFlags.joinToString(",")}"
            }
            "test_strategy" -> "test_strategy => measured candles/signals are required; descriptions alone cannot produce profitability results."
            "validate_results" -> {
                val values = args.split(',', ';', ' ', '\n').mapNotNull { it.trim().toDoubleOrNull() }
                if (values.size < 2) "validate_results => insufficient measured trade R values"
                else {
                    val r = AmarStrategyValidationEngine.analyze(values)
                    "validate_results => n=${r.sampleSize} winRate=${"%.2f".format(r.winRatePct)}% PF=${"%.3f".format(r.profitFactor)} expectancyR=${"%.4f".format(r.expectancyR)} maxDD=${"%.3f".format(r.maxDrawdownR)} recovery=${"%.3f".format(r.recoveryFactor)} SQN=${"%.3f".format(r.sqn)} verified=${r.verified}"
                }
            }
            "precision_audit" -> {
                val p = args.split(',', ';', ' ', '\n').mapNotNull { it.trim().toDoubleOrNull() }
                val input = if (p.size >= 7) AmarTradingPrecisionEngine.Input(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p.getOrElse(7) { 0.0 })
                else AmarTradingPrecisionEngine.defaultResearchGate().let { AmarTradingPrecisionEngine.Input(it.scorePct / 100.0, it.confidencePct / 100.0, it.scorePct / 100.0, 0.25, 0.40, 0.20, 0.35, it.uncertaintyPct / 100.0) }
                val r = AmarTradingPrecisionEngine.evaluate(input)
                "precision_audit => score=${"%.1f".format(r.scorePct)}% confidence=${"%.1f".format(r.confidencePct)}% uncertainty=${"%.1f".format(r.uncertaintyPct)}% gate=${r.gate} reasons=${r.reasons.joinToString(" | ")}"
            }
            "evolution_gate" -> {
                val p = args.split(',', ';').mapNotNull { it.trim().toDoubleOrNull() }
                if (p.size < 7) "evolution_gate => expected expectancy,dd,pf,sample,oos,stress,leakage(0/1)"
                else {
                    val candidate = AmarStrategyEvolutionEngine.mutate("AI-CANDIDATE", "user-requested governed mutation", AmarStrategyEvolutionEngine.MutationType.ENTRY)
                    val score = AmarStrategyEvolutionEngine.Score(p[0], p[1], p[2], p[3].toInt(), p[4], p[5], p[6] > 0.5, 0.0)
                    val d = AmarStrategyEvolutionEngine.gate(candidate, score)
                    "evolution_gate => gate=${d.gate} score=${"%.3f".format(d.score)} reasons=${d.reasons.joinToString(" | ")}"
                }
            }
            "champion_challenger" -> "champion_challenger => compare measured champion/challenger OOS, stress, PF and DD; no automatic adoption."
            "counterfactual" -> "counterfactual => alternative-vs-base deltas require measured score vectors; no invented outcome."
            "uncertainty_audit" -> {
                val p = args.split(',', ';').mapNotNull { it.trim().toDoubleOrNull() }
                if (p.size < 5) "uncertainty_audit => expected sample,expectancyStdev,oosWindows,failedWindows,drift"
                else {
                    val r = AmarDriftAndUncertaintyEngine.uncertainty(p[0].toInt(), p[1], p[2].toInt(), p[3].toInt(), p[4])
                    "uncertainty_audit => uncertainty=${"%.1f".format(r.uncertaintyPct)}% confidence=${"%.1f".format(r.confidencePct)}% reasons=${r.reasons.joinToString(" | ")}"
                }
            }
            "approval_proposal" -> {
                val c = context ?: return "approval_proposal => unavailable: no Android context"
                val proposal = AmarAiApprovalLedger(c).propose(args)
                "approval_proposal => id=${proposal.id}|status=${proposal.status}|fingerprint=${proposal.fingerprint}|HUMAN_APPROVAL_REQUIRED"
            }
            "inspect_bot" -> "inspect_bot => BOT Lab boundary available; no runtime mutation performed."
            "strategy_save" -> {
                val repo = context?.let { AmarAiStrategyNotesRepository(it) } ?: return "strategy_save => unavailable: no Android context"
                val parts = args.split("::", limit = 2)
                val saved = repo.save(parts.firstOrNull()?.trim().orEmpty(), parts.getOrNull(1)?.trim().orEmpty(), "DRAFT")
                "strategy_save => ${saved?.name ?: "rejected"} version=${saved?.version ?: 0} status=${saved?.status ?: "NOT_SAVED"}"
            }
            "strategy_load" -> {
                val repo = context?.let { AmarAiStrategyNotesRepository(it) } ?: return "strategy_load => unavailable: no Android context"
                val note = repo.find(args)
                "strategy_load($args) => ${note?.content ?: "NOT_FOUND"}"
            }
            else -> null
        }
    }

    private fun parsePlan(raw: String): Plan {
        val cleaned = raw.trim().removePrefix("```").removeSuffix("```").trim()
        return runCatching {
            val json = JSONObject(cleaned)
            val actions = mutableListOf<Pair<String, String>>()
            val array = json.optJSONArray("actions") ?: JSONArray()
            for (i in 0 until array.length()) {
                val a = array.optJSONObject(i) ?: continue
                actions += a.optString("tool", "proposal") to a.optString("args", "")
            }
            Plan(json.optString("answer", cleaned), actions)
        }.getOrElse { Plan(cleaned, emptyList()) }
    }
}
