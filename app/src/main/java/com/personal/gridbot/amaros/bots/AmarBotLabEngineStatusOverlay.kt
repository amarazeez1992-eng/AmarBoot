package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.ai.AmarAiSelfImprovementEngine
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingIntelligenceRegistry
import com.personal.gridbot.amaros.runtime.AmarBotOperationalEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AmarBotLabEngineStatusOverlay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var aiScore by remember { mutableStateOf<Double?>(null) }
    var proposalCount by remember { mutableStateOf(0) }
    var botCount by remember { mutableStateOf(0) }
    var engineError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            val audit = withContext(Dispatchers.Default) { AmarAiSelfImprovementEngine(context).audit() }
            aiScore = audit.score
            proposalCount = audit.proposals.count { it.status == AmarAiSelfImprovementEngine.Status.PROPOSED }
            withContext(Dispatchers.IO) { AmarBotOperationalEngine(context).ensureBotCatalog() }
            botCount = AmarBotVaultRepository(context).load().size
        }.onFailure { engineError = it.message ?: it.javaClass.simpleName }
    }

    Column(modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 3.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xE60A1722), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1A3E4D), RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("⚙", color = Color(0xFF1DE5FF), fontSize = 15.sp)
            Text("المحركات", color = Color(0xFFE9FBFF), fontWeight = FontWeight.Black, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("AI ${aiScore?.let { "%.1f".format(it) } ?: "…"}/9.9", color = Color(0xFF00E6A0), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(if (expanded) "▲" else "▼", color = Color(0xFF7896A5), fontSize = 9.sp)
        }

        if (expanded) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xF20A1722), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1A3E4D), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                EngineLine("AI Supervisor", "مرتبط • ${proposalCount} اقتراح قيد المراجعة", Color(0xFF1DE5FF))
                EngineLine("Trading Intelligence", "${AmarTradingIntelligenceRegistry.intelligenceEngines.size} محرك", Color(0xFF5C9DFF))
                EngineLine("Research Sources", "${AmarTradingIntelligenceRegistry.officialSources.size} مصدر مسجل", Color(0xFFFFC84A))
                EngineLine("Bot Operational", "${botCount}/10 بوت مفهرس", Color(0xFF00E6A0))
                EngineLine("Execution", "Fail-Closed • لا تنفيذ وسيط من الهاتف", Color(0xFFFF4F67))
                engineError?.let { Text("حالة المحرك: $it", color = Color(0xFFFF4F67), fontSize = 8.sp) }
            }
        }
    }
}

@Composable
private fun EngineLine(label: String, value: String, accent: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("●", color = accent, fontSize = 8.sp)
        Text(label, color = Color(0xFF7896A5), fontSize = 8.sp, modifier = Modifier.weight(.38f))
        Text(value, color = Color(0xFFE9FBFF), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(.62f))
    }
}
