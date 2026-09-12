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

/** AMAR AI Supervisor: Gemini plans, deterministic local engines verify. Broker execution stays fail-closed. */
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
        val second = gemini.generate(
            apiKey,
            model,
            systemPrompt(),
            buildPrompt(request) + "\n\nEXECUTED TOOL EVIDENCE:\n" + evidence.joinToString("\n") +
                "\n\nFINAL REVIEW: report measured results only; expose leakage, overfit, repainting, costs, slippage, drift and uncertainty."
        )
        val finalPlan = parsePlan(second.text)
        return Result(finalPlan.answer, finalPlan.actions.map { "${it.first}: ${it.second}" }, evidence)
    }

    private fun buildPrompt(request: String): String {
        val s = AmarMarketStateStore.snapshot
        val c = context?.let { AmarAiControlCenter(it) }
        val market = JSONObject()
            .put("symbol", s.symbol).put("timeframe", s.timeframe.name)
            .put("bid", s.bid).put("ask", s.ask).put("spread", s.spread)
            .put("direction", s.direction.name).put("strength", s.strength)
            .put("candleOpen", s.candleOpen).put("candleHigh", s.candleHigh)
            .put("candleLow", s.candleLow).put("candleClose", s.candleClose)
            .put("session", s.session).put("source", s.source.name).put("quality", s.quality.name)
        val authority = JSONObject()
            .put("aiEnabled", c?.aiEnabled ?: false)
            .put("emergencyStopped", c?.emergencyStopped ?: true)
            .put("aiAuthorityEnabled", c?.aiAuthorityEnabled ?: false)
            .put("executionAuthorized", c?.executionAuthorized ?: false)
            .put("brokerExecutionAuthorized", c?.brokerExecutionAuthorized ?: false)
        return "USER REQUEST:\n$request\n\nAPP CONTEXT:\n" + JSONObject()
            .put("market", market)
            .put("authority", authority)
            .put("capabilities", AmarTradingIntelligenceRegistry.intelligenceEngines.joinToString(", "))
            .put("knowledgeDomains", AmarTradingKnowledgeLibrary.domains.size)
            .put("sourceMesh", AmarTradingSourceMesh.sourceCatalogText())
            .put("openSourceBots", AmarTradingSourceMesh.botCatalogText())
            .put("mt5Office", AmarAiMt5Office.catalogText())
            .put("executionPolicy", "BROKER EXECUTION DISABLED")
            .put("approvalBoundary", "DRAFT/CHALLENGER only; explicit human approval required")
            .put("deterministicTools", "inspect_app, engine_market, tracking, candle, analyze_market, multi_source_research, test_strategy, validate_results, precision_audit, uncertainty_audit")
    }

    private fun systemPrompt() = """
You are AMAR AI Supervisor, specialized only in trading research, analysis, strategy engineering, validation, risk and governed execution planning.
Use real deterministic tools and measured evidence, never invented results.
For important questions, gather multiple independent sources and distinguish source count from independent evidence.
Classify evidence as SOURCE, VERIFIED, HYPOTHESIS or INFERENCE.
Challenge leakage, repainting, overfit, unrealistic fills, missing costs, slippage, spread, regime mismatch and drift.
If runtime data is unavailable, explicitly say DATA_UNAVAILABLE / fail-closed; never fill gaps from imagination.
For market/candle/position questions, prefer deterministic tools: engine_market, candle, tracking and inspect_app.
For research questions, use multi_source_research and inspect conflicts, provenance, freshness and license/reuse status.
Lifecycle: Idea -> Draft -> Discuss -> Evaluate -> Test -> OOS -> Stress -> Compare -> Risk Gate -> User Approval -> Adopt.
Never auto-adopt. strategy_save is always DRAFT. approval_proposal only records human review.
Broker execution is disabled; never claim an order was executed. Android queue acceptance is not broker execution.
Return JSON: {"answer":"Arabic answer","actions":[{"tool":"...","args":"..."}],"approvalRequired":true}
""".trimIndent()

    private suspend fun executeTool(tool: String, args: String): String? {
        return when (tool) {
            "trading_library_search" -> {
                val hits = AmarTradingKnowledgeLibrary.search(args, 12)
                "LIBRARY|$args|" + hits.joinToString(" || ") { "${it.name}:${it.concepts.joinToString(",")}" }
            }
            "library_search" -> {
                val q = args.lowercase()
                val hits = AmarTradingIntelligenceRegistry.intelligenceEngines.filter { q.isBlank() || it.lowercase().contains(q) }
                "INTELLIGENCE_LIBRARY|$args|${hits.joinToString(" | ")}"
            }
            "analyze_market" -> AmarAiEngineBinding.market()
            "research_external" -> {
                val plan = AmarTradingIntelligenceRegistry.recommendedResearchPlan(args)
                val results = runCatching { research.search(args, 8) }.getOrElse { return "EXTERNAL_RESEARCH|FAILED=${it.message ?: "unknown"}\n$plan" }
                "$plan\n" + if (results.isEmpty()) "EXTERNAL_RESEARCH|NO_PUBLIC_RESULTS" else results.joinToString("\n") { "SOURCE|${it.source}|${it.title}|${it.url}|${it.excerpt}" }
            }
            "multi_source_research" -> {
                val report = runCatching { AmarTradingSourceMesh.research(context, args, research, 12) }
                    .getOrElse { return "MULTI_SOURCE|FAILED=${it.message ?: "unknown"}" }
                val sample = report.evidence.take(20).joinToString("\n") { "EVIDENCE|${it.source}|${it.title}|${it.url}|${it.excerpt}" }
                "MULTI_SOURCE|query=${report.query}|channels=${report.searchedChannels}|evidence=${report.evidence.size}|supporting=${report.supportingChannels}|conflicts=${report.conflictChannels}|independent=${report.independentChannels}|authority=${report.authorityGrade}|confidence=${"%.1f".format(report.authorityConfidencePct)}|consensus=${"%.1f".format(report.consensusPct)}\n$sample\nCAVEAT|${report.caveat}"
            }
            "bot_discovery" -> {
                val q = args.lowercase()
                val bots = AmarTradingSourceMesh.bots.filter { q.isBlank() || (it.name + " " + it.focus).lowercase().contains(q) }
                "BOT_DISCOVERY|${bots.joinToString(" || ") { "${it.name}:${it.repo}:${it.url}" }}|licenseGate=REQUIRED"
            }
            "strategy_quality" -> {
                val a = AmarStrategyQualityEngine.audit(args)
                "STRATEGY_QUALITY|level=${a.level}/7|score=${"%.1f".format(a.scorePct)}|verdict=${a.verdict}|missing=${a.missing.joinToString(",")}|redFlags=${a.redFlags.joinToString(",")}"
            }
            "test_strategy" -> executeBacktest(args)
            "validate_results" -> {
                val values = parseDoubles(args)
                if (values.size < 2) "VALIDATION|ERROR=need_2_R_values"
                else {
                    val r = AmarStrategyValidationEngine.analyze(values)
                    "VALIDATION|n=${r.sampleSize}|winRate=${"%.2f".format(r.winRatePct)}|PF=${"%.3f".format(r.profitFactor)}|expectancyR=${"%.4f".format(r.expectancyR)}|maxDD=${"%.3f".format(r.maxDrawdownR)}|SQN=${"%.3f".format(r.sqn)}|verified=${r.verified}"
                }
            }
            "precision_audit" -> {
                val p = parseDoubles(args)
                val g = AmarTradingPrecisionEngine.defaultResearchGate()
                val input = if (p.size >= 7) AmarTradingPrecisionEngine.Input(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p.getOrElse(7) { 0.0 })
                else AmarTradingPrecisionEngine.Input(g.scorePct / 100.0, g.confidencePct / 100.0, g.scorePct / 100.0, .25, .40, .20, .35, g.uncertaintyPct / 100.0)
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
                "BOT_INSPECTION|${hits.joinToString(" || ") { "${it.id}:${it.name}:${it.type}:${it.path}:mutable=${it.mutable}" }}"
            }
            "strategy_save" -> {
                val c = context ?: return "STRATEGY_SAVE|ERROR=no_context"
                val parts = args.split("::", limit = 2)
                val saved = AmarAiStrategyNotesRepository(c).save(parts.firstOrNull()?.trim().orEmpty(), parts.getOrNull(1)?.trim().orEmpty(), "DRAFT")
                "STRATEGY_SAVE|name=${saved?.name ?: "REJECTED"}|version=${saved?.version ?: 0}|status=${saved?.status ?: "NOT_SAVED"}"
            }
            "strategy_load" -> {
                val c = context ?: return "STRATEGY_LOAD|ERROR=no_context"
                val note = AmarAiStrategyNotesRepository(c).find(args)
                "STRATEGY_LOAD|name=$args|status=${note?.status ?: "NOT_FOUND"}|content=${note?.content ?: ""}"
            }
            else -> AmarAiDeterministicToolGateway.execute(tool, args)
        }
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
            AmarAdvancedStrategyTestEngine.Signal(n, side, p[2].toDoubleOrNull() ?: return@mapNotNull null, p[3].toDoubleOrNull() ?: return@mapNotNull null, p[4].toDoubleOrNull() ?: return@mapNotNull null)
        }
        if (candles.isEmpty() || signals.isEmpty()) return "BACKTEST|ERROR=no_valid_candles_or_signals"
        val r = AmarAdvancedStrategyTestEngine.evaluate(candles, signals)
        return "BACKTEST|verified=${r.verified}|n=${r.sampleSize}|winRate=${"%.2f".format(r.winRatePct)}|PF=${if (r.profitFactor.isInfinite()) "INF" else "%.3f".format(r.profitFactor)}|expectancyR=${"%.4f".format(r.expectancyR)}|maxDD=${"%.3f".format(r.maxDrawdownR)}|netR=${"%.3f".format(r.netR)}"
    }

    private fun evolutionGate(args: String): String {
        val p = parseDoubles(args)
        if (p.size < 7) return "EVOLUTION|ERROR=need_expectancy,drawdown,PF,sample,OOS,stress,leakage"
        val c = AmarStrategyEvolutionEngine.mutate("AI-CANDIDATE", "governed mutation", AmarStrategyEvolutionEngine.MutationType.ENTRY)
        val d = AmarStrategyEvolutionEngine.gate(c, AmarStrategyEvolutionEngine.Score(p[0], p[1], p[2], p[3].toInt(), p[4], p[5], p[6] > .5, p.getOrElse(7) { 0.0 }))
        return "EVOLUTION|candidate=${c.id}|gate=${d.gate}|score=${"%.3f".format(d.score)}|reasons=${d.reasons.joinToString(" || ")}"
    }

    private fun parseScore(p: List<Double>, o: Int) = AmarStrategyEvolutionEngine.Score(p[o], p[o + 1], p[o + 2], p[o + 3].toInt(), p[o + 4], p[o + 5], p[o + 6] > .5, p[o + 7])

    private fun compareScores(args: String): String {
        val p = parseDoubles(args)
        if (p.size < 16) return "CHAMPION_CHALLENGER|ERROR=need_16_values"
        return "CHAMPION_CHALLENGER|decision=${AmarStrategyEvolutionEngine.championChallenger(parseScore(p, 0), parseScore(p, 8))}"
    }

    private fun counterfactual(args: String): String {
        val p = parseDoubles(args)
        if (p.size < 16) return "COUNTERFACTUAL|ERROR=need_16_values"
        val r = AmarStrategyEvolutionEngine.counterfactual(parseScore(p, 0), parseScore(p, 8))
        return "COUNTERFACTUAL|${r.entries.joinToString("|") { "${it.key}=${"%.4f".format(it.value)}" }}"
    }

    private fun uncertainty(args: String): String {
        val p = parseDoubles(args)
        if (p.size < 5) return "UNCERTAINTY|ERROR=need_5_values"
        val r = AmarDriftAndUncertaintyEngine.uncertainty(p[0].toInt(), p[1], p[2].toInt(), p[3].toInt(), p[4])
        return "UNCERTAINTY|uncertainty=${"%.1f".format(r.uncertaintyPct)}|confidence=${"%.1f".format(r.confidencePct)}|reasons=${r.reasons.joinToString(" || ")}"
    }

    private fun parseDoubles(args: String): List<Double> = args.split(',', ';', ' ', '\n').mapNotNull { it.trim().toDoubleOrNull() }

    private fun parsePlan(raw: String): Plan {
        val cleaned = raw.trim().removePrefix("```").removeSuffix("```").trim()
        return runCatching {
            val json = JSONObject(cleaned)
            val array = json.optJSONArray("actions") ?: JSONArray()
            val actions = buildList {
                for (i in 0 until array.length()) {
                    val a = array.optJSONObject(i) ?: continue
                    add(a.optString("tool", "proposal") to a.optString("args", ""))
                }
            }
            Plan(json.optString("answer", cleaned), actions)
        }.getOrElse { Plan(cleaned, emptyList()) }
    }
}
