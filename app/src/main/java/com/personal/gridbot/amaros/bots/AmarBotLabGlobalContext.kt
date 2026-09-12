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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.chart.AmarTimeframe
import kotlinx.coroutines.delay

/** Compact shared context for Interface 1: all timeframes, market state, bot/strategy status. */
@Composable
fun AmarBotLabGlobalContext() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember(context) { AmarBotVaultRepository(context) }
    val bots = remember(context) { repo.load() }
    val botCount = bots.size.coerceAtLeast(10)
    val selectedBot = bots.firstOrNull { it.botNumber == AmarBotLabSelectionContext.selectedBot }
    val strategyCount = selectedBot?.strategies?.size?.coerceAtLeast(10) ?: 10
    var remaining by remember { mutableStateOf(AmarTradingTimeframeContext.selected.remainingMillis()) }

    LaunchedEffect(AmarTradingTimeframeContext.selected) {
        while (true) {
            remaining = AmarTradingTimeframeContext.selected.remainingMillis()
            delay(1_000)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1B29), Color(0xFF061018), Color(0xFF091923))), RoundedCornerShape(17.dp))
            .border(1.dp, Color(0xFF1E5163), RoundedCornerShape(17.dp))
            .padding(horizontal = 9.dp, vertical = 8.dp)
    ) {
        val selected = AmarTradingTimeframeContext.selected
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            StatusPill("BOT $botCount", Color(0xFF00E6A0))
            StatusPill("STR $strategyCount", Color(0xFFB14DFF))
            StatusPill("TF ${selected.shortLabel}", Color(0xFF28E7FF))
            Box(Modifier.weight(1f))
            Text("السوق • موحّد", color = Color(0xFFFFC84A), fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            AmarTimeframe.entries.forEach { timeframe ->
                val active = timeframe == selected
                Box(
                    Modifier.height(28.dp)
                        .background(if (active) Color(0xFF28E7FF) else Color(0xFF102533), RoundedCornerShape(8.dp))
                        .border(1.dp, if (active) Color(0xFF9DFAFF) else Color(0xFF21495A), RoundedCornerShape(8.dp))
                        .clickable { AmarTradingTimeframeContext.selected = timeframe }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(timeframe.shortLabel, color = if (active) Color.Black else Color(0xFFEAFBFF), fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("الفريم ${selected.shortLabel} • ${formatTimeframeRemaining(remaining)}", color = Color(0xFF9DEFFF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text("كل الفريمات • كل البوتات • كل الاستراتيجيات", color = Color(0xFF7896A5), fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        AmarSharedMarketStatus()
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Box(
        Modifier
            .background(color.copy(alpha = .13f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = .45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 7.sp, fontWeight = FontWeight.Black)
    }
}
