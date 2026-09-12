package com.personal.gridbot.amaros.rooms.news

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AmarMarketPulse3DScreen() {
    var tab by remember { mutableStateOf(0) }
    val transition = rememberInfiniteTransition(label = "market3d")
    val angle by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "angle")
    val breathe by transition.animateFloat(.96f, 1.04f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "breathe")
    val floatY by transition.animateFloat(-5f, 5f, infiniteRepeatable(tween(2600, easing = EaseInOut), RepeatMode.Reverse), label = "float-y")
    val tickerX by transition.animateFloat(-14f, 14f, infiniteRepeatable(tween(3200, easing = EaseInOut), RepeatMode.Reverse), label = "ticker-x")
    val pulse by transition.animateFloat(.72f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "pulse")
    val themes = listOf(
        listOf(Color(0xFF00D9FF), Color(0xFF3155FF), Color(0xFF7A35FF)),
        listOf(Color(0xFFFF3CAC), Color(0xFFFF7A3D), Color(0xFFFFC857)),
        listOf(Color(0xFF6DFFB8), Color(0xFF00B8A9), Color(0xFF1D4ED8))
    )
    val labels = listOf("الأخبار", "السوق", "الجلسات")
    val colors = themes[tab]
    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            labels.forEachIndexed { i, label ->
                FilterChip(selected = tab == i, onClick = { tab = i }, label = { Text(label) }, modifier = Modifier.weight(1f))
            }
        }
        Box(Modifier.fillMaxWidth().height(240.dp).background(Brush.linearGradient(colors), RoundedCornerShape(32.dp)).border(1.dp, Color.White.copy(.25f), RoundedCornerShape(32.dp))) {
            Box(Modifier.align(Alignment.TopStart).padding(18.dp).size(10.dp).graphicsLayer { translationY = floatY; alpha = pulse }.background(Color.White, CircleShape))
            Box(Modifier.align(Alignment.BottomEnd).padding(25.dp).size(8.dp).graphicsLayer { translationY = -floatY; translationX = tickerX }.background(colors[0], CircleShape))
            Box(Modifier.align(Alignment.Center).size(150.dp).graphicsLayer { rotationZ = angle; scaleX = breathe; scaleY = breathe; translationY = floatY }.background(Brush.sweepGradient(colors + listOf(Color.White.copy(.10f))), RoundedCornerShape(44.dp)).border(2.dp, Color.White.copy(.34f), RoundedCornerShape(44.dp)))
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (tab == 0) "NEWS CORE" else if (tab == 1) "MARKET CORE" else "SESSION CORE", color = Color.White, fontSize = 22.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
                Text("3D • LIVE-READY • INTERACTIVE", color = Color.White.copy(.84f), fontSize = 10.sp)
            }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors[1].copy(.10f))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(labels[tab], style = MaterialTheme.typography.headlineSmall, color = colors[0], modifier = Modifier.weight(1f))
                    Text("● LIVE", color = colors[0].copy(alpha = pulse), fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
                }
                Box(Modifier.fillMaxWidth().height(25.dp).background(colors[0].copy(.08f), RoundedCornerShape(12.dp)).border(1.dp, colors[0].copy(.18f), RoundedCornerShape(12.dp))) {
                    Text("AMAR • ${labels[tab]} • حركة حية للواجهة • ", color = Color.White.copy(.72f), fontSize = 8.sp, modifier = Modifier.align(Alignment.Center).graphicsLayer { translationX = tickerX })
                }
                val lines = when (tab) {
                    0 -> listOf("الأحداث عالية التأثير", "تصنيف حسب الأهمية", "تغذية الأخبار عبر موصل مستقل", "تنبيهات قابلة للتخصيص")
                    1 -> listOf("الاتجاه والزخم", "التذبذب والسيولة", "مراقبة الأصول", "ربط لاحق بمصدر سوق حي")
                    else -> listOf("آسيا", "أوروبا", "أمريكا", "حالة الجلسة والنشاط")
                }
                lines.forEachIndexed { i, text ->
                    val localPhase = floatY * (if (i % 2 == 0) 1f else -1f)
                    Box(Modifier.fillMaxWidth().graphicsLayer { translationX = localPhase; alpha = (.82f + pulse * .18f) }.background(colors[i % colors.size].copy(.10f), RoundedCornerShape(16.dp)).border(1.dp, colors[i % colors.size].copy(.35f), RoundedCornerShape(16.dp)).padding(12.dp)) { Text("${i + 1}. $text", color = Color.White, fontSize = 12.sp) }
                }
            }
        }
        Text("الحركة والتفاعل مستقلان عن مصادر البيانات؛ البيانات الحية تُربط عبر الموصلات دون اختلاق بيانات.", style = MaterialTheme.typography.labelSmall)
    }
}
