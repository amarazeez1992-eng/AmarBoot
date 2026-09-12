package com.personal.gridbot.amaros.intelligence.trading

/**
 * AMAR Trading Intelligence registry.
 * Source/governance registry only; it is never an execution authority.
 */
object AmarTradingIntelligenceRegistry {
    data class Source(val name: String, val tier: Int, val role: String, val url: String, val openSource: Boolean = false)

    val officialSources: List<Source> = listOf(
        Source("CFTC Data", 1, "COT, derivatives positioning and regulatory data", "https://www.cftc.gov/data"),
        Source("SEC", 1, "filings, disclosures and market regulation", "https://www.sec.gov/"),
        Source("Federal Reserve", 1, "rates, liquidity and macro conditions", "https://www.federalreserve.gov/"),
        Source("BIS", 1, "FX, banking, liquidity and macro research", "https://www.bis.org/"),
        Source("CME Group", 1, "futures, options and market structure", "https://www.cmegroup.com/"),
        Source("IMF", 1, "global macroeconomic research and datasets", "https://www.imf.org/"),
        Source("TradingView Pine Docs", 1, "Pine execution model, strategies and alerts", "https://www.tradingview.com/pine-script-docs/"),
        Source("TradingView Webhooks", 1, "external alert transport contract", "https://www.tradingview.com/support/solutions/43000529348/"),
        Source("LuxAlgo", 2, "trading concepts, Quant and charting research", "https://www.luxalgo.com/"),
        Source("LuxAlgo Docs", 2, "platform and research documentation", "https://docs.luxalgo.com/"),
        Source("LuxAlgo GitHub", 2, "official open-source engineering", "https://github.com/LuxAlgo", true),
        Source("LuxAlgo Vela", 2, "open-source charting engine", "https://github.com/LuxAlgo/Vela", true),
        Source("LuxAlgo PineTS", 2, "open-source Pine runtime/transpiler", "https://github.com/LuxAlgo/PineTS", true),
        Source("LuxAlgo PineTS CLI", 2, "agent-friendly Pine execution CLI", "https://github.com/LuxAlgo/pinets-cli", true),
        Source("LuxAlgo MCP Server", 2, "official MCP access to trading concepts", "https://github.com/LuxAlgo/luxalgo-mcp-server", true),
        Source("GitHub", 3, "implementation discovery and open-source validation", "https://github.com/", true),
        Source("Wikipedia", 4, "definitions only; never proof", "https://www.wikipedia.org/")
    )

    val intelligenceEngines: List<String> = listOf(
        "Market Regime Engine", "Strategy Compiler", "Deterministic Backtest Engine",
        "Walk-Forward / OOS Engine", "Leakage & Reproducibility Guard", "Stress Testing Engine",
        "Strategy Quality Engine", "Strategy Validation Engine", "Mutation Lab", "Evolution Governance",
        "Champion / Challenger", "Counterfactual Analysis", "Evidence & Conflict Engine", "Knowledge Graph",
        "Strategy DNA / Lineage", "Decision Memory", "Portfolio Exposure Intelligence", "Drift Detection",
        "Uncertainty / Confidence Engine", "TradingView Adapter Contract", "MT5 Command Authority Boundary"
    )

    fun catalogText(): String = officialSources.joinToString(" | ") {
        "${it.name}[T${it.tier};${it.role};${if (it.openSource) "OSS" else "official"};${it.url}]"
    }

    fun governance(): String = """
AMAR TRADING RESEARCH GOVERNANCE:
1. Tier 1 primary institutions/exchanges/regulators outrank marketing, community and anonymous claims.
2. LuxAlgo is a high-value engineering/reference source, not proof that a strategy is profitable.
3. Open-source LuxAlgo projects may be inspected, adapted and tested subject to their individual licenses.
4. Never copy proprietary invite-only scripts, paid-only source, credentials, or restricted content.
5. TradingView is a chart/data/alert boundary, not an execution authority.
6. Future TradingView webhooks must be authenticated, replay-protected, idempotent, time-bounded and validated.
7. Pine/PineTS results must be independently validated against AMAR datasets before adoption.
8. No source can override measured OOS evidence, risk gates or human approval.
9. Findings are labelled SOURCE, measured results VERIFIED, hypotheses HYPOTHESIS.
10. AMAR AI may propose, compare and optimize; it cannot silently adopt a new strategy.
""".trimIndent()

    fun recommendedResearchPlan(query: String): String = buildString {
        appendLine("Research target: $query")
        appendLine("Priority official sources:")
        officialSources.filter { it.tier <= 2 }.forEach { appendLine("- ${it.name}: ${it.url}") }
        appendLine("Then inspect open-source LuxAlgo/GitHub implementations and independently validate.")
        appendLine("Required output: claim -> source -> date -> methodology -> data period -> assumptions -> limitations -> AMAR validation status.")
    }
}
