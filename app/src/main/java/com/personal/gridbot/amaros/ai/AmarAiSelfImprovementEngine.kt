package com.personal.gridbot.amaros.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Trading-only self-improvement layer.
 * AI may discover and propose upgrades; it never changes source code or adopts a strategy automatically.
 */
class AmarAiSelfImprovementEngine(context: Context) {
    enum class Status { PROPOSED, APPROVED, REJECTED }

    data class Proposal(
        val id: String,
        val area: String,
        val title: String,
        val reason: String,
        val action: String,
        val evidenceRequirement: String,
        val priority: Int,
        val status: Status = Status.PROPOSED,
        val createdAtMs: Long = System.currentTimeMillis()
    )

    data class Audit(
        val score: Double,
        val proposals: List<Proposal>,
        val strengths: List<String>,
        val warnings: List<String>
    )

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun audit(): Audit {
        val proposals = mutableListOf<Proposal>()
        fun add(area: String, title: String, reason: String, action: String, evidence: String, priority: Int) {
            proposals += Proposal(
                id = "AI-UP-${System.currentTimeMillis()}-${proposals.size}",
                area = area,
                title = title,
                reason = reason,
                action = action,
                evidenceRequirement = evidence,
                priority = priority
            )
        }

        add("AI", "Trading Agent Tool Registry", "The agent has deterministic tools but needs a versioned capability registry and health state.",
            "Add tool metadata: owner, input schema, latency budget, freshness, failure mode, evidence class and version.",
            "Every tool must expose deterministic output and provenance.", 10)
        add("AI", "Evidence Independence Graph", "Source count alone can overstate confidence when many pages copy one source.",
            "Cluster sources by origin, citation lineage and content fingerprint before calculating consensus.",
            "Independent-source count must be reported separately from raw result count.", 10)
        add("AI", "Research Confidence Gate", "Trading conclusions need explicit freshness, authority, conflict and uncertainty gates.",
            "Require authority, freshness, independence, conflict and uncertainty scores before a conclusion can be marked VERIFIED.",
            "No VERIFIED conclusion when a required evidence dimension is missing.", 10)
        add("AI", "Self-Improvement Queue", "AI needs a durable proposal queue instead of ephemeral suggestions.",
            "Persist proposals with status PROPOSED/APPROVED/REJECTED and keep a full audit trail.",
            "Human approval remains mandatory for adoption.", 9)
        add("AI", "Research Cost Router", "Searching every provider for every request wastes time and bandwidth.",
            "Route queries by domain, urgency, freshness and expected information gain; fan out only when justified.",
            "Measure latency, duplicate rate and evidence yield per provider.", 9)
        add("AI", "Uncertainty-First Answers", "Market information can be stale, conflicting or incomplete.",
            "Make DATA_UNAVAILABLE, CONFLICTED, STALE and HYPOTHESIS first-class answer states.",
            "The agent must refuse false precision and state what is missing.", 10)
        add("BOT LAB", "Walk-Forward Pipeline", "A backtest alone is insufficient for strategy validation.",
            "Chain in-sample, OOS and rolling walk-forward evaluation with parameter stability checks.",
            "Promotion requires passing predefined OOS and stability gates.", 10)
        add("BOT LAB", "Market-Cost Model", "Grid and scalping results are highly sensitive to spread, slippage and commission.",
            "Model spread, slippage, commission, latency and execution uncertainty in every serious test.",
            "A result without realistic costs is RESEARCH_ONLY.", 10)
        add("BOT LAB", "Regime & Stress Lab", "A strategy can fail when volatility, trend or liquidity changes.",
            "Add regime segmentation, spread shocks, gap shocks, latency shocks and Monte Carlo trade-order tests.",
            "Report worst-case and distributional outcomes, not only averages.", 10)
        add("BOT LAB", "Overfit & Leakage Gate", "Optimization can produce excellent but non-transferable results.",
            "Detect look-ahead leakage, repainting, parameter instability and excessive degrees of freedom.",
            "Any detected leakage blocks promotion.", 10)
        add("BOT LAB", "Champion/Challenger", "The best strategy should be compared against a live incumbent rather than judged in isolation.",
            "Maintain champion and challenger records with identical test protocols and rollback criteria.",
            "Challenger must beat the incumbent on risk-adjusted and robustness metrics.", 9)
        add("BOT LAB", "AI Strategy Health Monitor", "A deployed strategy needs continuous monitoring after validation.",
            "Monitor drift, drawdown, fill quality, spread, expectancy and regime mismatch with alert thresholds.",
            "Trigger REVIEW/PAUSE recommendations before degradation becomes catastrophic.", 10)

        val persisted = proposals.map { save(it) }
        val score = 7.4 + (persisted.count { it.priority >= 10 } * 0.12).coerceAtMost(1.5)
        return Audit(
            score = score.coerceAtMost(9.9),
            proposals = persisted.sortedByDescending { it.priority },
            strengths = listOf(
                "Deterministic trading tools exist.",
                "Multi-source research and evidence mesh exist.",
                "Human approval boundary exists.",
                "Broker execution remains fail-closed."
            ),
            warnings = listOf(
                "9.9/10 is a target, not a claim until the complete validation pipeline is green.",
                "AI must never infer execution success from Android queue acceptance.",
                "Source quantity must never be treated as independent confirmation."
            )
        )
    }

    fun pending(): List<Proposal> = readAll().filter { it.status == Status.PROPOSED }.sortedByDescending { it.priority }

    fun decide(id: String, status: Status): Proposal? {
        val all = readAll().toMutableList()
        val index = all.indexOfFirst { it.id == id }
        if (index < 0) return null
        val updated = all[index].copy(status = status)
        all[index] = updated
        writeAll(all)
        return updated
    }

    private fun save(proposal: Proposal): Proposal {
        val all = readAll().filterNot { it.id == proposal.id || (it.title == proposal.title && it.status == Status.PROPOSED) }.toMutableList()
        all += proposal
        writeAll(all.takeLast(MAX_ITEMS))
        return proposal
    }

    private fun readAll(): List<Proposal> = runCatching {
        val array = JSONArray(prefs.getString(KEY_ITEMS, "[]"))
        (0 until array.length()).mapNotNull { decode(array.getJSONObject(it)) }
    }.getOrDefault(emptyList())

    private fun writeAll(items: List<Proposal>) {
        val array = JSONArray()
        items.forEach { p ->
            array.put(JSONObject().apply {
                put("id", p.id); put("area", p.area); put("title", p.title); put("reason", p.reason)
                put("action", p.action); put("evidence", p.evidenceRequirement); put("priority", p.priority)
                put("status", p.status.name); put("createdAtMs", p.createdAtMs)
            })
        }
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private fun decode(o: JSONObject): Proposal? = runCatching {
        Proposal(o.getString("id"), o.getString("area"), o.getString("title"), o.getString("reason"),
            o.getString("action"), o.getString("evidence"), o.getInt("priority"),
            Status.valueOf(o.getString("status")), o.getLong("createdAtMs"))
    }.getOrNull()

    companion object {
        private const val PREFS = "amar_ai_self_improvement_v1"
        private const val KEY_ITEMS = "items"
        private const val MAX_ITEMS = 100
    }
}
