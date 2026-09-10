package com.personal.gridbot.amaros.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.personal.gridbot.amaros.broker.AmarMt5LiveMarketDataProvider
import kotlinx.coroutines.delay

/** B32: binds real MT5 data to the chart without fabricating candles. */
@Composable
fun AmarLiveTradingChartHost(
    symbol: String,
    provider: AmarMt5LiveMarketDataProvider,
    quote: AmarChartQuote = AmarChartQuote(),
    positions: List<AmarChartPosition> = emptyList(),
    pendingOrders: List<AmarChartPendingOrder> = emptyList(),
    history: List<AmarChartHistoryDeal> = emptyList(),
    executionController: AmarChartExecutionController? = null,
    refreshMs: Long = 1_000L,
) {
    LaunchedEffect(symbol, provider, refreshMs) {
        while (true) {
            AmarTimeframe.entries.forEach { timeframe ->
                runCatching { provider.refresh(symbol, timeframe) }
            }
            delay(refreshMs.coerceAtLeast(1_000L))
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
        liveExecutionAuthorized = executionController != null,
    )
}
