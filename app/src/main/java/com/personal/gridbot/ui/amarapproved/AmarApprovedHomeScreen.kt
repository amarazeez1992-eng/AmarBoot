package com.personal.gridbot.ui.amarapproved

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF17485A)
private val Cyan = Color(0xFF58D9D2)
private val Green = Color(0xFF16C784)
private val Gold = Color(0xFFD79B28)
private val Glass = Color(0xCFFFFFFF)

@Composable
fun AmarApprovedHomeScreen() {
    var selected by remember { mutableStateOf("الرئيسية") }
    val transition = rememberInfiniteTransition(label = "amar-home")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFEAF8F6), Color(0xFFF7F4EA))))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0x2258D9D2), radius = size.minDimension * .62f, center = center.copy(y = size.height * .58f))
            drawCircle(Color(0x18D79B28), radius = size.minDimension * .42f, center = center.copy(y = size.height * .60f))
            for (i in 1..4) {
                drawCircle(Cyan.copy(alpha = .10f), radius = size.minDimension * (.18f + i * .055f), center = center.copy(y = size.height * .61f), style = Stroke(width = 2.dp.toPx()))
            }
        }

        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
            Spacer(Modifier.height(10.dp))
            TopBar()
            Spacer(Modifier.height(22.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("عمار", fontSize = 52.sp, fontWeight = FontWeight.ExtraBold, color = Gold)
                Text("منصة التداول الذكية", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(8.dp))
                Text("ذكاء • تحليل • قرارات أفضل", fontSize = 15.sp, color = Ink.copy(alpha = .72f))
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF0B9E79), Green)))
                        .clickable { selected = "الحساب التجريبي" }
                        .padding(horizontal = 28.dp, vertical = 9.dp)
                ) { Text("تجريبي", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(24.dp))
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FeatureNode("السوق", "▥", Color(0xFF20B88A), Modifier.padding(bottom = 12.dp)) { selected = "السوق" }
                    Row(horizontalArrangement = Arrangement.spacedBy(34.dp), verticalAlignment = Alignment.CenterVertically) {
                        FeatureNode("البوت", "◉", Color(0xFF1DBB87), Modifier) { selected = "البوت" }
                        CoreOrb(pulse)
                        FeatureNode("الرسم البياني", "▥", Color(0xFF269ED6), Modifier) { selected = "الرسم البياني" }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(34.dp), verticalAlignment = Alignment.CenterVertically) {
                        FeatureNode("المخاطر", "◇", Gold, Modifier) { selected = "المخاطر" }
                        FeatureNode("التحليل", "✦", Color(0xFF8B68D8), Modifier) { selected = "التحليل" }
                    }
                }
            }

            AnimatedContent(
                targetState = selected,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "selected"
            ) { value ->
                Text(value, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Center, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            BottomBar(selected) { selected = it }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(50)).clip(RoundedCornerShape(50)).background(Glass).padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("● عمار متصل", color = Green, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("⚗ الحساب التجريبي", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text("XAUUSD  M5 ⌄", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("⚙", color = Ink, fontSize = 24.sp)
    }
}

@Composable
private fun FeatureNode(title: String, icon: String, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable(onClick = onClick)) {
        Box(Modifier.size(82.dp).shadow(12.dp, CircleShape).clip(CircleShape).background(Brush.radialGradient(listOf(Color.White, accent.copy(alpha = .48f), Color(0xFFE9F6F3)))), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 32.sp, color = accent, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(7.dp))
        Box(Modifier.clip(RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(Ink, accent.copy(alpha = .72f)))).padding(horizontal = 17.dp, vertical = 7.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CoreOrb(scale: Float) {
    Box(Modifier.size((122 * scale).dp).shadow(18.dp, CircleShape).clip(CircleShape).background(Brush.radialGradient(listOf(Color.White, Color(0xFF55D9D2), Color(0xFF177A83)))), contentAlignment = Alignment.Center) {
        Text("✦", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomBar(selected: String, onSelect: (String) -> Unit) {
    val items = listOf("المزيد" to "⋯", "الأداء" to "▥", "الرئيسية" to "⌂", "التنبيهات" to "♢", "الإعدادات" to "⚙")
    Row(Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(30)).clip(RoundedCornerShape(30)).background(Glass).padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        items.forEach { (label, icon) ->
            val active = selected == label
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelect(label) }.padding(horizontal = 5.dp)) {
                Text(icon, color = if (active) Gold else Ink, fontSize = if (active) 28.sp else 22.sp, fontWeight = FontWeight.Bold)
                Text(label, color = if (active) Gold else Ink.copy(alpha = .78f), fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
