package com.personal.gridbot.amaros.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.personal.gridbot.amaros.broker.AmarMt5LiveMarketDataProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** B32: binds real MT5 data without fabricating candles; each timeframe has its own cadence. */
@Composable
fun AmarLiveTradingChartHost(
    symbol: String,
    provider: AmarMt5LiveMarketDataProvider,
    quote: AmarChartQuote = AmarChartQuote(),
    positions: List<AmarChartPosition> = emptyList(),
    pendingOrders: List<AmarChartPendingOrder> = emptyList(),
    history: List<AmarChartHistoryDeal> = emptyList(),
    executionController: AmarChartExecutionController? = null,
    liveExecutionAuthorized: Boolean = false,
    refreshMs: Long = 1_000L,
) {
    LaunchedEffect(symbol, provider, refreshMs) {
        kotlinx.coroutines.coroutineScope {
            AmarTimeframe.entries.forEach { timeframe ->
                launch {
                    val cadence = timeframeCadenceMs(timeframe, refreshMs)
                    while (true) {
                        runCatching { provider.refresh(symbol, timeframe) }
                        delay(cadence)
                    }
                }
            }
        }
    }
    AmarTradingChartScreen(
        symbol = symbol,
        provider = provider,
        quote = quote,
        positions = positions,
        pendingOrders = pendingOrders,
        history = history,
        executionController = executionController,
        liveExecutionAuthorized = liveExecutionAuthorized,
    )
}

private fun timeframeCadenceMs(timeframe: AmarTimeframe, baseMs: Long): Long {
    val base = baseMs.coerceAtLeast(1_000L)
    val multiplier = when (timeframe) {
        AmarTimeframe.M1 -> 1L
        AmarTimeframe.M2, AmarTimeframe.M3, AmarTimeframe.M4, AmarTimeframe.M5 -> 2L
        AmarTimeframe.M6, AmarTimeframe.M10, AmarTimeframe.M12, AmarTimeframe.M15 -> 5L
        AmarTimeframe.M20, AmarTimeframe.M30 -> 10L
        AmarTimeframe.H1, AmarTimeframe.H2, AmarTimeframe.H3, AmarTimeframe.H4 -> 30L
        AmarTimeframe.H6, AmarTimeframe.H8, AmarTimeframe.H12 -> 60L
        AmarTimeframe.D1, AmarTimeframe.W1, AmarTimeframe.MN1 -> 300L
    }
    return (base * multiplier).coerceAtMost(15 * 60 * 1000L)
}
