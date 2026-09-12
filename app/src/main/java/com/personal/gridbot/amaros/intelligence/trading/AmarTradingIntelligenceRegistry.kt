package com.personal.gridbot.amaros.intelligence.trading

/** AMAR Trading Intelligence registry: sources, research engines and governance only. */
object AmarTradingIntelligenceRegistry {
    data class Source(val name: String, val tier: Int, val role: String, val url: String, val openSource: Boolean = false)

    val officialSources: List<Source> = listOf(
        Source("CFTC Data", 1, "COT, derivatives positioning and regulatory data", "https://www.cftc.gov/data"),
        Source("SEC", 1, "filings, disclosures and market regulation", "https://www.sec.gov/"),
        Source("Federal Reserve", 1, "rates, liquidity and macro conditions", "https://www.federalreserve.gov/"),
        Source("BIS", 1, "FX, banking, liquidity and macro research", "https://www.bis.org/"),
        Source("CME Group", 1, "futures, options and market structure", "https://www.cmegroup.com/"),
        Source("IMF", 1, "global macro research and datasets", "https://www.imf.org/"),
        Source("TradingView Pine Script Manual", 1, "Pine language, indicators, strategies, data, repainting and alerts", "https://www.tradingview.com/pine-script-docs/"),
        Source("TradingView Community Scripts", 1, "public indicators, strategies and libraries discovery", "https://www.tradingview.com/scripts/"),
        Source("TradingView Public Libraries", 1, "open public Pine libraries", "https://www.tradingview.com/pine-script-docs/concepts/libraries/"),
        Source("TradingView Webhooks", 1, "alert-to-external-system transport", "https://www.tradingview.com/support/solutions/43000529348/"),
        Source("LuxAlgo", 2, "trading concepts, Quant and charting research", "https://www.luxalgo.com/"),
        Source("LuxAlgo Library", 2, "technical-analysis concept encyclopedia", "https://www.luxalgo.com/library/"),
        Source("LuxAlgo Docs", 2, "platform and engineering documentation", "https://docs.luxalgo.com/"),
        Source("LuxAlgo GitHub", 2, "official open-source engineering", "https://github.com/LuxAlgo", true),
        Source("LuxAlgo Vela", 2, "open-source web charting engine", "https://github.com/LuxAlgo/Vela", true),
        Source("LuxAlgo PineTS", 2, "open-source Pine runtime/transpiler", "https://github.com/LuxAlgo/PineTS", true),
        Source("LuxAlgo PineTS CLI", 2, "agent-friendly Pine execution CLI", "https://github.com/LuxAlgo/pinets-cli", true),
        Source("LuxAlgo MCP Server", 2, "official MCP concept access", "https://github.com/LuxAlgo/luxalgo-mcp-server", true),
        Source("GitHub", 3, "implementation discovery and OSS validation", "https://github.com/", true),
        Source("Wikipedia", 4, "definitions only; never proof", "https://www.wikipedia.org/")
    )

    val intelligenceEngines: List<String> = listOf(
        "Trading Knowledge Library", "Market Regime Engine", "Strategy Compiler", "Deterministic Backtest Engine",
        "Walk-Forward / OOS Engine", "Leakage & Reproducibility Guard", "Trade-Level Stress Testing Engine",
        "Strategy Quality Engine", "Strategy Validation Engine", "Mutation Lab", "Evolution Governance",
        "Champion / Challenger", "Counterfactual Analysis", "Evidence & Conflict Engine", "Knowledge Graph",
        "Strategy DNA / Lineage", "Decision Memory", "Portfolio Exposure Intelligence", "Drift Detection",
        "Uncertainty / Confidence Engine", "Research Provenance Engine", "TradingView Adapter Contract",
        "TradingView Public Script Discovery", "LuxAlgo Concept Discovery", "Chrome Research Bridge",
        "MT5 Command Authority Boundary"
    )

    fun catalogText(): String = officialSources.joinToString(" | ") {
        "${it.name}[T${it.tier};${it.role};${if (it.openSource) "OSS" else "official"};${it.url}]"
    }

    fun governance(): String = """
AMAR TRADING RESEARCH GOVERNANCE:
1. Tier-1 primary institutions/exchanges/regulators outrank marketing, community and anonymous claims.
2. TradingView public scripts are discovery inputs, not automatically trusted or copied.
3. Only source code whose publication/license permits reuse may enter the internal implementation library.
4. Protected, invite-only, paid proprietary or credentialed content is never copied into AMAR.
5. LuxAlgo is a high-value engineering/reference source, not proof of profitability.
6. Vela is Apache-2.0; PineTS and its addons have separate license obligations. Keep attribution and license boundaries explicit.
7. TradingView is a chart/data/alert boundary, not broker authority.
8. Pine/PineTS results require independent AMAR datasets, OOS validation and stress testing.
9. Every imported research item needs provenance: source, URL, version/date, license, methodology and AMAR validation state.
10. No source can override measured OOS evidence, risk gates or human approval.
11. Findings are labelled SOURCE, VERIFIED, HYPOTHESIS or INFERENCE.
12. AMAR AI may research, compare, mutate and optimize candidates; it cannot silently adopt a strategy.
""".trimIndent()

    fun recommendedResearchPlan(query: String): String = buildString {
        appendLine("Research target: $query")
        appendLine("Priority official sources:")
        officialSources.filter { it.tier <= 2 }.forEach { appendLine("- ${it.name}: ${it.url}") }
        appendLine("Then search the internal taxonomy, TradingView public scripts and LuxAlgo Library; preserve provenance and licenses.")
        appendLine("Required report: claim -> source -> date/version -> methodology -> dataset -> assumptions -> limitations -> AMAR validation -> confidence -> next test.")
    }
}
