package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.personal.gridbot.amaros.chart.AmarTimeframe
import kotlinx.coroutines.delay

/** Fixed Bot-Lab context shared by every bot, strategy and interface. */
@Composable
fun AmarBotLabGlobalContext() {
    var remaining by remember { mutableStateOf(AmarTradingTimeframeContext.selected.remainingMillis()) }

    LaunchedEffect(AmarTradingTimeframeContext.selected) {
        while (true) {
            remaining = AmarTradingTimeframeContext.selected.remainingMillis()
            delay(1_000)
        }
    }

    Column(
        Modifier.fillMaxWidth().background(Color(0xFF06121B)).padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        val selected = AmarTradingTimeframeContext.selected
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ثوابت مختبر البوت", color = Color(0xFFB9F8FF), fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text(
                    "الفريم ${selected.shortLabel} • متبقي ${formatTimeframeRemaining(remaining)} • مشترك مع كل البوتات والاستراتيجيات والواجهات",
                    color = Color(0xFF18F2A4), fontSize = 9.sp, fontWeight = FontWeight.Bold
                )
            }
            Text("حالة السوق: موحدة", color = Color(0xFFFFC84D), fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(5.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AmarTimeframe.entries.forEach { timeframe ->
                val active = timeframe == selected
                Box(
                    Modifier.height(32.dp)
                        .background(if (active) Color(0xFF24E8FF) else Color(0xFF102533), RoundedCornerShape(9.dp))
                        .border(1.dp, if (active) Color(0xFF8DFAFF) else Color(0xFF1C4657), RoundedCornerShape(9.dp))
                        .clickable { AmarTradingTimeframeContext.selected = timeframe }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(timeframe.shortLabel, color = if (active) Color.Black else Color(0xFFEAFBFF), fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        AmarSharedMarketStatus()
    }
}
