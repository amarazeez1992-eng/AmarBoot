package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import org.json.JSONArray
import org.json.JSONObject

/**
 * AI Supervisor boundary.
 * The model can analyze, research through registered tools, test, inspect and manage
 * the local strategy notebook. Broker execution remains a separate transport boundary.
 */
class AmarAiAgentEngine(
    private val context: Context? = null,
    private val gemini: AmarGeminiClient = AmarGeminiClient()
) {
    data class Result(val answer: String, val proposedActions: List<String>, val toolEvidence: List<String>)
    private data class Plan(val answer: String, val actions: List<Pair<String, String>>)

    suspend fun ask(apiKey: String, model: String, request: String): Result {
        val first = gemini.generate(apiKey, model, systemPrompt(), buildPrompt(request))
        val plan = parsePlan(first.text)
        val evidence = plan.actions.mapNotNull { executeTool(it.first, it.second) }
        if (evidence.isEmpty()) return Result(plan.answer, plan.actions.map { "${it.first}: ${it.second}" }, emptyList())
        val finalPrompt = buildPrompt(request) + "\n\nTOOL RESULTS:\n" + evidence.joinToString("\n") +
            "\n\nReturn the final Arabic decision. Distinguish facts, calculations, assumptions and recommendations. If a strategy is incomplete, explain the missing rule and propose alternatives. Never invent a backtest result. Finish with the exact next approval step required from the user."
        val second = gemini.generate(apiKey, model, systemPrompt(), finalPrompt)
        val finalPlan = parsePlan(second.text)
        return Result(finalPlan.answer, plan.actions.map { "${it.first}: ${it.second}" }, evidence)
    }

    private fun buildPrompt(request: String): String {
        val s = AmarMarketStateStore.snapshot
        val contextJson = JSONObject().put("market", JSONObject()
            .put("symbol", s.symbol).put("timeframe", s.timeframe.name).put("bid", s.bid).put("ask", s.ask)
            .put("spread", s.spread).put("direction", s.direction.name).put("strength", s.strength)
            .put("candleOpen", s.candleOpen).put("candleHigh", s.candleHigh).put("candleLow", s.candleLow).put("candleClose", s.candleClose)
            .put("session", s.session).put("source", s.source.name).put("quality", s.quality.name))
            .put("capabilities", "market analysis, strategy design, strategy critique, strategy notebook, Bot Lab, B21 testing, B22 deterministic simulation, chart analysis")
            .put("executionPolicy", "CURRENT BUILD: analysis/advisory/local strategy management; broker execution transport remains disabled until MT5 bridge phase")
            .put("authority", "User is final decision maker. Never silently change an approved strategy. Any proposed change must be presented for approval.")
        return "USER REQUEST:\n$request\n\nAPP CONTEXT:\n$contextJson"
    }

    private fun systemPrompt() = """
You are AMAR AI Supervisor inside AmarBoot.
You are a high-level trading-system advisor and engineering copilot, not a passive chatbot.
You must challenge incomplete or inconsistent strategies, explain why, propose improvements, compare alternatives, and request the user's approval before changing an approved strategy or authorizing broker execution.
You may analyze real app evidence only when the source/quality says it is available. Never invent prices, percentages, indicators, backtest statistics, broker state or execution results.
A confidence percentage is allowed only when its basis is explicitly described. A strategy success rate must come from a verified historical/forward test; never guess it.
You can inspect the app, test deterministic strategy logic, search the registered library, and save/load strategy notes through tools.
Broker execution is NOT available in the current build. Never claim that an order was sent, opened, closed or modified.
When the future MT5 transport is connected, execution must still pass through the command authority, emergency stop, validation, idempotency and post-execution verification.
Return ONLY valid JSON: {"answer":"Arabic answer","actions":[{"tool":"library_search|analyze_market|test_strategy|inspect_bot|strategy_save|strategy_load","args":"short description"}],"approvalRequired":true}
""".trimIndent()

    private fun executeTool(tool: String, args: String): String? = when (tool) {
        "library_search" -> {
            val q = args.lowercase()
            val catalog = listOf(
                "BOT 1 — grid, tracking, execution controls",
                "Indicator Desk — indicator library",
                "Strategy Vault — strategy research",
                "Chart Pack — chart analysis",
                "Risk Pack — risk analysis"
            )
            val matches = catalog.filter { q.isBlank() || it.lowercase().contains(q) || (q.contains("indicator") && it.startsWith("Indicator")) || (q.contains("مؤشر") && it.startsWith("Indicator")) }
            "library_search($args) => ${if (matches.isEmpty()) catalog.joinToString(" | ") else matches.joinToString(" | ")}"
        }
        "analyze_market" -> {
            val s = AmarMarketStateStore.snapshot
            "analyze_market => symbol=${s.symbol}, timeframe=${s.timeframe}, bid=${s.bid}, ask=${s.ask}, spread=${s.spread}, direction=${s.direction}, strength=${s.strength}, source=${s.source}, quality=${s.quality}"
        }
        "test_strategy" -> "test_strategy => deterministic B21/B22 test proposal only. A success rate may be reported only after verified test results are available."
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
