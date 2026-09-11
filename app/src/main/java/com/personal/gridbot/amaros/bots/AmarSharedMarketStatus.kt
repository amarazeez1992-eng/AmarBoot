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
import com.personal.gridbot.amaros.chart.AmarTimeframe
import kotlinx.coroutines.delay

/**
 * حالة سوق موحدة لجميع واجهات Bot Lab (A/B/C/D والمستقبلية).
 * هذه الطبقة تعرض الحالة المشتركة فقط؛ لا تنشئ أسعاراً أو إشارات وهمية.
 * مصدر البيانات الحقيقي سيُوصل لاحقاً عبر طبقة السوق/MT5 المشتركة.
 */
@Composable
fun AmarSharedMarketStatus() {
    val timeframe = AmarTradingTimeframeContext.selected
    var remaining by remember(timeframe) { mutableStateOf(timeframe.remainingMillis()) }

    LaunchedEffect(timeframe) {
        while (true) {
            remaining = timeframe.remainingMillis()
            delay(1_000)
        }
    }

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
                Text("MT5 • بانتظار المصدر", color = Color(0xFFFFC84D), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MarketStateCell("شراء", "—", Color(0xFF18F2A4), Modifier.weight(1f))
                MarketStateCell("محايد", "—", Color(0xFFFFC84D), Modifier.weight(1f))
                MarketStateCell("بيع", "—", Color(0xFFFF4F78), Modifier.weight(1f))
                MarketStateCell("القوة", "—", Color(0xFF8DFAFF), Modifier.weight(1f))
            }

            Text(
                "الحالة نفسها مشتركة بين A وB وC وD وجميع الواجهات المستقبلية. لا تُعرض أرقام أو إشارات قبل وصول بيانات السوق الحقيقية.",
                color = Color(0xFF8CA9B5),
                fontSize = 8.sp
            )
        }
    }
}

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
