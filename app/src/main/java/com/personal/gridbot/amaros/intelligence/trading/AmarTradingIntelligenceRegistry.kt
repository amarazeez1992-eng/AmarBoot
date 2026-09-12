package com.personal.gridbot.amaros.intelligence.trading

/** AMAR Trading Intelligence registry: global research authority, source hierarchy, engines and governance. */
object AmarTradingIntelligenceRegistry {
    data class Source(val name: String, val tier: Int, val role: String, val url: String, val openSource: Boolean = false)

    val officialSources: List<Source> = listOf(
        Source("CFTC Data", 1, "COT, derivatives positioning and regulatory data", "https://www.cftc.gov/data"),
        Source("SEC", 1, "filings, disclosures and market regulation", "https://www.sec.gov/"),
        Source("Federal Reserve", 1, "rates, liquidity and macro conditions", "https://www.federalreserve.gov/"),
        Source("BIS", 1, "FX, banking, liquidity and macro research", "https://www.bis.org/"),
        Source("CME Group", 1, "futures, options and market structure", "https://www.cmegroup.com/"),
        Source("IMF", 1, "global macro research and datasets", "https://www.imf.org/"),
        Source("World Bank", 1, "global macroeconomic and development datasets", "https://www.worldbank.org/"),
        Source("OECD", 1, "macro, economic indicators and policy datasets", "https://www.oecd.org/"),
        Source("ECB", 1, "euro-area monetary policy and financial statistics", "https://www.ecb.europa.eu/"),
        Source("Bank of England", 1, "UK monetary policy and financial data", "https://www.bankofengland.co.uk/"),
        Source("BLS", 1, "US employment, inflation and labor statistics", "https://www.bls.gov/"),
        Source("FRED", 1, "US macroeconomic time series and economic data", "https://fred.stlouisfed.org/"),
        Source("EIA", 1, "energy, oil, gas and macro energy data", "https://www.eia.gov/"),
        Source("Nasdaq", 1, "market information and exchange research", "https://www.nasdaq.com/"),
        Source("NYSE", 1, "exchange and market structure reference", "https://www.nyse.com/"),
        Source("ICE", 1, "global futures, commodities and market infrastructure", "https://www.ice.com/"),
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
        Source("QuantConnect LEAN", 3, "open-source institutional-caliber research and execution engine", "https://github.com/QuantConnect/Lean", true),
        Source("Freqtrade", 3, "open-source bot, backtesting and strategy framework", "https://github.com/freqtrade/freqtrade", true),
        Source("Hummingbot", 3, "open-source market making and connector framework", "https://github.com/hummingbot/hummingbot", true),
        Source("vectorbt", 3, "quantitative research and vectorized backtesting", "https://github.com/polakowo/vectorbt", true),
        Source("backtesting.py", 3, "strategy backtesting and research", "https://github.com/kernc/backtesting.py", true),
        Source("Backtrader", 3, "algorithmic trading and backtesting framework", "https://github.com/mementum/backtrader", true),
        Source("HFTBacktest", 3, "high-frequency trading research and execution simulation", "https://github.com/nkaz001/hftbacktest", true),
        Source("CCXT", 3, "exchange connectivity and market data abstraction", "https://github.com/ccxt/ccxt", true),
        Source("OctoBot", 3, "open trading automation platform", "https://github.com/Drakkar-Software/OctoBot", true),
        Source("Superalgos", 3, "open trading automation and research platform", "https://github.com/Superalgos/Superalgos", true),
        Source("VeighNa / vn.py", 3, "open quantitative trading framework", "https://github.com/vnpy/vnpy", true),
        Source("TA-Lib", 3, "technical analysis indicator library", "https://github.com/TA-Lib/ta-lib", true),
        Source("OpenPineScript", 3, "open Pine language implementation research", "https://github.com/be-thomas/OpenPineScript", true),
        Source("Wikipedia", 4, "definitions only; never proof", "https://www.wikipedia.org/")
    )

    val intelligenceEngines: List<String> = listOf(
        "Global Research Authority", "Multi-Source Research Mesh", "Primary-Source Priority Router",
        "Source Independence Scoring", "Evidence Convergence Engine", "Semantic Conflict Detection",
        "Research Provenance Engine", "Freshness / Recency Gate", "Citation Completeness Gate",
        "Trading Knowledge Library", "Market Regime Engine", "Strategy Compiler", "Deterministic Backtest Engine",
        "Walk-Forward / OOS Engine", "Leakage & Reproducibility Guard", "Trade-Level Stress Testing Engine",
        "Strategy Quality Engine", "Strategy Validation Engine", "Mutation Lab", "Evolution Governance",
        "Champion / Challenger", "Counterfactual Analysis", "Knowledge Graph", "Strategy DNA / Lineage",
        "Decision Memory", "Portfolio Exposure Intelligence", "Drift Detection", "Uncertainty / Confidence Engine",
        "TradingView Public Script Discovery", "Open-Source Bot Discovery", "License / Provenance Gate",
        "Chrome Research Bridge", "MT5 Command Authority Boundary"
    )

    fun catalogText(): String = officialSources.joinToString(" | ") {
        "${it.name}[T${it.tier};${it.role};${if (it.openSource) "OSS" else "official"};${it.url}]"
    }

    fun governance(): String = """
AMAR GLOBAL TRADING RESEARCH AUTHORITY:
1. Research broadly, but rank evidence: Tier-1 primary institutions/exchanges/regulators > Tier-2 established technical/reference ecosystems > Tier-3 open-source implementations > Tier-4 general definitions.
2. Search many independent channels for important claims; never stop at the first useful result when the claim materially affects a strategy or decision.
3. Independence matters: ten pages repeating the same source do not equal ten independent confirmations.
4. A source-convergence score is evidence quality only. It is NEVER a profitability probability.
5. High confidence requires multiple independent sources plus reproducible AMAR validation where a numerical trading claim is involved.
6. Every important claim should carry source, URL, date/version when available, methodology, assumptions, limitations and validation state.
7. Fresh information must be separated from historical reference material. Prefer recent primary data when the question is time-sensitive.
8. Conflicting evidence must be surfaced, not averaged away. State what conflicts and why the conflict may exist.
9. TradingView public scripts are discovery inputs, not automatically trusted or copied. Public libraries are reusable only within their publication/license rules.
10. Protected, invite-only, paid proprietary or credentialed content is never copied into AMAR.
11. GitHub is a discovery and code-evidence source. Public visibility does not mean unrestricted reuse; inspect repository license and provenance before reuse.
12. Open-source bots and engines can be inspected for architecture, strategy logic, testing methods and reusable concepts, subject to their licenses.
13. LuxAlgo is a high-value engineering/reference ecosystem, never proof of profitability. Respect Vela, PineTS and addon licenses and attribution.
14. Pine/PineTS/TradingView results require independent AMAR datasets, OOS validation and stress testing before being treated as VERIFIED.
15. Findings are labelled SOURCE, VERIFIED, HYPOTHESIS or INFERENCE.
16. Numeric success claims require measured reproducible data. No percentage may be invented from source convergence.
17. Strategy lifecycle: Idea -> Draft -> Discuss -> Evaluate -> Test -> OOS -> Stress -> Compare -> Risk Gate -> User Approval -> Adopt.
18. AMAR AI may research, compare, critique, optimize and prepare candidates. It cannot silently adopt, publish, download proprietary content, or execute broker trades.
19. Current Android build remains advisory/local for broker execution until the MT5 command authority and ACK/verify/reconcile path is enabled.
20. Emergency stop and human approval always outrank AI authority.
""".trimIndent()

    fun recommendedResearchPlan(query: String): String = buildString {
        appendLine("Research target: $query")
        appendLine("Phase 1 — primary/global sources:")
        officialSources.filter { it.tier == 1 }.forEach { appendLine("- ${it.name}: ${it.url}") }
        appendLine("Phase 2 — established technical/reference ecosystems:")
        officialSources.filter { it.tier == 2 }.forEach { appendLine("- ${it.name}: ${it.url}") }
        appendLine("Phase 3 — open-source implementation and bot discovery:")
        officialSources.filter { it.tier == 3 }.forEach { appendLine("- ${it.name}: ${it.url}") }
        appendLine("Required report: claim -> independent sources -> date/version -> methodology -> dataset -> assumptions -> limitations -> conflict check -> AMAR validation -> confidence -> next test.")
    }

    fun confidencePolicy(): String = """
CONFIDENCE POLICY:
- LOW: one weak/secondary channel, incomplete provenance, or unresolved conflict.
- MODERATE: multiple relevant channels but limited independence or no reproducible OOS evidence.
- HIGH: multiple independent authoritative/technical sources with coherent evidence and complete provenance.
- VERY HIGH: HIGH evidence plus reproducible AMAR OOS/stress validation, adequate sample size, realistic costs and no material leakage/repainting.
- VERIFIED: a measured result reproduced by AMAR with explicit dataset, assumptions, experiment fingerprint and validation window.
No research-source count alone can produce VERY HIGH or VERIFIED confidence for profitability.
""".trimIndent()
}
