package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import org.json.JSONArray
import org.json.JSONObject

/**
 * B36 implementation boundary: AI proposes; local tools provide verified app context;
 * execution remains outside the agent and requires user approval.
 */
class AmarAiAgentEngine(private val gemini: AmarGeminiClient = AmarGeminiClient()) {
    data class Result(val answer: String, val proposedActions: List<String>, val toolEvidence: List<String>)

    suspend fun ask(apiKey: String, model: String, request: String): Result {
        val snapshot = AmarMarketStateStore.snapshot
        val context = JSONObject()
            .put("market", JSONObject()
                .put("symbol", snapshot.symbol)
                .put("timeframe", snapshot.timeframe.name)
                .put("bid", snapshot.bid)
                .put("ask", snapshot.ask)
                .put("spread", snapshot.spread)
                .put("direction", snapshot.direction.name)
                .put("strength", snapshot.strength)
                .put("candleOpen", snapshot.candleOpen)
                .put("candleHigh", snapshot.candleHigh)
                .put("candleLow", snapshot.candleLow)
                .put("candleClose", snapshot.candleClose)
                .put("session", snapshot.session)
                .put("source", snapshot.source.name)
                .put("quality", snapshot.quality.name))
            .put("library", JSONArray(listOf(
                JSONObject().put("name", "BOT 1").put("type", "bot").put("capabilities", "grid,tracking,execution controls"),
                JSONObject().put("name", "Indicator Desk").put("type", "indicator").put("capabilities", "indicator library"),
                JSONObject().put("name", "Strategy Vault").put("type", "strategy").put("capabilities", "strategy research"),
                JSONObject().put("name", "Chart Pack").put("type", "chart").put("capabilities", "chart analysis"),
                JSONObject().put("name", "Risk Pack").put("type", "risk").put("capabilities", "risk analysis"))))
            .put("modes", "SIMULATION, DEMO, READ_ONLY; LIVE execution is disabled")
            .put("testing", "B21 testing and B22 deterministic simulation exist; AI may propose tests but cannot execute broker trades")

        val system = """
You are AMAR AI Supervisor inside AmarBoot. You are an analytical agent, not an autonomous trader.
Use the supplied app context. Never invent market values. If market quality is not LIVE, say so.
You may recommend indicators, strategies, tests, simulations and app actions.
Never place, modify or close broker orders. Never enable LIVE mode. Every execution must become a proposal for user approval.
Return ONLY valid JSON with this shape:
{"answer":"Arabic answer","actions":[{"tool":"library_search|analyze_market|test_strategy|inspect_bot","args":"short description"}],"approvalRequired":true}
If data is missing, explicitly state what is missing. Be concise but rigorous.
        """.trimIndent()
        val prompt = "USER REQUEST:\n$request\n\nAPP CONTEXT:\n$context"
        val response = gemini.generate(apiKey, model, system, prompt)
        return parse(response.text)
    }

    private fun parse(raw: String): Result {
        val cleaned = raw.trim().removePrefix("```").removeSuffix("```").trim()
        return runCatching {
            val json = JSONObject(cleaned)
            val answer = json.optString("answer", cleaned)
            val actions = mutableListOf<String>()
            val array = json.optJSONArray("actions") ?: JSONArray()
            for (i in 0 until array.length()) {
                val action = array.optJSONObject(i) ?: continue
                actions += "${action.optString("tool", "proposal")}: ${action.optString("args", "")}".trim()
            }
            Result(answer, actions, emptyList())
        }.getOrElse { Result(cleaned, emptyList(), listOf("Gemini returned non-structured text; no action was executed.")) }
    }
}
