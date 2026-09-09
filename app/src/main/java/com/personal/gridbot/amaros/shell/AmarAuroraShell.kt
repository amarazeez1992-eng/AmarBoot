package com.personal.gridbot.amaros.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.core.AmarEvent
import com.personal.gridbot.amaros.core.AmarEventBus
import com.personal.gridbot.amaros.navigation.AmarRoom
import com.personal.gridbot.amaros.rooms.commandcenter.CommandCenterScreen

private val Void = Color(0xFF040611)
private val Cyan = Color(0xFF42E8FF)
private val Violet = Color(0xFF9B6BFF)
private val Emerald = Color(0xFF45F0A0)
private val Gold = Color(0xFFFFD166)
private val Magenta = Color(0xFFFF5BBE)

@Composable
fun AmarAuroraShell(initialState: AmarAppState = AmarAppState(), onOpenLegacyGrid: () -> Unit = {}) {
    var state by remember { mutableStateOf(initialState) }
    var orbit by remember { mutableFloatStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "aurora-motion")
    val drift by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(10000), RepeatMode.Reverse), label = "aurora-drift")
    val pulse by transition.animateFloat(.82f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse")

    Box(Modifier.fillMaxSize().background(Void)) {
        AuroraBackdrop(drift)
        Row(Modifier.fillMaxSize().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoomOrbit(
                selected = state.selectedRoom,
                orbit = orbit,
                onDrag = { orbit += it / 90f },
                onSelect = {
                    state = state.copy(selectedRoom = it)
                    AmarEventBus.publish(AmarEvent.RoomSelected(it.name))
                }
            )
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiveHud(state.selectedRoom, pulse, onOpenLegacyGrid)
                AnimatedContent(targetState = state.selectedRoom, label = "room") { room ->
                    RoomStage(room, drift, onOpenLegacyGrid)
                }
            }
        }
    }
}

@Composable
private fun AuroraBackdrop(drift: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(Brush.radialGradient(listOf(Cyan.copy(.18f), Color.Transparent), androidx.compose.ui.geometry.Offset(w * (.18f + drift * .22f), h * .15f), w * .7f))
        drawRect(Brush.radialGradient(listOf(Violet.copy(.15f), Color.Transparent), androidx.compose.ui.geometry.Offset(w * (.82f - drift * .18f), h * .78f), w * .65f))
        drawRect(Brush.radialGradient(listOf(Emerald.copy(.07f), Color.Transparent), androidx.compose.ui.geometry.Offset(w * .48f, h * .48f), w * .5f))
        for (i in 0..12) {
            val y = h * i / 12f
            drawLine(Color.White.copy(.025f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(w, y), 1f)
        }
    }
}

@Composable
private fun RoomOrbit(selected: AmarRoom, orbit: Float, onDrag: (Float) -> Unit, onSelect: (AmarRoom) -> Unit) {
    Box(Modifier.width(92.dp).fillMaxHeight().clip(RoundedCornerShape(30.dp)).background(Color.White.copy(.045f)).pointerInput(Unit) { detectDragGestures { _, drag -> onDrag(drag.y) } }) {
        Column(Modifier.fillMaxSize().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("A", color = Cyan, style = MaterialTheme.typography.headlineSmall)
            Text("AMAR", color = Color.White.copy(.55f), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(10.dp))
            AmarRoom.entries.forEachIndexed { index, room ->
                val active = room == selected
                val shift = kotlin.math.sin((index + orbit) * .65f).toFloat() * if (active) 3f else 1f
                TextButton(onClick = { onSelect(room) }, modifier = Modifier.width(82.dp).graphicsLayer { translationX = shift; scaleX = if (active) 1.08f else 1f; scaleY = if (active) 1.08f else 1f }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(room.emoji, style = MaterialTheme.typography.titleMedium)
                        Text(room.titleAr.take(8), color = if (active) Cyan else Color.White.copy(.48f), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveHud(room: AmarRoom, pulse: Float, onOpenLegacyGrid: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White.copy(.055f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(11.dp * pulse).clip(CircleShape).background(Emerald))
        Column(Modifier.weight(1f)) {
            Text("AMAR // TRADING OS", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text("${room.emoji} ${room.titleAr}  •  XAUUSD  •  M5  •  DEMO", color = Color.White.copy(.55f), style = MaterialTheme.typography.labelMedium)
        }
        HudMetric("82", "SCORE", Cyan)
        HudMetric("+12.84", "FLOAT", Emerald)
        HudMetric("30", "GRID", Gold)
        if (room == AmarRoom.BOT_LAB) TextButton(onClick = onOpenLegacyGrid) { Text("GRID", color = Gold) }
    }
}

@Composable
private fun HudMetric(value: String, label: String, accent: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(value, color = accent, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color.White.copy(.4f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun RoomStage(room: AmarRoom, drift: Float, onOpenLegacyGrid: () -> Unit) {
    when (room) {
        AmarRoom.COMMAND_CENTER -> CommandCenterScreen()
        AmarRoom.BOT_LAB -> GridDeck(drift, onOpenLegacyGrid)
        AmarRoom.MARKET -> MarketDeck(drift)
        AmarRoom.CHART -> ChartDeck(drift)
        AmarRoom.RISK -> RiskDeck()
        AmarRoom.PERFORMANCE -> MetricsDeck("PERFORMANCE FLOW", listOf("+18.40", "+127.40", "66.7%", "2.1%"), listOf("TODAY", "TOTAL", "WIN RATE", "DRAWDOWN"), Violet)
        AmarRoom.POSITIONS -> MetricsDeck("POSITIONS MATRIX", listOf("6", "18", "+12.84", "0.12"), listOf("OPEN", "PENDING", "FLOAT", "SPREAD"), Emerald)
        AmarRoom.INDICATORS -> MetricsDeck("INDICATOR MATRIX", listOf("EMA", "RSI", "VWAP", "ATR"), listOf("TREND", "MOMENTUM", "PRICE", "VOLATILITY"), Violet)
        AmarRoom.ANALYSIS -> MetricsDeck("ANALYSIS ENGINE", listOf("BUY", "82%", "MOMENTUM", "M5"), listOf("BIAS", "SCORE", "REGIME", "FRAME"), Cyan)
        AmarRoom.TESTING -> MetricsDeck("TEST LAB", listOf("BACKTEST", "FORWARD", "MONTE", "READY"), listOf("ENGINE", "MODE", "SIM", "STATE"), Gold)
        AmarRoom.TOOLS -> MetricsDeck("TRADING TOOLS", listOf("RISK", "LOT", "TP/SL", "GRID"), listOf("CALC", "SIZE", "CONTROL", "TOOLS"), Cyan)
        AmarRoom.ALERTS -> MetricsDeck("ALERT MATRIX", listOf("LIVE", "0", "ARMED", "SAFE"), listOf("LINK", "ACTIVE", "RULES", "RISK"), Magenta)
        AmarRoom.LIBRARY -> MetricsDeck("AMAR LIBRARY", listOf("CORE", "14", "READY", "EXT"), listOf("SYSTEM", "MODULES", "STATE", "HOOKS"), Cyan)
        AmarRoom.SETTINGS -> MetricsDeck("CONTROL DECK", listOf("HIGH", "AURORA", "DEMO", "SAFE"), listOf("MOTION", "THEME", "MODE", "GUARD"), Violet)
    }
}

@Composable
private fun GridDeck(drift: Float, onOpenLegacyGrid: () -> Unit) = Stage("GRID COMMAND DECK", Gold) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        RobotNode(drift, Modifier.weight(1.2f))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Dial("LOT", "0.02", .30f, Gold)
            Dial("STEP", "40", .48f, Cyan)
            Dial("MAX", "30", .72f, Violet)
            Dial("MART", "2.00", .60f, Magenta)
            TextButton(onClick = onOpenLegacyGrid) { Text("فتح محرك Grid الحالي", color = Gold) }
        }
    }
}

@Composable
private fun RobotNode(drift: Float, modifier: Modifier) = Box(modifier.fillMaxHeight().clip(RoundedCornerShape(28.dp)).background(Color.White.copy(.035f)), contentAlignment = Alignment.Center) {
    Canvas(Modifier.fillMaxSize()) {
        val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f + kotlin.math.sin(drift * 6.28f) * 12f)
        drawCircle(Cyan.copy(.08f), size.minDimension * .36f, c)
        drawCircle(Cyan.copy(.22f), size.minDimension * .24f, c, style = Stroke(3f))
        drawCircle(Violet.copy(.18f), size.minDimension * .31f, c, style = Stroke(1.5f))
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("AMAR", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text("ANALYZING • DEMO", color = Cyan, style = MaterialTheme.typography.labelMedium)
        Text("82%", color = Emerald, style = MaterialTheme.typography.displaySmall)
    }
}

@Composable
private fun MarketDeck(drift: Float) = Stage("MARKET PULSE", Emerald) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ChartDeck(drift, Modifier.weight(1.7f))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("XAUUSD", "BUY", "82%", Cyan)
            Tile("SPREAD", "0.12", "LOW", Emerald)
            Tile("SESSION", "LONDON", "OPEN", Gold)
            Tile("VOLATILITY", "HIGH", "ATR", Magenta)
        }
    }
}

@Composable
private fun ChartDeck(drift: Float, modifier: Modifier = Modifier.fillMaxSize()) = Stage("XAUUSD • M5 • LIVE CANVAS", Cyan, modifier) {
    Canvas(Modifier.fillMaxSize().padding(12.dp)) {
        val base = size.height * .72f
        for (i in 0..10) {
            val y = size.height * i / 10f
            drawLine(Color.White.copy(.055f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f)
        }
        val path = Path()
        for (i in 0..80) {
            val x = size.width * i / 80f
            val y = base - kotlin.math.sin(i * .34f + drift * 2f) * size.height * .055f - i * size.height * .0028f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            if (i % 5 == 0) drawLine(Cyan.copy(.3f), androidx.compose.ui.geometry.Offset(x, y - 10), androidx.compose.ui.geometry.Offset(x, y + 10), 1.5f)
        }
        drawPath(path, Cyan, style = Stroke(3f, cap = StrokeCap.Round))
        drawCircle(Emerald, 6f, androidx.compose.ui.geometry.Offset(size.width * .86f, base - size.height * .24f))
    }
}

@Composable
private fun RiskDeck() = Stage("RISK RADAR", Magenta) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Dial("RISK", "5%", .18f, Emerald)
            Dial("DAILY DD", "10%", .35f, Gold)
            Dial("MAX LOT", "0.20", .52f, Cyan)
            Dial("BASKET", "$100", .72f, Magenta)
        }
        Tile("EQUITY", "$1,012.84", "SAFE", Emerald, Modifier.weight(1f))
    }
}

@Composable
private fun MetricsDeck(title: String, values: List<String>, labels: List<String>, accent: Color) = Stage(title, accent) {
    Row(Modifier.fillMaxSize().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        values.zip(labels).forEach { (value, label) -> Tile(label, value, "LIVE", accent, Modifier.width(190.dp)) }
    }
}

@Composable
private fun Stage(title: String, accent: Color, modifier: Modifier = Modifier.fillMaxSize(), content: @Composable () -> Unit) = Column(modifier.clip(RoundedCornerShape(30.dp)).background(Color.White.copy(.045f)).padding(14.dp)) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(8.dp))
        Text(title, color = Color.White.copy(.9f), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.weight(1f))
        Text("● LIVE", color = accent, style = MaterialTheme.typography.labelSmall)
    }
    Spacer(Modifier.height(12.dp))
    Box(Modifier.fillMaxSize()) { content() }
}

@Composable
private fun Tile(label: String, value: String, state: String, accent: Color, modifier: Modifier = Modifier.width(170.dp)) = Column(modifier.clip(RoundedCornerShape(24.dp)).background(Color.Black.copy(.18f)).padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
    Text(label, color = Color.White.copy(.42f), style = MaterialTheme.typography.labelSmall)
    Text(value, color = accent, style = MaterialTheme.typography.headlineSmall)
    Text(state, color = Color.White.copy(.55f), style = MaterialTheme.typography.labelMedium)
}

@Composable
private fun Dial(label: String, value: String, amount: Float, accent: Color) = Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.Black.copy(.16f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) {
        Text(label, color = Color.White.copy(.45f), style = MaterialTheme.typography.labelSmall)
        Text(value, color = accent, style = MaterialTheme.typography.titleLarge)
    }
    Canvas(Modifier.size(54.dp)) {
        drawArc(accent.copy(.15f), -90f, 360f, false, style = Stroke(5f))
        drawArc(accent, -90f, amount * 360f, false, style = Stroke(5f, cap = StrokeCap.Round))
    }
}
