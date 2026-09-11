package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import org.json.JSONArray
import org.json.JSONObject

/** B36 boundary: AI proposes; read-only tools provide app evidence; execution needs user approval. */
class AmarAiAgentEngine(private val gemini: AmarGeminiClient = AmarGeminiClient()) {
    data class Result(val answer: String, val proposedActions: List<String>, val toolEvidence: List<String>)
    private data class Plan(val answer: String, val actions: List<Pair<String, String>>)

    suspend fun ask(apiKey: String, model: String, request: String): Result {
        val first = gemini.generate(apiKey, model, systemPrompt(), buildPrompt(request))
        val plan = parsePlan(first.text)
        val evidence = plan.actions.mapNotNull { executeReadOnlyTool(it.first, it.second) }
        if (evidence.isEmpty()) return Result(plan.answer, plan.actions.map { "${it.first}: ${it.second}" }, emptyList())
        val finalPrompt = buildPrompt(request) + "\n\nREAD-ONLY TOOL RESULTS:\n" + evidence.joinToString("\n") +
            "\n\nReturn the final Arabic decision, compare the evidence, list risks, and finish with USER APPROVAL REQUIRED."
        val second = gemini.generate(apiKey, model, systemPrompt(), finalPrompt)
        val finalPlan = parsePlan(second.text)
        return Result(finalPlan.answer, plan.actions.map { "${it.first}: ${it.second}" }, evidence)
    }

    private fun buildPrompt(request: String): String {
        val s = AmarMarketStateStore.snapshot
        val context = JSONObject().put("market", JSONObject()
            .put("symbol", s.symbol).put("timeframe", s.timeframe.name).put("bid", s.bid).put("ask", s.ask)
            .put("spread", s.spread).put("direction", s.direction.name).put("strength", s.strength)
            .put("candleOpen", s.candleOpen).put("candleHigh", s.candleHigh).put("candleLow", s.candleLow).put("candleClose", s.candleClose)
            .put("session", s.session).put("source", s.source.name).put("quality", s.quality.name))
            .put("capabilities", "BOT 1, indicator library, strategy research, chart analysis, B21 testing, B22 deterministic simulation")
            .put("executionPolicy", "SIMULATION/DEMO/READ_ONLY only; LIVE execution disabled")
        return "USER REQUEST:\n$request\n\nAPP CONTEXT:\n$context"
    }

    private fun systemPrompt() = """
You are AMAR AI Supervisor inside AmarBoot. You are an analytical agent, not an autonomous trader.
Use app context and read-only tools. Never invent market values. If market quality is not LIVE, say so.
For library requests use library_search. For current conditions use analyze_market. For bot/strategy testing use test_strategy and do not claim a test ran unless verified results exist.
Never place, modify or close broker orders. Never enable LIVE mode. Every execution is a proposal for user approval.
Return ONLY valid JSON: {"answer":"Arabic answer","actions":[{"tool":"library_search|analyze_market|test_strategy|inspect_bot","args":"short description"}],"approvalRequired":true}
""".trimIndent()

    private fun executeReadOnlyTool(tool: String, args: String): String? = when (tool) {
        "library_search" -> {
            val q = args.lowercase()
            val catalog = listOf(
                "BOT 1 — bot — grid, tracking, execution controls",
                "Indicator Desk — indicator — indicator library",
                "Strategy Vault — strategy — strategy research",
                "Chart Pack — chart — chart analysis",
                "Risk Pack — risk — risk analysis"
            )
            val matches = catalog.filter { q.isBlank() || it.lowercase().contains(q) || (q.contains("indicator") && it.startsWith("Indicator")) || (q.contains("مؤشر") && it.startsWith("Indicator")) }
            "library_search($args) => ${if (matches.isEmpty()) catalog.joinToString(" | ") else matches.joinToString(" | ")}"
        }
        "analyze_market" -> {
            val s = AmarMarketStateStore.snapshot
            "analyze_market => symbol=${s.symbol}, timeframe=${s.timeframe}, bid=${s.bid}, ask=${s.ask}, spread=${s.spread}, direction=${s.direction}, strength=${s.strength}, source=${s.source}, quality=${s.quality}"
        }
        "test_strategy" -> "test_strategy => proposal only: prepare deterministic B21/B22 simulation inputs and report required; no broker execution was performed."
        "inspect_bot" -> "inspect_bot => proposal only: BOT 1 is available through the existing Bot Lab boundary; no runtime mutation performed."
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
