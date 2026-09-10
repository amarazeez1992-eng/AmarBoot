package com.personal.gridbot.amaros.bots

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MarketInk = Color(0xFF172033)
private val MarketMuted = Color(0xFF718096)
private val MarketGreen = Color(0xFF12B76A)
private val MarketRed = Color(0xFFE5484D)
private val MarketYellow = Color(0xFFF4B400)
private val MarketBlue = Color(0xFF246BFE)
private val MarketBorder = Color(0xFFE1E7F0)

/** واجهة حالة السوق فقط. لا تعرض بيانات مصطنعة ولا تغير استراتيجية البوت. */
@Composable
fun Bot1MarketStatus() {
    val timeframes = remember {
        listOf("4 ساعات", "ساعة", "30 دقيقة", "15 دقيقة", "5 دقائق", "1 دقيقة")
    }
    val transition = rememberInfiniteTransition(label = "marketPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "marketPulseValue"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "حالة السوق",
                        color = MarketInk,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "قراءة متعددة الفريمات لبوت 1",
                        color = MarketBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .alpha(pulse)
                        .background(MarketYellow, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            MarketConnectionBanner()
            Spacer(modifier = Modifier.height(11.dp))
            Text(
                text = "اتجاه الفريمات",
                color = MarketInk,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(7.dp))
            timeframes.forEach { timeframe ->
                MarketTimeframeRow(timeframe)
                Spacer(modifier = Modifier.height(6.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            MarketSummary()
        }
    }
}

@Composable
private fun MarketConnectionBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF8EA), RoundedCornerShape(13.dp))
            .border(1.dp, Color(0xFFF5D98A), RoundedCornerShape(13.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MarketYellow, CircleShape)
        )
        Spacer(modifier = Modifier.size(7.dp))
        Column {
            Text(
                text = "بانتظار بيانات السوق الحقيقية",
                color = Color(0xFF805B12),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "لن يتم عرض نسب مصطنعة قبل ربط مصدر الأسعار.",
                color = MarketMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun MarketTimeframeRow(timeframe: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(13.dp))
            .border(1.dp, MarketBorder, RoundedCornerShape(13.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeframe,
            modifier = Modifier.weight(1f),
            color = MarketInk,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        DirectionBadge(label = "—", color = MarketMuted)
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "—",
            color = MarketMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DirectionBadge(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MarketSummary() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        MarketScoreCard("الشراء", "—", MarketGreen, Modifier.weight(1f))
        MarketScoreCard("الحياد", "—", MarketYellow, Modifier.weight(1f))
        MarketScoreCard("البيع", "—", MarketRed, Modifier.weight(1f))
    }
}

@Composable
private fun MarketScoreCard(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.07f), RoundedCornerShape(13.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(13.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = MarketMuted,
            fontSize = 9.sp
        )
    }
}
