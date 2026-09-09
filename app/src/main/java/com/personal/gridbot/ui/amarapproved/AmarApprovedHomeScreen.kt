package com.personal.gridbot.ui.amarapproved

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AmarInk = Color(0xFF164B61)
private val AmarCyan = Color(0xFF4EDBD3)
private val AmarGreen = Color(0xFF16C887)
private val AmarGold = Color(0xFFD69A25)
private val AmarBlue = Color(0xFF259DD8)
private val AmarViolet = Color(0xFF8069D8)
private val AmarGlass = Color(0xDDFBFFFF)

@Composable
fun AmarApprovedHomeScreen() {
    var selected by remember { mutableStateOf("الرئيسية") }
    val motion = rememberInfiniteTransition(label = "amar-motion")
    val pulse by motion.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "core-pulse"
    )
    val sweep by motion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000)),
        label = "core-sweep"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF8FCFC),
                        Color(0xFFEAF7F8),
                        Color(0xFFF9F4E9)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val compact = maxWidth < 360.dp
        val horizontal = if (compact) 12.dp else 18.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            AmarTopBar(compact)
            Spacer(Modifier.height(if (compact) 12.dp else 18.dp))

            AmarBrandHeader()

            Spacer(Modifier.height(if (compact) 10.dp else 16.dp))
            AmarStatusChip(selected)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AmarSpatialCore(
                    compact = compact,
                    pulse = pulse,
                    sweep = sweep,
                    selected = selected,
                    onSelect = { selected = it }
                )
            }

            Text(
                text = "ذكاء • تحليل • قرارات أفضل",
                color = AmarInk,
                fontSize = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            AmarGoldDivider()
            Spacer(Modifier.height(if (compact) 8.dp else 12.dp))
            AmarBottomBar(selected = selected, onSelect = { selected = it })
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun AmarTopBar(compact: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(40.dp))
            .clip(RoundedCornerShape(40.dp))
            .background(AmarGlass)
            .padding(horizontal = if (compact) 12.dp else 18.dp, vertical = if (compact) 10.dp else 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AmarGreen)
            )
            Spacer(Modifier.width(7.dp))
            Text("عمار متصل", color = AmarInk, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.Bold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TinyLineIcon(kind = IconKind.FLASK, color = AmarInk, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(6.dp))
            Text("الحساب تجريبي", color = AmarInk, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("XAUUSD", color = AmarInk, fontSize = if (compact) 12.sp else 14.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(4.dp))
            Text("M5", color = AmarInk, fontSize = if (compact) 11.sp else 13.sp)
            Spacer(Modifier.width(5.dp))
            Text("⌄", color = AmarGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            Modifier
                .size(if (compact) 32.dp else 38.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xE8FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            TinyLineIcon(kind = IconKind.SETTINGS, color = AmarInk, modifier = Modifier.size(if (compact) 20.dp else 24.dp))
        }
    }
}

@Composable
private fun AmarBrandHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("عمار", color = AmarGold, fontSize = 50.sp, fontWeight = FontWeight.ExtraBold)
        Text("منصة التداول الذكية", color = AmarInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("AMAR TRADING", color = AmarInk.copy(alpha = .58f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}

@Composable
private fun AmarStatusChip(selected: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF087F70), AmarGreen)))
            .padding(horizontal = 24.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("تجريبي", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(8.dp))
        Text("• $selected", color = Color.White.copy(alpha = .9f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AmarSpatialCore(
    compact: Boolean,
    pulse: Float,
    sweep: Float,
    selected: String,
    onSelect: (String) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val core = if (compact) 118.dp else 142.dp
        val node = if (compact) 72.dp else 86.dp
        val labelWidth = if (compact) 86.dp else 108.dp

        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f + size.height * .03f)
            val orbit = minOf(size.width * .34f, size.height * .34f)
            for (i in 1..5) {
                drawCircle(
                    color = AmarCyan.copy(alpha = .10f),
                    radius = orbit * (0.55f + i * .10f),
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }
            drawArc(
                color = AmarGold.copy(alpha = .58f),
                startAngle = sweep,
                sweepAngle = 115f,
                useCenter = false,
                topLeft = Offset(center.x - orbit, center.y - orbit),
                size = Size(orbit * 2f, orbit * 2f),
                style = Stroke(width = 2.5f)
            )
            drawArc(
                color = AmarCyan.copy(alpha = .65f),
                startAngle = sweep + 180f,
                sweepAngle = 105f,
                useCenter = false,
                topLeft = Offset(center.x - orbit * .88f, center.y - orbit * .88f),
                size = Size(orbit * 1.76f, orbit * 1.76f),
                style = Stroke(width = 2f)
            )
        }

        CoreGlobe(
            modifier = Modifier
                .align(Alignment.Center)
                .size(core * pulse),
            sweep = sweep
        )

        SpatialNode(
            title = "السوق",
            kind = IconKind.MARKET,
            accent = AmarGreen,
            size = node,
            labelWidth = labelWidth,
            selected = selected == "السوق",
            modifier = Modifier.align(Alignment.TopCenter),
            onClick = { onSelect("السوق") }
        )

        SpatialNode(
            title = "البوت",
            kind = IconKind.BOT,
            accent = AmarGreen,
            size = node,
            labelWidth = labelWidth,
            selected = selected == "البوت",
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = { onSelect("البوت") }
        )

        SpatialNode(
            title = "الرسم البياني",
            kind = IconKind.CHART,
            accent = AmarBlue,
            size = node,
            labelWidth = labelWidth,
            selected = selected == "الرسم البياني",
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = { onSelect("الرسم البياني") }
        )

        SpatialNode(
            title = "المخاطر",
            kind = IconKind.RISK,
            accent = AmarGold,
            size = node,
            labelWidth = labelWidth,
            selected = selected == "المخاطر",
            modifier = Modifier.align(Alignment.BottomStart),
            onClick = { onSelect("المخاطر") }
        )

        SpatialNode(
            title = "التحليل",
            kind = IconKind.ANALYSIS,
            accent = AmarViolet,
            size = node,
            labelWidth = labelWidth,
            selected = selected == "التحليل",
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = { onSelect("التحليل") }
        )
    }
}

@Composable
private fun CoreGlobe(modifier: Modifier, sweep: Float) {
    Box(
        modifier = modifier
            .shadow(20.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFFBFFFF7), Color(0xFF38CFC6), Color(0xFF0B6471)))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(13.dp)) {
            drawCircle(Color(0x35FFFFFF), style = Stroke(width = 2.dp.toPx()))
            drawOval(Color(0x70FFFFFF), style = Stroke(width = 1.6.dp.toPx()))
            drawArc(
                color = Color(0xCCFFFFFF),
                startAngle = sweep,
                sweepAngle = 75f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx())
            )
            drawLine(Color(0x66FFFFFF), Offset(size.width * .18f, size.height * .54f), Offset(size.width * .82f, size.height * .54f), strokeWidth = 1.4.dp.toPx())
            drawLine(Color(0x55FFFFFF), Offset(size.width * .30f, size.height * .20f), Offset(size.width * .70f, size.height * .80f), strokeWidth = 1.2.dp.toPx())
            drawCircle(AmarGold, radius = 5.dp.toPx(), center = Offset(size.width * .70f, size.height * .32f))
        }
        Text("عمار", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
    }
}

private enum class IconKind { MARKET, BOT, CHART, RISK, ANALYSIS, SETTINGS, FLASK, HOME, ALERT, PERFORMANCE, MORE }

@Composable
private fun SpatialNode(
    title: String,
    kind: IconKind,
    accent: Color,
    size: androidx.compose.ui.unit.Dp,
    labelWidth: androidx.compose.ui.unit.Dp,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(if (selected) 18.dp else 11.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color.White, accent.copy(alpha = .72f), accent.copy(alpha = .30f)))),
            contentAlignment = Alignment.Center
        ) {
            TinyLineIcon(kind = kind, color = Color.White, modifier = Modifier.size(size * .48f))
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .width(labelWidth)
                .clip(RoundedCornerShape(50.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xF31A5A66), accent.copy(alpha = .92f))))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

@Composable
private fun TinyLineIcon(kind: IconKind, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val stroke = 2.2.dp.toPx()
        val w = size.width
        val h = size.height
        when (kind) {
            IconKind.MARKET, IconKind.PERFORMANCE -> {
                drawLine(color, Offset(w * .18f, h * .82f), Offset(w * .18f, h * .20f), strokeWidth = stroke)
                drawLine(color, Offset(w * .18f, h * .82f), Offset(w * .88f, h * .82f), strokeWidth = stroke)
                drawRect(color, Offset(w * .28f, h * .56f), Size(w * .12f, h * .26f))
                drawRect(color, Offset(w * .47f, h * .40f), Size(w * .12f, h * .42f))
                drawRect(color, Offset(w * .66f, h * .24f), Size(w * .12f, h * .58f))
            }
            IconKind.BOT -> {
                drawRoundRect(color, Offset(w * .18f, h * .25f), Size(w * .64f, h * .50f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f), style = Stroke(stroke))
                drawCircle(color, 2.5.dp.toPx(), Offset(w * .40f, h * .49f))
                drawCircle(color, 2.5.dp.toPx(), Offset(w * .60f, h * .49f))
                drawLine(color, Offset(w * .50f, h * .25f), Offset(w * .50f, h * .12f), strokeWidth = stroke)
                drawCircle(color, 2.dp.toPx(), Offset(w * .50f, h * .10f))
                drawLine(color, Offset(w * .10f, h * .48f), Offset(w * .18f, h * .48f), strokeWidth = stroke)
                drawLine(color, Offset(w * .82f, h * .48f), Offset(w * .90f, h * .48f), strokeWidth = stroke)
            }
            IconKind.CHART -> {
                drawLine(color, Offset(w * .12f, h * .82f), Offset(w * .12f, h * .18f), strokeWidth = stroke)
                drawLine(color, Offset(w * .12f, h * .82f), Offset(w * .90f, h * .82f), strokeWidth = stroke)
                drawRect(color, Offset(w * .26f, h * .46f), Size(w * .10f, h * .36f), style = Stroke(stroke))
                drawLine(color, Offset(w * .31f, h * .34f), Offset(w * .31f, h * .62f), strokeWidth = stroke)
                drawRect(color, Offset(w * .49f, h * .28f), Size(w * .10f, h * .54f), style = Stroke(stroke))
                drawLine(color, Offset(w * .54f, h * .18f), Offset(w * .54f, h * .68f), strokeWidth = stroke)
                drawRect(color, Offset(w * .72f, h * .38f), Size(w * .10f, h * .44f), style = Stroke(stroke))
            }
            IconKind.RISK -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * .50f, h * .10f)
                    lineTo(w * .84f, h * .25f)
                    lineTo(w * .77f, h * .66f)
                    lineTo(w * .50f, h * .88f)
                    lineTo(w * .23f, h * .66f)
                    lineTo(w * .16f, h * .25f)
                    close()
                }
                drawPath(path, color, style = Stroke(stroke))
                drawLine(color, Offset(w * .38f, h * .51f), Offset(w * .47f, h * .61f), strokeWidth = stroke)
                drawLine(color, Offset(w * .47f, h * .61f), Offset(w * .66f, h * .39f), strokeWidth = stroke)
            }
            IconKind.ANALYSIS -> {
                drawCircle(color, w * .32f, Offset(w * .50f, h * .48f), style = Stroke(stroke))
                drawLine(color, Offset(w * .50f, h * .48f), Offset(w * .73f, h * .25f), strokeWidth = stroke)
                drawLine(color, Offset(w * .26f, h * .75f), Offset(w * .40f, h * .61f), strokeWidth = stroke)
                drawLine(color, Offset(w * .40f, h * .61f), Offset(w * .52f, h * .70f), strokeWidth = stroke)
                drawLine(color, Offset(w * .52f, h * .70f), Offset(w * .72f, h * .50f), strokeWidth = stroke)
            }
            IconKind.SETTINGS -> {
                drawCircle(color, w * .32f, Offset(w / 2f, h / 2f), style = Stroke(stroke))
                drawCircle(color, w * .08f, Offset(w / 2f, h / 2f))
                for (i in 0 until 8) {
                    val a = Math.toRadians((i * 45).toDouble())
                    val x1 = w / 2f + kotlin.math.cos(a).toFloat() * w * .38f
                    val y1 = h / 2f + kotlin.math.sin(a).toFloat() * h * .38f
                    val x2 = w / 2f + kotlin.math.cos(a).toFloat() * w * .48f
                    val y2 = h / 2f + kotlin.math.sin(a).toFloat() * h * .48f
                    drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = stroke)
                }
            }
            IconKind.FLASK -> {
                drawLine(color, Offset(w * .40f, h * .16f), Offset(w * .40f, h * .34f), strokeWidth = stroke)
                drawLine(color, Offset(w * .60f, h * .16f), Offset(w * .60f, h * .34f), strokeWidth = stroke)
                drawLine(color, Offset(w * .32f, h * .16f), Offset(w * .68f, h * .16f), strokeWidth = stroke)
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * .40f, h * .34f)
                    lineTo(w * .20f, h * .78f)
                    quadraticBezierTo(w * .50f, h * .90f, w * .80f, h * .78f)
                    lineTo(w * .60f, h * .34f)
                }
                drawPath(p, color, style = Stroke(stroke))
            }
            IconKind.HOME -> {
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * .15f, h * .46f)
                    lineTo(w * .50f, h * .16f)
                    lineTo(w * .85f, h * .46f)
                    lineTo(w * .78f, h * .46f)
                    lineTo(w * .78f, h * .84f)
                    lineTo(w * .22f, h * .84f)
                    lineTo(w * .22f, h * .46f)
                    close()
                }
                drawPath(p, color, style = Stroke(stroke))
            }
            IconKind.ALERT -> {
                drawCircle(color, w * .34f, Offset(w / 2f, h * .48f), style = Stroke(stroke))
                drawLine(color, Offset(w * .50f, h * .28f), Offset(w * .50f, h * .56f), strokeWidth = stroke)
                drawCircle(color, 1.8.dp.toPx(), Offset(w * .50f, h * .68f))
            }
            IconKind.MORE -> {
                drawCircle(color, 2.5.dp.toPx(), Offset(w * .25f, h * .50f))
                drawCircle(color, 2.5.dp.toPx(), Offset(w * .50f, h * .50f))
                drawCircle(color, 2.5.dp.toPx(), Offset(w * .75f, h * .50f))
            }
        }
    }
}

@Composable
private fun AmarGoldDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Box(Modifier.width(74.dp).height(1.dp).background(AmarGold.copy(alpha = .45f)))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(AmarGold).padding(1.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.width(74.dp).height(1.dp).background(AmarGold.copy(alpha = .45f)))
    }
}

@Composable
private fun AmarBottomBar(selected: String, onSelect: (String) -> Unit) {
    val items = listOf(
        "الإعدادات" to IconKind.SETTINGS,
        "التنبيهات" to IconKind.ALERT,
        "الرئيسية" to IconKind.HOME,
        "الأداء" to IconKind.PERFORMANCE,
        "المزيد" to IconKind.MORE
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(34.dp))
            .clip(RoundedCornerShape(34.dp))
            .background(AmarGlass)
            .padding(horizontal = 5.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, kind) ->
            val active = selected == label
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onSelect(label) }
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(if (active) 48.dp else 34.dp)
                        .clip(CircleShape)
                        .background(if (active) Brush.radialGradient(listOf(Color.White, AmarGreen, Color(0xFF0B746D))) else Brush.radialGradient(listOf(Color.White, Color(0xFFE8F3F4)))),
                    contentAlignment = Alignment.Center
                ) {
                    TinyLineIcon(kind, if (active) Color.White else AmarInk, Modifier.size(if (active) 26.dp else 22.dp))
                }
                Text(label, color = if (active) AmarGold else AmarInk, fontSize = 10.sp, fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold)
            }
        }
    }
}
