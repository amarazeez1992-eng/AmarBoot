package com.personal.gridbot.amaros.ai.core

/**
 * AI-facing MT5 office. It exposes the existing MT5 code/bot ecosystem as references for
 * inspection and research without copying or modifying the execution engine.
 */
object AmarAiMt5Office {
    data class Asset(val id: String, val name: String, val type: String, val path: String, val role: String, val mutable: Boolean)

    val assets = listOf(
        Asset("mt5.ea.grid.v2", "Grid_Martingale_Basket_v2", "EA", "mt5/Experts/Grid_Martingale_Basket_v2.mq5", "protected execution reference; inspect only", false),
        Asset("mt5.bridge", "MT5 Bridge", "transport", "bridge/", "future command/ACK/verify/reconcile boundary", false),
        Asset("mt5.grid.engine", "Grid Planning Engine", "engine", "app/.../amaros/bots/AmarGridPlanningEngine.kt", "deterministic grid planning", false),
        Asset("mt5.bot.lab", "Bot Lab", "workspace", "app/.../amaros/bots/", "bot discovery and command planning", false),
        Asset("mt5.orders", "Order Manager", "runtime", "app/.../amaros/bots/", "pending-order state reference", false),
        Asset("mt5.positions", "Position Manager", "runtime", "app/.../amaros/bots/", "position state reference", false)
    )

    val botSources = listOf(
        "Freqtrade|https://github.com/freqtrade/freqtrade",
        "Freqtrade Strategies|https://github.com/freqtrade/freqtrade-strategies",
        "Hummingbot|https://github.com/hummingbot/hummingbot",
        "QuantConnect LEAN|https://github.com/QuantConnect/Lean",
        "OctoBot|https://github.com/Drakkar-Software/OctoBot",
        "Superalgos|https://github.com/Superalgos/Superalgos",
        "NostalgiaForInfinity|https://github.com/iterativv/NostalgiaForInfinity"
    )

    fun catalogText(): String = buildString {
        appendLine("MT5 OFFICE: ${assets.size} internal references")
        assets.forEach { appendLine("${it.id}|${it.name}|${it.type}|${it.path}|mutable=${it.mutable}|${it.role}") }
        appendLine("OPEN-SOURCE BOT OFFICE")
        botSources.forEach(::appendLine)
        append("RULE: inspect -> license/provenance -> test -> user approval; never silently modify the protected EA.")
    }

    fun search(query: String): List<Asset> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return assets
        return assets.filter { (it.name + " " + it.type + " " + it.role + " " + it.path).lowercase().contains(q) }
    }
}
