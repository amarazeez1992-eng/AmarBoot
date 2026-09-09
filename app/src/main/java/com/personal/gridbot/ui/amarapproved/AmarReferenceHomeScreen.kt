package com.personal.gridbot.ui.amarapproved

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmarReferenceHomeScreen() {
    var selected by remember { mutableStateOf("الرئيسية") }
    val t = rememberInfiniteTransition(label = "amar_live")
    val pulse by t.animateFloat(.96f, 1.06f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "pulse")
    val orbit by t.animateFloat(0f, 360f, infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "orbit")

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 700.dp
        Box(Modifier.fillMaxSize()) {
            ReferenceBackground(pulse)
            Column(
                Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopGlassBar()
                Spacer(Modifier.height(if (compact) 8.dp else 14.dp))
                Text("عمار", color = Color(0xFFB98518), fontSize = if (compact) 30.sp else 38.sp, fontWeight = FontWeight.ExtraBold)
                Text("منصة التداول الذكية", color = Color(0xFF174A52), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.clip(RoundedCornerShape(18.dp)).background(Color(0xDD19A986)).padding(horizontal = 18.dp, vertical = 6.dp)) {
                    Text("تجريبي", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2f, size.height * .49f)
                val r = if (size.width < 700f) size.width * .18f else size.width * .19f
                drawCircle(Brush.radialGradient(listOf(Color(0xFF73E9DD), Color(0xFF139E9A), Color(0xFF075C70))), r, c)
                drawCircle(Color(0x66FFFFFF), r * .88f, c, style = Stroke(1.5f))
                drawCircle(Color(0x44FFD45B), r * 1.10f, c, style = Stroke(2.5f))
                val a = Math.toRadians(orbit.toDouble())
                val p = Offset(c.x + cos(a).toFloat() * r * 1.1f, c.y + sin(a).toFloat() * r * 1.1f)
                drawCircle(Color(0xFFFFD35A), 5.dp.toPx(), p)
            }

            Node("السوق", Alignment.TopCenter, .29f, selected == "السوق") { selected = "السوق" }
            Node("البوت", Alignment.CenterStart, .38f, selected == "البوت") { selected = "البوت" }
            Node("الرسم البياني", Alignment.CenterEnd, .38f, selected == "الرسم البياني") { selected = "الرسم البياني" }
            Node("المخاطر", Alignment.BottomStart, .22f, selected == "المخاطر") { selected = "المخاطر" }
            Node("التحليل", Alignment.BottomEnd, .22f, selected == "التحليل") { selected = "التحليل" }
            BottomNav(selected) { selected = it }
        }
    }
}

@Composable
private fun ReferenceBackground(pulse: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFFF9FEFF), Color(0xFFDDF7F5), Color(0xFFEAF1DA), Color(0xFFF9E9C7))))
        drawCircle(Color(0x44FFFFFF), w * .32f * pulse, Offset(w * .5f, h * .34f))
        drawOval(Color(0x3348BDB1), Offset(-w*.18f, h*.48f), androidx.compose.ui.geometry.Size(w*.78f, h*.35f))
        drawOval(Color(0x335C8F83), Offset(w*.52f, h*.48f), androidx.compose.ui.geometry.Size(w*.78f, h*.35f))
        drawOval(Color(0x5583B58C), Offset(-w*.12f, h*.68f), androidx.compose.ui.geometry.Size(w*.72f, h*.42f))
        drawOval(Color(0x4480A87D), Offset(w*.48f, h*.68f), androidx.compose.ui.geometry.Size(w*.72f, h*.42f))
        drawLine(Color(0xAAE5B84F), Offset(w*.18f, h*.58f), Offset(w*.82f, h*.58f), 2.dp.toPx())
    }
}

@Composable
private fun TopGlassBar() {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color(0xBFFFFFFF)).padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("عمار متصل", Color(0xFF0A786F), 12.sp, fontWeight = FontWeight.Bold)
        Text("الحساب تجريبي", Color(0xFF174A52), 10.sp)
        Text("XAUUSD  M5  ﹀", Color(0xFF174A52), 10.sp, fontWeight = FontWeight.SemiBold)
        Text("⚙", Color(0xFF174A52), 17.sp)
    }
}

@Composable
private fun Node(label: String, alignment: Alignment, fraction: Float, selected: Boolean, onClick: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val top = if (alignment == Alignment.TopCenter) maxHeight * fraction else 0.dp
        val bottom = if (alignment == Alignment.BottomStart || alignment == Alignment.BottomEnd) maxHeight * fraction else 0.dp
        Box(Modifier.align(alignment).padding(top = top, bottom = bottom).size(82.dp).scale(if (selected) 1.10f else 1f).clip(CircleShape).background(Color(0xCFFFFFFF)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Text(label, Color(0xFF174A52), 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BottomNav(selected: String, onSelected: (String) -> Unit) {
    val items = listOf("الإعدادات", "التنبيهات", "الرئيسية", "الأداء", "المزيد")
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp).clip(RoundedCornerShape(30.dp)).background(Color(0xD9FFFFFF)).padding(5.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        items.forEach { item ->
            Column(Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected == item) Color(0xFFDCF5EF) else Color.Transparent).clickable { onSelected(item) }.padding(horizontal = 6.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (selected == item) "●" else "○", Color(0xFF159B89), 10.sp)
                Text(item, Color(0xFF174A52), 8.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
