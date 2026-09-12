package com.personal.gridbot.amaros.intelligence.trading

import android.content.Context
import com.personal.gridbot.amaros.ai.AmarAiExternalResearch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.security.MessageDigest

/** Fast multi-source research mesh. It indexes sources and aggregates independent evidence; it never invents profitability. */
object AmarTradingSourceMesh {
    data class Source(val id: String, val name: String, val kind: String, val url: String, val reuse: String)
    data class Bot(val name: String, val repo: String, val url: String, val focus: String, val status: String)
    data class Evidence(val source: String, val title: String, val url: String, val excerpt: String, val external: Boolean = true)
    data class Report(
        val query: String,
        val searchedChannels: Int,
        val evidence: List<Evidence>,
        val supportingChannels: Int,
        val conflictChannels: Int,
        val consensusPct: Double,
        val newItems: List<Evidence>,
        val caveat: String
    )

    val sources = listOf(
        Source("tradingview", "TradingView Community Scripts", "discovery", "https://www.tradingview.com/scripts/", "license-check-required"),
        Source("pine", "TradingView Pine Script Docs", "primary", "https://www.tradingview.com/pine-script-docs/", "reference"),
        Source("luxalgo", "LuxAlgo Library", "concept", "https://www.luxalgo.com/library/", "license-check-required"),
        Source("vela", "LuxAlgo Vela", "opensource", "https://github.com/LuxAlgo/Vela", "Apache-2.0-check"),
        Source("pinets", "LuxAlgo PineTS", "opensource", "https://github.com/LuxAlgo/PineTS", "license-check-required"),
        Source("cftc", "CFTC", "primary", "https://www.cftc.gov/data", "reference"),
        Source("sec", "SEC", "primary", "https://www.sec.gov/", "reference"),
        Source("fed", "Federal Reserve", "primary", "https://www.federalreserve.gov/", "reference"),
        Source("bis", "BIS", "primary", "https://www.bis.org/", "reference"),
        Source("cme", "CME Group", "primary", "https://www.cmegroup.com/education.html", "reference"),
        Source("imf", "IMF", "primary", "https://www.imf.org/", "reference"),
        Source("github", "GitHub", "code", "https://github.com/", "per-repository-license"),
        Source("freqtrade", "Freqtrade", "bot", "https://github.com/freqtrade/freqtrade", "GPL-check"),
        Source("freqtrade-strategies", "Freqtrade Strategies", "strategies", "https://github.com/freqtrade/freqtrade-strategies", "license-check"),
        Source("hummingbot", "Hummingbot", "bot", "https://github.com/hummingbot/hummingbot", "license-check"),
        Source("lean", "QuantConnect Lean", "engine", "https://github.com/QuantConnect/Lean", "license-check"),
        Source("vectorbt", "vectorbt", "research", "https://github.com/polakowo/vectorbt", "license-check"),
        Source("backtestingpy", "backtesting.py", "research", "https://github.com/kernc/backtesting.py", "license-check"),
        Source("zipline", "Zipline", "research", "https://github.com/quantopian/zipline", "license-check"),
        Source("zipline-reloaded", "Zipline Reloaded", "research", "https://github.com/stefan-jansen/zipline-reloaded", "license-check"),
        Source("backtrader", "Backtrader", "research", "https://github.com/mementum/backtrader", "license-check"),
        Source("ccxt", "CCXT", "exchange", "https://github.com/ccxt/ccxt", "license-check"),
        Source("octobot", "OctoBot", "bot", "https://github.com/Drakkar-Software/OctoBot", "license-check"),
        Source("octobot-mm", "OctoBot Market Making", "bot", "https://github.com/Drakkar-Software/OctoBot-Market-Making", "license-check"),
        Source("superalgos", "Superalgos", "platform", "https://github.com/Superalgos/Superalgos", "license-check"),
        Source("osengine", "OsEngine", "platform", "https://github.com/AlexWan/OsEngine", "license-check"),
        Source("hftbacktest", "HFTBacktest", "hft-research", "https://github.com/nkaz001/hftbacktest", "license-check"),
        Source("tensortrade", "TensorTrade", "ml-research", "https://github.com/tensortrade-org/tensortrade", "license-check"),
        Source("vnpy", "VeighNa / vn.py", "framework", "https://github.com/vnpy/vnpy", "license-check"),
        Source("talib", "TA-Lib", "indicators", "https://github.com/TA-Lib/ta-lib", "license-check"),
        Source("pandas-ta", "pandas-ta", "indicators", "https://github.com/aarigs/pandas-ta", "license-check"),
        Source("openpine", "OpenPineScript", "language", "https://github.com/be-thomas/OpenPineScript", "license-check"),
        Source("nfi", "NostalgiaForInfinity", "strategy", "https://github.com/iterativv/NostalgiaForInfinity", "license-check")
    )

    val bots = listOf(
        Bot("Freqtrade", "freqtrade/freqtrade", "https://github.com/freqtrade/freqtrade", "crypto strategy engine, backtesting, FreqAI", "discovery"),
        Bot("Freqtrade Strategies", "freqtrade/freqtrade-strategies", "https://github.com/freqtrade/freqtrade-strategies", "strategy collection", "discovery"),
        Bot("Hummingbot", "hummingbot/hummingbot", "https://github.com/hummingbot/hummingbot", "market making, arbitrage, connectors", "discovery"),
        Bot("OctoBot", "Drakkar-Software/OctoBot", "https://github.com/Drakkar-Software/OctoBot", "automated trading platform", "discovery"),
        Bot("Superalgos", "Superalgos/Superalgos", "https://github.com/Superalgos/Superalgos", "trading automation and research", "discovery"),
        Bot("NostalgiaForInfinity", "iterativv/NostalgiaForInfinity", "https://github.com/iterativv/NostalgiaForInfinity", "Freqtrade strategy", "discovery"),
        Bot("OctoBot Market Making", "Drakkar-Software/OctoBot-Market-Making", "https://github.com/Drakkar-Software/OctoBot-Market-Making", "market making", "discovery")
    )

    fun sourceCatalogText(): String = sources.joinToString("\n") { "${it.name}|${it.kind}|${it.url}|reuse=${it.reuse}" }
    fun botCatalogText(): String = bots.joinToString("\n") { "${it.name}|${it.focus}|${it.url}|status=${it.status}" }

    suspend fun research(context: Context?, query: String, external: AmarAiExternalResearch = AmarAiExternalResearch(), maxQueries: Int = 12): Report = coroutineScope {
        require(query.isNotBlank()) { "Research query is required" }
        val q = query.trim()
        val variants = linkedSetOf(
            q,
            "$q trading strategy",
            "$q algorithmic trading",
            "$q backtest",
            "$q risk management",
            "$q market regime",
            "$q open source trading bot",
            "$q GitHub trading bot",
            "$q TradingView Pine Script",
            "$q quantitative research",
            "$q validation walk forward",
            "$q slippage execution"
        ).take(maxQueries)
        val fetched = variants.map { v -> async { runCatching { external.search(v, 6) }.getOrDefault(emptyList()) } }.awaitAll().flatten()
        val evidence = fetched.distinctBy { "${it.source}|${it.url}" }.map { Evidence(it.source, it.title, it.url, it.excerpt) }
        val terms = q.lowercase().split(Regex("\\s+")).filter { it.length >= 3 }
        val supporting = evidence.map { e -> e to terms.count { term -> (e.title + " " + e.excerpt).lowercase().contains(term) } }
            .filter { it.second > 0 }
            .map { it.first.source }.distinct().size
        val conflict = 0
        val consensus = if (supporting == 0) 0.0 else (supporting.toDouble() / maxOf(1, minOf(10, variants.size))) * 100.0
        val fingerprint = sha256(evidence.joinToString("\n") { "${it.source}|${it.title}|${it.url}" })
        val newItems = context?.let { detectNew(it, evidence, fingerprint) } ?: emptyList()
        Report(q, variants.size, evidence, supporting, conflict, consensus.coerceAtMost(100.0), newItems,
            "Consensus measures conceptual/source convergence only. It is NOT a profitability probability. Any success percentage must come from reproducible AMAR backtests/OOS/stress tests.")
    }

    private fun detectNew(context: Context, evidence: List<Evidence>, fingerprint: String): List<Evidence> {
        val prefs = context.getSharedPreferences("amar_trading_discovery", Context.MODE_PRIVATE)
        val previous = prefs.getString("fingerprint", "")
        val known = prefs.getStringSet("known_urls", emptySet()).orEmpty()
        val fresh = evidence.filter { it.url !in known }
        prefs.edit().putString("fingerprint", fingerprint).putStringSet("known_urls", (known + evidence.map { it.url }).takeLast(500).toSet()).apply()
        return if (previous == fingerprint) emptyList() else fresh
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
