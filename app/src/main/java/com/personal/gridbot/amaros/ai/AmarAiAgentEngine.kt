package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.ai.core.AmarAiApprovalLedger
import com.personal.gridbot.amaros.ai.core.AmarAiControlCenter
import com.personal.gridbot.amaros.ai.core.AmarAiMt5Office
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.intelligence.advanced.AmarDriftAndUncertaintyEngine
import com.personal.gridbot.amaros.intelligence.advanced.AmarStrategyEvolutionEngine
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingIntelligenceRegistry
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingKnowledgeLibrary
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingPrecisionEngine
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingSourceMesh
import org.json.JSONArray
import org.json.JSONObject

/**
 * AMAR AI Supervisor.
 * Every declared tool below either executes deterministic local logic or performs
 * explicitly labelled external research. No tool is allowed to claim broker execution.
 */
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
        val finalPrompt = buildPrompt(request) + "\n\nEXECUTED TOOL EVIDENCE:\n" + evidence.joinToString("\n") +
            "\n\nFINAL REVIEW: distinguish SOURCE/VERIFIED/HYPOTHESIS/INFERENCE; report measured metrics only; expose leakage, overfit, repainting, costs, slippage, regime mismatch, drift and uncertainty; finish with the exact human approval boundary."
        val second = gemini.generate(apiKey, model, systemPrompt(), finalPrompt)
        val finalPlan = parsePlan(second.text)
        return Result(finalPlan.answer, finalPlan.actions.map { "${it.first}: ${it.second}" }, evidence)
    }

    private fun buildPrompt(request: String): String {
        val s = AmarMarketStateStore.snapshot
        val control = context?.let { AmarAiControlCenter(it) }
        val market = JSONObject()
            .put("symbol", s.symbol).put("timeframe", s.timeframe.name)
            .put("bid", s.bid).put("ask", s.ask).put("spread", s.spread)
            .put("direction", s.direction.name).put("strength", s.strength)
            .put("candleOpen", s.candleOpen).put("candleHigh", s.candleHigh)
            .put("candleLow", s.candleLow).put("candleClose", s.candleClose)
            .put("session", s.session).put("source", s.source.name).put("quality", s.quality.name)
        val authority = JSONObject()
            .put("aiEnabled", control?.aiEnabled ?: false)
            .put("emergencyStopped", control?.emergencyStopped ?: true)
            .put("aiAuthorityEnabled", control?.aiAuthorityEnabled ?: false)
            .put("executionAuthorized", control?.executionAuthorized ?: false)
            .put("brokerExecutionAuthorized", control?.brokerExecutionAuthorized ?: false)
        val app = JSONObject()
            .put("market", market)
            .put("aiAuthority", authority)
            .put("capabilities", AmarTradingIntelligenceRegistry.intelligenceEngines.joinToString(", "))
            .put("knowledgeDomains", AmarTradingKnowledgeLibrary.domains.size)
            .put("knowledgeGovernance", AmarTradingKnowledgeLibrary.governance())
            .put("sourceMesh", AmarTradingSourceMesh.sourceCatalogText())
            .put("openSourceBots", AmarTradingSourceMesh.botCatalogText())
            .put("mt5Office", AmarAiMt5Office.catalogText())
            .put("researchCatalog", AmarTradingIntelligenceRegistry.catalogText())
            .put("researchGovernance", AmarTradingIntelligenceRegistry.governance())
            .put("executionPolicy", "BROKER EXECUTION DISABLED; only deterministic planning/research/validation is executable in this build")
            .put("approvalBoundary", "AI can create DRAFT/CHALLENGER proposals. Only explicit human approval may promote to APPROVED.")
        return "USER REQUEST:\n$request\n\nAPP CONTEXT:\n$app"
    }

    private fun systemPrompt() = """
You are AMAR AI Supervisor inside AmarBoot.
Operate as a senior quantitative research, strategy engineering, risk, validation and decision-intelligence system.
Use tools for real local computation whenever measured inputs exist. Do not replace computation with prose.
Never invent market data, backtests, broker state, source claims or execution results.
Use SOURCE for external material, VERIFIED only for reproducible measured evidence, HYPOTHESIS for untested ideas, and INFERENCE for reasoning.
Challenge look-ahead leakage, repainting, overfitting, data snooping, hidden exposure, unrealistic fills, missing costs, regime mismatch and distribution drift.
TradingView/GitHub/open-source discovery is not permission to copy protected or incompatible code; inspect provenance and license.
Research convergence is evidence quality, never a probability of profit.
Strategy lifecycle: Idea -> Draft -> Discuss -> Evaluate -> Test -> OOS -> Stress -> Compare -> Risk Gate -> User Approval -> Adopt.
No autonomous adoption. strategy_save always creates DRAFT. approval_proposal only records a human-review proposal.
Current broker execution is disabled. Never claim an order was sent, opened, closed or modified.
Emergency stop outranks all AI authority.
When a tool needs structured input, use its documented compact format. Do not invent missing measurements.
Return JSON: {"answer":"Arabic answer","actions":[{"tool":"...","args":"..."}],"approvalRequired":true}
""".trimIndent()

    private suspend fun executeTool(tool: String, args: String): String? = when (tool) {
        "trading_library_search" -> {
            val hits = AmarTradingKnowledgeLibrary.search(args, 12)
            "LIBRARY|query=$args|hits=" + (if (hits.isEmpty()) "NONE" else hits.joinToString(" || ") { "${it.name}:${it.concepts.joinToString(",")}" })
        }
        "library_search" -> {
            val q = args.lowercase()
            val hits = AmarTradingIntelligenceRegistry.intelligenceEngines.filter { q.isBlank() || it.lowercase().contains(q) }
            "INTELLIGENCE_LIBRARY|query=$args|hits=${hits.joinToString(" | ") }"
        }
        "analyze_market" -> {
            val s = AmarMarketStateStore.snapshot
            "MARKET|symbol=${s.symbol}|timeframe=${s.timeframe}|bid=${s.bid}|ask=${s.ask}|spread=${s.spread}|direction=${s.direction}|strength=${s.strength}|source=${s.source}|quality=${s.quality}"
        }
        "research_external" -> {
            val plan = AmarTradingIntelligenceRegistry.recommendedResearchPlan(args)
            val results = runCatching { research.search(args, 8) }.getOrElse { return "EXTERNAL_RESEARCH|FAILED=${it.message ?: "unknown"}\n$plan" }
            "$plan\n" + if (results.isEmpty()) "EXTERNAL_RESEARCH|NO_PUBLIC_RESULTS" else results.joinToString("\n") { "SOURCE|${it.source}|${it.title}|${it.url}|${it.excerpt}" }
        }
        "multi_source_research" -> {
            val report = runCatching { AmarTradingSourceMesh.research(context, args, research, 12) }
                .getOrElse { return "MULTI_SOURCE|FAILED=${it.message ?: "unknown"}" }
            "MULTI_SOURCE|query=${report.query}|channels=${report.searchedChannels}|evidence=${report.evidence.size}|supporting=${report.supportingChannels}|conflicts=${report.conflictChannels}|convergence=${"%.1f".format(report.consensusPct)}|authority=${report.authorityGrade}|authorityConfidence=${"%.1f".format(report.authorityConfidencePct)}|newItems=${report.newItems.size}\n" + report.evidence.take(30).joinToString("\n") { "SOURCE|${it.source}|${it.title}|${it.url}|${it.excerpt}" }
        }
        "bot_discovery" -> {
            val q = args.lowercase()
            val internal = AmarTradingSourceMesh.bots.filter { q.isBlank() || (it.name + " " + it.focus).lowercase().contains(q) }
            val external = runCatching { AmarTradingSourceMesh.research(context, "$args open source trading bot", research, 10) }.getOrNull()
            "BOT_DISCOVERY|internal=${internal.joinToString(" || ") { "${it.name}:${it.repo}:${it.url}" }}|externalEvidence=${external?.evidence?.size ?: 0}|licenseGate=REQUIRED"
        }
        "strategy_quality" -> {
            val a = AmarStrategyQualityEngine.audit(args)
            "STRATEGY_QUALITY|level=${a.level}/7|score=${"%.1f".format(a.scorePct)}|verdict=${a.verdict}|missing=${a.missing.joinToString(",")}|redFlags=${a.redFlags.joinToString(",")}"
        }
        "test_strategy" -> executeBacktest(args)
        "validate_results" -> {
            val values = parseDoubles(args)
            if (values.size < 2) "VALIDATION|ERROR=need_at_least_2_trade_R_values"
            else { val r = AmarStrategyValidationEngine.analyze(values); "VALIDATION|n=${r.sampleSize}|winRate=${"%.2f".format(r.winRatePct)}|PF=${"%.3f".format(r.profitFactor)}|expectancyR=${"%.4f".format(r.expectancyR)}|maxDD=${"%.3f".format(r.maxDrawdownR)}|recovery=${"%.3f".format(r.recoveryFactor)}|SQN=${"%.3f".format(r.sqn)}|verified=${r.verified}" }
        }
        "precision_audit" -> {
            val p = parseDoubles(args)
            val input = if (p.size >= 7) AmarTradingPrecisionEngine.Input(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p.getOrElse(7) { 0.0 }) else AmarTradingPrecisionEngine.defaultResearchGate().let { AmarTradingPrecisionEngine.Input(it.scorePct / 100.0, it.confidencePct / 100.0, it.scorePct / 100.0, .25, .40, .20, .35, it.uncertaintyPct / 100.0) }
            val r = AmarTradingPrecisionEngine.evaluate(input)
            "PRECISION|score=${"%.1f".format(r.scorePct)}|confidence=${"%.1f".format(r.confidencePct)}|uncertainty=${"%.1f".format(r.uncertaintyPct)}|gate=${r.gate}|reasons=${r.reasons.joinToString(" || ")}"
        }
        "evolution_gate" -> evolutionGate(args)
        "champion_challenger" -> compareScores(args)
        "counterfactual" -> counterfactual(args)
        "uncertainty_audit" -> uncertainty(args)
        "approval_proposal" -> {
            val c = context ?: return "APPROVAL_PROPOSAL|ERROR=no_context"
            val p = AmarAiApprovalLedger(c).propose(args)
            "APPROVAL_PROPOSAL|id=${p.id}|status=${p.status}|fingerprint=${p.fingerprint}|HUMAN_APPROVAL_REQUIRED"
        }
        "inspect_bot" -> {
            val hits = AmarAiMt5Office.search(args)
            "BOT_INSPECTION|query=$args|matches=${hits.joinToString(" || ") { "${it.id}:${it.name}:${it.type}:${it.path}:mutable=${it.mutable}:role=${it.role}" }}"
        }
        "strategy_save" -> {
            val c = context ?: return "STRATEGY_SAVE|ERROR=no_context"
            val parts = args.split("::", limit = 2)
            val repo = AmarAiStrategyNotesRepository(c)
            val saved = repo.save(parts.firstOrNull()?.trim().orEmpty(), parts.getOrNull(1)?.trim().orEmpty(), "DRAFT")
            "STRATEGY_SAVE|name=${saved?.name ?: "REJECTED"}|version=${saved?.version ?: 0}|status=${saved?.status ?: "NOT_SAVED"}"
        }
        "strategy_load" -> {
            val c = context ?: return "STRATEGY_LOAD|ERROR=no_context"
            val note = AmarAiStrategyNotesRepository(c).find(args)
            "STRATEGY_LOAD|name=$args|status=${note?.status ?: "NOT_FOUND"}|content=${note?.content ?: ""}"
        }
        else -> null
    }

    private fun executeBacktest(args: String): String {
        val sections = args.split(";", limit = 2)
        if (sections.size < 2) return "BACKTEST|ERROR=use candles=<t:o:h:l:c|...>;signals=<index:BUY:entry:sl:tp|...>"
        val candles = sections[0].substringAfter("candles=", "").split('|').mapNotNull { row ->
            val p = row.split(':').mapNotNull { it.toDoubleOrNull() }
            if (p.size == 5) AmarAdvancedStrategyTestEngine.Candle(p[0].toLong(), p[1], p[2], p[3], p[4]) else null
        }
        val signals = sections[1].substringAfter("signals=", "").split('|').mapNotNull { row ->
            val p = row.split(':')
            if (p.size != 5) return@mapNotNull null
            val side = runCatching { AmarAdvancedStrategyTestEngine.Side.valueOf(p[1].uppercase()) }.getOrNull() ?: return@mapNotNull null
            val n = p[0].toIntOrNull() ?: return@mapNotNull null
            val entry = p[2].toDoubleOrNull() ?: return@mapNotNull null
            val sl = p[3].toDoubleOrNull() ?: return@mapNotNull null
            val tp = p[4].toDoubleOrNull() ?: return@mapNotNull null
            AmarAdvancedStrategyTestEngine.Signal(n, side, entry, sl, tp)
        }
        if (candles.isEmpty() || signals.isEmpty()) return "BACKTEST|ERROR=no_valid_candles_or_signals"
        val r = AmarAdvancedStrategyTestEngine.evaluate(candles, signals)
        return "BACKTEST|verified=${r.verified}|n=${r.sampleSize}|winRate=${"%.2f".format(r.winRatePct)}|PF=${if (r.profitFactor.isInfinite()) "INF" else "%.3f".format(r.profitFactor)}|expectancyR=${"%.4f".format(r.expectancyR)}|maxDD=${"%.3f".format(r.maxDrawdownR)}|netR=${"%.3f".format(r.netR)}|avgBars=${"%.2f".format(r.averageBarsHeld)}"
    }

    private fun evolutionGate(args: String): String {
        val p = parseDoubles(args)
        if (p.size < 7) return "EVOLUTION|ERROR=need expectancy,drawdown,PF,sample,OOS,stress,leakage"
        val c = AmarStrategyEvolutionEngine.mutate("AI-CANDIDATE", "governed mutation from user request", AmarStrategyEvolutionEngine.MutationType.ENTRY)
        val d = AmarStrategyEvolutionEngine.gate(c, AmarStrategyEvolutionEngine.Score(p[0], p[1], p[2], p[3].toInt(), p[4], p[5], p[6] > .5, p.getOrElse(7) { 0.0 }))
        return "EVOLUTION|candidate=${c.id}|gate=${d.gate}|score=${"%.3f".format(d.score)}|reasons=${d.reasons.joinToString(" || ")}"
    }

    private fun parseScore(parts: List<Double>, offset: Int): AmarStrategyEvolutionEngine.Score = AmarStrategyEvolutionEngine.Score(parts[offset], parts[offset + 1], parts[offset + 2], parts[offset + 3].toInt(), parts[offset + 4], parts[offset + 5], parts[offset + 6] > .5, parts[offset + 7])

    private fun compareScores(args: String): String {
        val p = parseDoubles(args)
        if (p.size < 16) return "CHAMPION_CHALLENGER|ERROR=need_16_values(two scores x 8: expectancy,DD,PF,sample,OOS,stress,leakage,complexity)"
        val decision = AmarStrategyEvolutionEngine.championChallenger(parseScore(p, 0), parseScore(p, 8))
        return "CHAMPION_CHALLENGER|decision=$decision"
    }

    private fun counterfactual(args: String): String {
        val p = parseDoubles(args)
        if (p.size < 16) return "COUNTERFACTUAL|ERROR=need_16_values(two scores x 8)"
        val d = AmarStrategyEvolutionEngine.counterfactual(parseScore(p, 0), parseScore(p, 8))
        return "COUNTERFACTUAL|${d.entries.joinToString("|") { "${it.key}=${"%.4f".format(it.value)}" }}"
    }

    private fun uncertainty(args: String): String {
        val p = parseDoubles(args)
        if (p.size < 5) return "UNCERTAINTY|ERROR=need sample,expectancyStdev,oosWindows,failedWindows,drift"
        val r = AmarDriftAndUncertaintyEngine.uncertainty(p[0].toInt(), p[1], p[2].toInt(), p[3].toInt(), p[4])
        return "UNCERTAINTY|uncertainty=${"%.1f".format(r.uncertaintyPct)}|confidence=${"%.1f".format(r.confidencePct)}|reasons=${r.reasons.joinToString(" || ")}"
    }

    private fun parseDoubles(args: String): List<Double> = args.split(',', ';', ' ', '\n').mapNotNull { it.trim().toDoubleOrNull() }

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
