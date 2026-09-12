package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingIntelligenceRegistry
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingPrecisionEngine
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

        val finalPrompt = buildPrompt(request) +
            "\n\nEVIDENCE FROM LOCAL/EXTERNAL TOOLS:\n" + evidence.joinToString("\n") +
            "\n\nFINAL REVIEW PROTOCOL:\n" +
            "1) Separate SOURCE, VERIFIED, HYPOTHESIS and INFERENCE. " +
            "2) Detect contradictions, missing data and uncertainty. " +
            "3) Never convert a quality/confidence score into a profitability probability. " +
            "4) Never report a backtest/success rate without measured evidence, methodology and sample size. " +
            "5) Check regime suitability, leakage, overfitting, costs, slippage and execution assumptions. " +
            "6) Prefer OOS, walk-forward and stress evidence over in-sample optimization. " +
            "7) State exact failure conditions and what evidence is still required. " +
            "8) Finish with the exact next approval step required from the user."
        val second = gemini.generate(apiKey, model, systemPrompt(), finalPrompt)
        val finalPlan = parsePlan(second.text)
        return Result(finalPlan.answer, plan.actions.map { "${it.first}: ${it.second}" }, evidence)
    }

    private fun buildPrompt(request: String): String {
        val s = AmarMarketStateStore.snapshot
        val contextJson = JSONObject()
            .put("market", JSONObject()
                .put("symbol", s.symbol).put("timeframe", s.timeframe.name).put("bid", s.bid).put("ask", s.ask)
                .put("spread", s.spread).put("direction", s.direction.name).put("strength", s.strength)
                .put("candleOpen", s.candleOpen).put("candleHigh", s.candleHigh).put("candleLow", s.candleLow)
                .put("candleClose", s.candleClose).put("session", s.session).put("source", s.source.name).put("quality", s.quality.name))
            .put("capabilities", AmarTradingIntelligenceRegistry.intelligenceEngines.joinToString(", "))
            .put("researchCatalog", AmarTradingIntelligenceRegistry.catalogText())
            .put("researchGovernance", AmarTradingIntelligenceRegistry.governance())
            .put("researchHierarchy", AmarTradingResearchRegistry.catalogText())
            .put("researchProtocol", AmarTradingResearchRegistry.protocol())
            .put("executionPolicy", "CURRENT BUILD: analysis/advisory/local strategy management; broker execution transport remains disabled until MT5 bridge phase")
            .put("authority", "User is final decision maker. Never silently change an approved strategy. Any proposed change must be presented for approval.")
            .put("precisionPolicy", "High precision means evidence quality + validation + uncertainty control, never a promised win rate.")
        return "USER REQUEST:\n$request\n\nAPP CONTEXT:\n$contextJson"
    }

    private fun systemPrompt() = """
You are AMAR AI Supervisor inside AmarBoot.
You are a professional trading research, strategy engineering, quantitative validation, risk and decision-intelligence system — not a passive chatbot.
Operate as a senior quant/research architect: define -> challenge -> source -> formalize -> regime-map -> test -> walk-forward -> OOS -> stress -> compare -> audit -> recommend -> human approval.
Your objective is maximum decision accuracy and engineering rigor, not maximum confidence.
Never invent market data, prices, indicators, backtest statistics, broker state, source claims or execution results.
Use SOURCE for external material, VERIFIED only for measured/reproducible evidence, HYPOTHESIS for untested ideas, and INFERENCE for model reasoning.
Challenge incomplete strategies. Detect ambiguity, survivorship bias, look-ahead leakage, repainting, overfitting, data snooping, hidden exposure, unrealistic fills, spread/slippage omission, regime mismatch, concentration and undefined failure modes.
Treat Tier 1 regulators/exchanges/central institutions as highest-quality evidence. Treat LuxAlgo as a strong engineering/reference ecosystem, but never as proof of profitability. Respect every open-source license and never reproduce restricted/proprietary content.
Use the official TradingView documentation for Pine execution/alerts and treat TradingView webhooks as a future authenticated transport boundary, never as broker authority.
When Pine/PineTS code is used, require independent AMAR validation, OOS testing and stress testing before adoption.
Prefer deterministic calculations, reproducible datasets, experiment IDs, dataset fingerprints, explicit assumptions and auditable evidence chains.
Use the precision gate to decide whether the system is research-only, paper-test, human-review or approval-ready. The gate is NOT a probability of profit.
Strategy changes follow: Idea -> Save Draft -> Discuss -> Evaluate -> Test -> OOS -> Stress -> Compare -> Risk Gate -> User Approval -> Adopt. No autonomous adoption.
Broker execution is NOT available in the current build. Never claim an order was sent, opened, closed or modified.
When future MT5 transport is connected, execution must pass command authority, emergency stop, scope validation, TTL, replay protection, idempotency, ACK, postcondition verification and reconciliation.
Return ONLY valid JSON: {"answer":"Arabic answer","actions":[{"tool":"library_search|analyze_market|research_external|strategy_quality|test_strategy|validate_results|inspect_bot|strategy_save|strategy_load|precision_audit","args":"short description"}],"approvalRequired":true}
""".trimIndent()

    private suspend fun executeTool(tool: String, args: String): String? = when (tool) {
        "library_search" -> {
            val catalog = AmarTradingIntelligenceRegistry.intelligenceEngines
            val q = args.lowercase()
            val matches = catalog.filter { q.isBlank() || it.lowercase().contains(q) ||
                (q.contains("regime") && it.contains("Regime")) ||
                (q.contains("strategy") && it.contains("Strategy")) }
            "library_search($args) => ${if (matches.isEmpty()) catalog.joinToString(" | ") else matches.joinToString(" | ")}"
        }
        "analyze_market" -> {
            val s = AmarMarketStateStore.snapshot
            "analyze_market => symbol=${s.symbol}, timeframe=${s.timeframe}, bid=${s.bid}, ask=${s.ask}, spread=${s.spread}, direction=${s.direction}, strength=${s.strength}, source=${s.source}, quality=${s.quality}"
        }
        "research_external" -> {
            val plan = AmarTradingIntelligenceRegistry.recommendedResearchPlan(args)
            val results = runCatching { research.search(args, 6) }.getOrElse { return "research_external => $plan\nFETCH_FAILED=${it.message ?: "unknown error"}" }
            if (results.isEmpty()) "$plan\nNO_PUBLIC_RESULTS"
            else "$plan\n" + results.joinToString("\n") { "EXTERNAL|${it.source}|${it.title}|${it.url}|${it.excerpt}" }
        }
        "strategy_quality" -> {
            val audit = AmarStrategyQualityEngine.audit(args)
            "strategy_quality => level=${audit.level}/7 score=${"%.1f".format(audit.scorePct)}% verdict=${audit.verdict} strengths=${audit.strengths.joinToString(",")} missing=${audit.missing.joinToString(",")} redFlags=${audit.redFlags.joinToString(",")}"
        }
        "test_strategy" -> "test_strategy => deterministic B21/B22 boundary. Measured candles/signals are required; descriptions alone cannot produce profitability results."
        "validate_results" -> {
            val values = args.split(',', ';', ' ', '\n').mapNotNull { it.trim().toDoubleOrNull() }
            if (values.size < 2) "validate_results => insufficient measured trade R values; need at least 2 finite observations"
            else {
                val r = AmarStrategyValidationEngine.analyze(values)
                "validate_results => n=${r.sampleSize} winRate=${"%.2f".format(r.winRatePct)}% PF=${"%.3f".format(r.profitFactor)} expectancyR=${"%.4f".format(r.expectancyR)} payoff=${"%.3f".format(r.payoffRatio)} maxDD=${"%.3f".format(r.maxDrawdownR)} recovery=${"%.3f".format(r.recoveryFactor)} SQN=${"%.3f".format(r.sqn)} longestLoss=${r.longestLossStreak} MC_DD_P50=${"%.3f".format(r.monteCarloDrawdownP50)} MC_DD_P95=${"%.3f".format(r.monteCarloDrawdownP95)} verified=${r.verified}"
            }
        }
        "precision_audit" -> {
            val p = args.split(',', ';', ' ', '\n').mapNotNull { it.trim().toDoubleOrNull() }
            val input = if (p.size >= 7) AmarTradingPrecisionEngine.Input(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p.getOrElse(7) { 0.0 })
            else AmarTradingPrecisionEngine.defaultResearchGate().let { AmarTradingPrecisionEngine.Input(it.scorePct / 100.0, it.confidencePct / 100.0, it.scorePct / 100.0, 0.25, 0.40, 0.20, 0.35, it.uncertaintyPct / 100.0) }
            val r = AmarTradingPrecisionEngine.evaluate(input)
            "precision_audit => score=${"%.1f".format(r.scorePct)}% confidence=${"%.1f".format(r.confidencePct)}% uncertainty=${"%.1f".format(r.uncertaintyPct)}% gate=${r.gate} reasons=${r.reasons.joinToString(" | ")}"
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
