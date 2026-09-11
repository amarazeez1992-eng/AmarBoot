package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Shared market-state view for A/B/C/D and future interfaces.
 * All displayed market values come from AmarMarketStateStore.
 */
@Composable
fun AmarSharedMarketStatus() {
    val timeframe = AmarTradingTimeframeContext.selected
    val market = AmarMarketStateStore.snapshot
    var remaining by remember(timeframe) { mutableStateOf(timeframe.remainingMillis()) }

    LaunchedEffect(timeframe) {
        while (true) {
            remaining = timeframe.remainingMillis()
            delay(1_000)
        }
    }

    val sourceLabel = when (market.source) {
        AmarMarketSource.MT5 -> "MT5"
        AmarMarketSource.TRADING_VIEW -> "TradingView"
        AmarMarketSource.SIMULATION -> "Simulation"
        AmarMarketSource.NONE -> "بانتظار المصدر"
    }
    val qualityLabel = when (market.quality) {
        AmarMarketDataQuality.LIVE -> "مباشر"
        AmarMarketDataQuality.DELAYED -> "متأخر"
        AmarMarketDataQuality.STALE -> "قديم"
        AmarMarketDataQuality.UNAVAILABLE -> "غير متاح"
    }
    val directionLabel = when (market.direction) {
        AmarMarketDirection.BUY -> "شراء"
        AmarMarketDirection.NEUTRAL -> "محايد"
        AmarMarketDirection.SELL -> "بيع"
        AmarMarketDirection.UNKNOWN -> "—"
    }
    val strengthLabel = market.strength?.let { "%.1f".format(it) } ?: "—"

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF081721))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("حالة السوق", color = Color(0xFFE9FBFF), fontSize = 15.sp, fontWeight = FontWeight.Black)
                    Text(
                        "حالة موحدة • ${timeframe.shortLabel} • الشمعة التالية ${formatTimeframeRemaining(remaining)}",
                        color = Color(0xFF24E8FF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("$sourceLabel • $qualityLabel", color = Color(0xFFFFC84D), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MarketStateCell("الاتجاه", directionLabel, Color(0xFF18F2A4), Modifier.weight(1f))
                MarketStateCell("Bid", market.bid?.let(::formatMarketNumber) ?: "—", Color(0xFF8DFAFF), Modifier.weight(1f))
                MarketStateCell("Ask", market.ask?.let(::formatMarketNumber) ?: "—", Color(0xFF24E8FF), Modifier.weight(1f))
                MarketStateCell("القوة", strengthLabel, Color(0xFFFFC84D), Modifier.weight(1f))
            }

            Text(
                if (market.hasPrice) {
                    "${market.symbol.ifBlank { "السوق" }} • Spread ${market.spread?.let(::formatMarketNumber) ?: "—"}"
                } else {
                    "الحالة نفسها مشتركة بين A وB وC وD وجميع الواجهات المستقبلية. لا تُعرض بيانات قبل وصول مصدر سوق موثوق."
                },
                color = Color(0xFF8CA9B5),
                fontSize = 8.sp
            )
        }
    }
}

private fun formatMarketNumber(value: Double): String = "%.5f".format(value)

@Composable
private fun MarketStateCell(title: String, value: String, accent: Color, modifier: Modifier) {
    Column(
        modifier
            .background(Color(0xFF0D202B), RoundedCornerShape(11.dp))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(11.dp))
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = accent, fontSize = 8.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color(0xFFE9FBFF), fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}
