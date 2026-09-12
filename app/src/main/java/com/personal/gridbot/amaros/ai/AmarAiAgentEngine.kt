package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import org.json.JSONArray
import org.json.JSONObject

/** AI Supervisor: multi-stage, evidence-driven, approval-gated and fail-closed. */
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
        if (evidence.isEmpty()) {
            return Result(plan.answer, plan.actions.map { "${it.first}: ${it.second}" }, emptyList())
        }
        val finalPrompt = buildPrompt(request) +
            "\n\nEVIDENCE FROM LOCAL/EXTERNAL TOOLS:\n" + evidence.joinToString("\n") +
            "\n\nFINAL REVIEW PROTOCOL:\n" +
            "1) Separate verified facts from inference and opinion. " +
            "2) Identify contradictions or missing data. " +
            "3) Never convert a quality score into a profitability forecast. " +
            "4) Never report a backtest/success rate unless measured evidence exists. " +
            "5) State limitations, sample size and source quality. " +
            "6) If the strategy is incomplete, propose the exact missing rules. " +
            "7) Finish with the exact next approval step required from the user."
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
                .put("candleOpen", s.candleOpen).put("candleHigh", s.candleHigh).put("candleLow", s.candleLow).put("candleClose", s.candleClose)
                .put("session", s.session).put("source", s.source.name).put("quality", s.quality.name))
            .put("capabilities", "market analysis, strategy design/critique, strategy quality audit, strategy notebook, Bot Lab, B21/B22 testing, statistical validation, chart analysis, external research, voice/image input")
            .put("researchHierarchy", AmarTradingResearchRegistry.catalogText())
            .put("researchProtocol", AmarTradingResearchRegistry.protocol())
            .put("executionPolicy", "CURRENT BUILD: analysis/advisory/local strategy management; broker execution transport remains disabled until MT5 bridge phase")
            .put("authority", "User is final decision maker. Never silently change an approved strategy. Any proposed change must be presented for approval.")
        return "USER REQUEST:\n$request\n\nAPP CONTEXT:\n$contextJson"
    }

    private fun systemPrompt() = """
You are AMAR AI Supervisor inside AmarBoot.
You are a professional trading research, strategy engineering, validation and risk-analysis system — not a passive chatbot.
Use a disciplined pipeline: define -> challenge -> research -> formalize -> test -> stress-test -> audit -> recommend -> request approval.
Challenge incomplete strategies. Detect ambiguity, survivorship bias, look-ahead leakage, repainting, overfitting, hidden exposure, unrealistic fills, excessive concentration and undefined failure modes.
Use only real evidence. Never invent prices, percentages, indicators, backtest statistics, broker state or execution results.
A strategy quality score measures specification quality only. It is NOT probability of profit.
A strategy success rate may be reported only when verified historical/forward test evidence exists, with sample size and methodology.
Treat Tier 1 regulators/exchanges/central institutions as higher-quality evidence than community code or educational references.
External research must be clearly labelled and never treated as proof without validation.
Statistical validation must report limitations and must not imply that Monte Carlo or historical results guarantee the future.
You may inspect the app, audit strategy definitions, search external sources, validate measured trade results and save/load strategy notes through tools.
Broker execution is NOT available in the current build. Never claim an order was sent, opened, closed or modified.
When future MT5 transport is connected, execution must still pass through command authority, emergency stop, validation, idempotency, ACK, postcondition verification and reconciliation.
Return ONLY valid JSON: {"answer":"Arabic answer","actions":[{"tool":"library_search|analyze_market|research_external|strategy_quality|test_strategy|validate_results|inspect_bot|strategy_save|strategy_load","args":"short description"}],"approvalRequired":true}
""".trimIndent()

    private suspend fun executeTool(tool: String, args: String): String? {
        return when (tool) {
            "library_search" -> {
                val q = args.lowercase()
                val catalog = listOf(
                    "BOT 1 — grid, tracking, execution controls",
                    "Indicator Desk — indicator library",
                    "Strategy Vault — strategy research",
                    "Chart Pack — chart analysis",
                    "Risk Pack — risk analysis",
                    "Research Hierarchy — regulator/exchange/institution sources"
                )
                val matches = catalog.filter {
                    q.isBlank() || it.lowercase().contains(q) ||
                        (q.contains("indicator") && it.startsWith("Indicator")) ||
                        (q.contains("مؤشر") && it.startsWith("Indicator"))
                }
                "library_search($args) => ${if (matches.isEmpty()) catalog.joinToString(" | ") else matches.joinToString(" | ")}"
            }
            "analyze_market" -> {
                val s = AmarMarketStateStore.snapshot
                "analyze_market => symbol=${s.symbol}, timeframe=${s.timeframe}, bid=${s.bid}, ask=${s.ask}, spread=${s.spread}, direction=${s.direction}, strength=${s.strength}, source=${s.source}, quality=${s.quality}"
            }
            "research_external" -> {
                val results = runCatching { research.search(args, 6) }.getOrElse { return "research_external => failed: ${it.message ?: "unknown error"}" }
                if (results.isEmpty()) "research_external($args) => no public results"
                else results.joinToString("\n") { "EXTERNAL|${it.source}|${it.title}|${it.url}|${it.excerpt}" }
            }
            "strategy_quality" -> {
                val audit = AmarStrategyQualityEngine.audit(args)
                "strategy_quality => level=${audit.level}/7 score=${"%.1f".format(audit.scorePct)}% verdict=${audit.verdict} strengths=${audit.strengths.joinToString(",")} missing=${audit.missing.joinToString(",")} redFlags=${audit.redFlags.joinToString(",")}"
            }
            "test_strategy" -> {
                "test_strategy => deterministic B21/B22 test boundary. Provide verified candle/signal data to produce measured trades; no profitability claim is allowed from a description alone."
            }
            "validate_results" -> {
                val values = args.split(',', ';', ' ', '\n').mapNotNull { it.trim().toDoubleOrNull() }
                if (values.size < 2) "validate_results => insufficient measured trade R values; need at least 2 finite observations"
                else {
                    val r = AmarStrategyValidationEngine.analyze(values)
                    "validate_results => n=${r.sampleSize} winRate=${"%.2f".format(r.winRatePct)}% PF=${"%.3f".format(r.profitFactor)} expectancyR=${"%.4f".format(r.expectancyR)} payoff=${"%.3f".format(r.payoffRatio)} maxDD=${"%.3f".format(r.maxDrawdownR)} recovery=${"%.3f".format(r.recoveryFactor)} SQN=${"%.3f".format(r.sqn)} longestLoss=${r.longestLossStreak} top20ProfitConcentration=${"%.1f".format(r.profitConcentrationTop20Pct)}% MC_DD_P50=${"%.3f".format(r.monteCarloDrawdownP50)} MC_DD_P95=${"%.3f".format(r.monteCarloDrawdownP95)} verified=${r.verified}"
                }
            }
            "inspect_bot" -> "inspect_bot => BOT Lab is available through its existing boundary; no runtime mutation performed."
            "strategy_save" -> {
                val repo = context?.let { AmarAiStrategyNotesRepository(it) }
                    ?: return "strategy_save => unavailable: AI engine has no Android context"
                val parts = args.split("::", limit = 2)
                val name = parts.firstOrNull()?.trim().orEmpty()
                val content = parts.getOrNull(1)?.trim().orEmpty()
                val saved = repo.save(name, content, "DRAFT")
                "strategy_save => ${saved?.name ?: "rejected"} version=${saved?.version ?: 0} status=${saved?.status ?: "NOT_SAVED"}"
            }
            "strategy_load" -> {
                val repo = context?.let { AmarAiStrategyNotesRepository(it) }
                    ?: return "strategy_load => unavailable: AI engine has no Android context"
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
