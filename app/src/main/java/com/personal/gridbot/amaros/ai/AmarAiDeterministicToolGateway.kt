package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.chart.AmarTimeframe

/** Deterministic evidence tools exposed to the AI agent. No broker writes are performed here. */
object AmarAiDeterministicToolGateway {
    suspend fun execute(tool: String, args: String): String? = when (tool) {
        "inspect_app" -> "APP_INSPECTION|${AmarAiAppInspector.describe()}"
        "engine_market" -> "${AmarAiEngineBinding.market()}"
        "tracking" -> AmarAiEngineBinding.tracking(args.trim().ifBlank { null })
        "candle" -> {
            val parts = args.split(',', ';', '|').map { it.trim() }.filter { it.isNotBlank() }
            val symbol = parts.firstOrNull()?.ifBlank { AmarMarketStateStore.snapshot.symbol }
                ?: AmarMarketStateStore.snapshot.symbol
            val requestedTf = parts.drop(1).firstOrNull()
            val timeframe = requestedTf?.let { value ->
                AmarTimeframe.entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.shortLabel.equals(value, ignoreCase = true) }
            } ?: AmarMarketStateStore.snapshot.timeframe
            AmarAiEngineBinding.candle(symbol, timeframe)
        }
        else -> null
    }
}
