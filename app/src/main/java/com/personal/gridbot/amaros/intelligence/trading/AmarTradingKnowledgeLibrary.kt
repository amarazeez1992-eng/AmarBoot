package com.personal.gridbot.amaros.intelligence.trading

/**
 * Internal taxonomy/index for trading research. It stores concepts and discovery routes,
 * not a copied dump of third-party proprietary scripts.
 */
object AmarTradingKnowledgeLibrary {
    data class Domain(val name: String, val concepts: List<String>, val discoveryUrls: List<String>)

    val domains = listOf(
        Domain("Trend", listOf("trend structure", "moving averages", "ADX", "Supertrend", "breakout trend", "trend strength"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Momentum", listOf("RSI", "stochastic", "ROC", "CCI", "momentum divergence", "rate of change"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Volatility", listOf("ATR", "Bollinger Bands", "Keltner", "volatility regime", "range expansion", "squeeze"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Volume & Flow", listOf("volume profile", "VWAP", "OBV", "money flow", "order flow", "delta"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Market Structure", listOf("HH HL LH LL", "support resistance", "swing structure", "break of structure", "change of character"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("SMC / ICT", listOf("liquidity", "fair value gap", "order block", "imbalance", "displacement", "sweeps"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Wyckoff", listOf("accumulation", "distribution", "spring", "upthrust", "effort versus result"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Elliott & Harmonics", listOf("Elliott Wave", "ABCD", "Gartley", "Bat", "Butterfly", "harmonic ratios"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Patterns", listOf("candlestick patterns", "chart patterns", "continuation", "reversal", "flags", "triangles"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Levels", listOf("pivot points", "Fibonacci", "previous high low", "daily weekly levels", "anchored VWAP"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Statistics", listOf("expectancy", "profit factor", "drawdown", "distribution", "Monte Carlo", "bootstrap", "Sharpe", "SQN"), listOf("https://www.tradingview.com/pine-script-docs/", "https://www.luxalgo.com/library/")),
        Domain("Machine Learning", listOf("classification", "regime classification", "feature engineering", "cross validation", "data leakage"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Time & Sessions", listOf("session behavior", "London", "New York", "Asia", "kill zones", "day of week", "seasonality"), listOf("https://www.tradingview.com/scripts/", "https://www.luxalgo.com/library/")),
        Domain("Sentiment & Breadth", listOf("market breadth", "sentiment", "COT", "put call", "positioning"), listOf("https://www.cftc.gov/data", "https://www.tradingview.com/scripts/")),
        Domain("Risk & Exits", listOf("position sizing", "R multiple", "ATR stop", "trailing stop", "portfolio heat", "risk of ruin"), listOf("https://www.cmegroup.com/education", "https://www.tradingview.com/scripts/")),
        Domain("Macro & Fundamentals", listOf("rates", "inflation", "employment", "GDP", "central banks", "liquidity", "yield curve"), listOf("https://www.federalreserve.gov/", "https://www.bis.org/", "https://www.imf.org/")),
        Domain("Validation", listOf("backtest", "walk forward", "OOS", "stress test", "slippage", "spread", "reproducibility", "repainting", "look ahead"), listOf("https://www.tradingview.com/pine-script-docs/", "https://www.luxalgo.com/library/"))
    )

    fun search(query: String, limit: Int = 12): List<Domain> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return domains.take(limit)
        val ranked = domains.map { domain ->
            val haystack = (listOf(domain.name) + domain.concepts).joinToString(" ").lowercase()
            val score = q.split(Regex("\\s+")).count { haystack.contains(it) }
            domain to score
        }.filter { it.second > 0 }.sortedByDescending { it.second }
        return ranked.take(limit).map { it.first }
    }

    fun indexText(): String = domains.joinToString("\n") { d -> "${d.name}: ${d.concepts.joinToString(", ")} | ${d.discoveryUrls.joinToString(", ")}" }

    fun governance(): String = """
The AMAR internal library is a research index and taxonomy, not a copied archive of every third-party script.
TradingView public scripts are discoverable through official Community Scripts; only scripts whose source/license permits reuse may be copied into AMAR.
Protected/invite-only/paid proprietary code is never copied or represented as AMAR-owned knowledge.
Every imported implementation receives provenance, license, source URL, version/date when available, and validation status.
""".trimIndent()
}
