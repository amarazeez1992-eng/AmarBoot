package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF172033)
private val Muted = Color(0xFF718096)
private val Green = Color(0xFF12B76A)
private val Red = Color(0xFFE5484D)
private val Yellow = Color(0xFFF4B400)
private val Border = Color(0xFFE1E7F0)
private val Blue = Color(0xFF246BFE)

/**
 * Stable B30 market-status presentation layer.
 * Live MT5 data is deliberately not fabricated here.
 * The real bridge provider can be attached without changing this UI contract.
 */
@Composable
fun Bot1MarketStatus() {
    val frames = listOf(
        "4 ساعات" to "H4",
        "ساعة" to "H1",
        "30 دقيقة" to "M30",
        "15 دقيقة" to "M15",
        "5 دقائق" to "M5",
        "1 دقيقة" to "M1",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "حالة السوق",
                color = Ink,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "قراءة متعددة الفريمات — بانتظار بيانات MT5 الحقيقية",
                color = Blue,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            frames.forEach { (arabic, _) ->
                WaitingFrameRow(arabic)
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WaitingSummary("شراء", Green, Modifier.weight(1f))
                WaitingSummary("حياد", Yellow, Modifier.weight(1f))
                WaitingSummary("بيع", Red, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WaitingFrameRow(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(text = "بانتظار البيانات", color = Muted, fontSize = 10.sp)
        Spacer(modifier = Modifier.padding(start = 7.dp))
        Text(text = "—", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WaitingSummary(label: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
            .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "—", color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Muted, fontSize = 9.sp)
    }
}
