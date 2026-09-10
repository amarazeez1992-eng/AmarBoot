package com.personal.gridbot.amaros.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.personal.gridbot.amaros.broker.AmarMt5LiveMarketDataProvider
import kotlinx.coroutines.delay

/** B32: binds the real MT5 provider to the chart without fabricating candles. */
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
    var timeframe by remember { mutableStateOf(AmarTimeframe.M1) }
    LaunchedEffect(symbol, timeframe, provider, refreshMs) {
        while (true) {
            runCatching { provider.refresh(symbol, timeframe) }
            delay(refreshMs.coerceAtLeast(250L))
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
