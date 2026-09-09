package com.personal.gridbot.amaros.shell

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.core.AmarEvent
import com.personal.gridbot.amaros.core.AmarEventBus
import com.personal.gridbot.amaros.navigation.AmarRoom
import kotlin.math.cos
import kotlin.math.sin

private val Bg = Color(0xFF03050D)
private val Ice = Color(0xFFF2FCFF)
private val Cyan = Color(0xFF35E8FF)
private val Violet = Color(0xFFA66CFF)
private val Green = Color(0xFF42F0A0)
private val Gold = Color(0xFFFFD166)
private val Pink = Color(0xFFFF58C8)
private val Red = Color(0xFFFF6675)

@Composable
fun AmarAuroraShell(
    initialState: AmarAppState = AmarAppState(),
    onOpenLegacyGrid: () -> Unit = {}
) {
    var entered by remember { mutableStateOf(false) }
    var selected by remember { mutableIntStateOf(0) }
    var orbit by remember { mutableFloatStateOf(0f) }
    var focus by remember { mutableIntStateOf(0) }
    var appState by remember { mutableStateOf(initialState) }
    val rooms = AmarRoom.entries
    val transition = rememberInfiniteTransition(label = "aurora")
    val flow by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(9000), RepeatMode.Reverse), label = "flow")
    val pulse by transition.animateFloat(0.72f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "pulse")
    Box(Modifier.fillMaxSize().background(Bg)) {
        AuroraBackground(flow, pulse)
        if (!entered) {
            EntryPortal(flow, pulse) { entered = true }
        } else {
            NexusWorld(rooms, selected, orbit, flow, pulse, focus, { orbit += it / 220f }, { index ->
                selected = index
                focus = 0
                appState = appState.copy(selectedRoom = rooms[index])
                AmarEventBus.publish(AmarEvent.RoomSelected(rooms[index].name))
            }, { focus = it }, onOpenLegacyGrid)
        }
    }
}

@Composable
private fun AuroraBackground(flow: Float, pulse: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(Brush.radialGradient(listOf(Cyan.copy(alpha = 0.20f * pulse), Color.Transparent), androidx.compose.ui.geometry.Offset(w * (0.15f + flow * 0.20f), h * 0.12f), w * 0.72f))
        drawRect(Brush.radialGradient(listOf(Violet.copy(alpha = 0.18f), Color.Transparent), androidx.compose.ui.geometry.Offset(w * (0.88f - flow * 0.20f), h * 0.84f), w * 0.70f))
        drawRect(Brush.radialGradient(listOf(Pink.copy(alpha = 0.07f), Color.Transparent), androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.50f), w * 0.46f))
        for (i in 0..18) {
            val y = h * i / 18f
            drawLine(Color.White.copy(alpha = 0.018f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(w, y), 1f)
        }
        for (i in 0..12) {
            val x = w * i / 12f
            drawLine(Color.White.copy(alpha = 0.012f), androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, h), 1f)
        }
    }
}

@Composable
private fun EntryPortal(flow: Float, pulse: Float, onEnter: () -> Unit) {
    Box(Modifier.fillMaxSize().clickable { onEnter() }) {
        Canvas(Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.22f
            for (i in 0..7) drawCircle(Cyan.copy(alpha = 0.025f + i * 0.012f), radius + i * 30f, center, style = Stroke(1f))
            drawCircle(Cyan.copy(alpha = 0.30f), radius, center, style = Stroke(3f))
            drawCircle(Violet.copy(alpha = 0.20f), radius * 0.70f, center, style = Stroke(2f))
            for (i in 0..15) {
                val angle = flow * 6.283f + i * 0.392f
                val point = androidx.compose.ui.geometry.Offset(center.x + cos(angle) * radius * 1.27f, center.y + sin(angle) * radius * 1.27f)
                drawCircle(if (i % 2 == 0) Cyan else Violet, 3f + pulse * 2f, point)
            }
        }
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("A M A R", color = Ice, fontSize = 42.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
            Text("AURORA NEXUS", color = Cyan, fontSize = 13.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(28.dp))
            GlassPill("دخول إلى النظام  ›", Cyan)
            Spacer(Modifier.height(12.dp))
            Text("DEMO • LOCAL • SAFE", color = Color.White.copy(alpha = 0.42f), fontSize = 9.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun NexusWorld(rooms: List<AmarRoom>, selected: Int, orbit: Float, flow: Float, pulse: Float, focus: Int, onDrag: (Float) -> Unit, onSelect: (Int) -> Unit, onFocus: (Int) -> Unit, onLegacy: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        NexusHeader(rooms[selected], pulse)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxSize()) {
            SpatialRail(rooms, selected, orbit, flow, onDrag, onSelect, Modifier.fillMaxHeight().width(126.dp))
            LivingRoom(rooms[selected], flow, pulse, focus, onFocus, onLegacy, Modifier.fillMaxSize().padding(start = 136.dp))
        }
    }
}

@Composable
private fun NexusHeader(room: AmarRoom, pulse: Float) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color.White.copy(alpha = 0.055f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp * pulse).clip(CircleShape).background(Green))
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text("AMAR / NEXUS", color = Ice, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("${room.emoji} ${room.titleAr}  •  XAUUSD  •  M5", color = Color.White.copy(alpha = 0.48f), fontSize = 10.sp)
        }
        Text("DEMO", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(14.dp))
        Text("LIVE", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SpatialRail(rooms: List<AmarRoom>, selected: Int, orbit: Float, flow: Float, onDrag: (Float) -> Unit, onSelect: (Int) -> Unit, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(30.dp)).background(Color.White.copy(alpha = 0.035f)).pointerInput(Unit) { detectDragGestures { _, drag -> onDrag(drag.y) } }) {
        Canvas(Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            drawOval(Cyan.copy(alpha = 0.08f), center, size.width * 0.31f, size.height * 0.38f, style = Stroke(1.5f))
            drawOval(Violet.copy(alpha = 0.06f), center, size.width * 0.23f, size.height * 0.27f, style = Stroke(1f))
        }
        rooms.forEachIndexed { index, room ->
            val angle = (index - selected) * 0.72f + orbit + flow * 0.12f
            val depth = (cos(angle) + 1f) / 2f
            val y = 0.50f + sin(angle) * 0.40f
            val active = index == selected
            Box(Modifier.align(Alignment.TopCenter).offset(y = (y * 900f - 450f).dp).graphicsLayer {
                val scale = 0.70f + depth * 0.34f + if (active) 0.08f else 0f
                scaleX = scale
                scaleY = scale
                alpha = 0.35f + depth * 0.65f
                rotationY = sin(angle) * 24f
                cameraDistance = 28f * density
            }.clip(RoundedCornerShape(18.dp)).background(if (active) Cyan.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.18f)).clickable { onSelect(index) }.padding(horizontal = 8.dp, vertical = 9.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(room.emoji, fontSize = if (active) 24.sp else 18.sp)
                    Text(room.titleAr.take(9), color = if (active) Ice else Color.White.copy(alpha = 0.45f), fontSize = 8.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun LivingRoom(room: AmarRoom, flow: Float, pulse: Float, focus: Int, onFocus: (Int) -> Unit, onLegacy: () -> Unit, modifier: Modifier) {
    Column(modifier) {
        Text(room.titleAr, color = Ice, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("${room.name.replace('_', ' ')}  •  LIVING SPACE", color = Cyan, fontSize = 9.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        when (room) {
            AmarRoom.COMMAND_CENTER -> CommandRoom(flow, pulse, focus, onFocus)
            AmarRoom.MARKET -> MarketRoom(flow, focus, onFocus)
            AmarRoom.CHART -> ChartRoom(flow, focus, onFocus)
            AmarRoom.BOT_LAB -> GridRoom(flow, pulse, focus, onFocus, onLegacy)
            AmarRoom.RISK -> RiskRoom(flow, pulse, focus, onFocus)
            AmarRoom.POSITIONS -> DataRoom("ORDER MATRIX", listOf("6 OPEN", "18 PENDING", "+12.84 FLOAT", "SPREAD 0.12"), Cyan, focus, onFocus)
            AmarRoom.PERFORMANCE -> DataRoom("PERFORMANCE ENGINE", listOf("+18.40 TODAY", "+127.40 TOTAL", "66.7% WIN", "2.1% DD"), Gold, focus, onFocus)
            AmarRoom.INDICATORS -> DataRoom("SIGNAL CONSTELLATION", listOf("EMA TREND", "RSI 64", "VWAP ABOVE", "ATR HIGH"), Violet, focus, onFocus)
            AmarRoom.ANALYSIS -> DataRoom("ANALYSIS MATRIX", listOf("BUY BIAS", "82 SCORE", "MOMENTUM", "M5 REGIME"), Cyan, focus, onFocus)
            AmarRoom.TESTING -> DataRoom("SIMULATION DECK", listOf("BACKTEST", "FORWARD", "MONTE CARLO", "PARAMETERS"), Pink, focus, onFocus)
            AmarRoom.TOOLS -> DataRoom("TOOL DECK", listOf("RISK SIZE", "LOT SIZE", "TP / SL", "GRID CALC"), Gold, focus, onFocus)
            AmarRoom.ALERTS -> DataRoom("ALERT STREAM", listOf("CONNECTION", "RISK ARMED", "TP / SL", "REBUILD"), Pink, focus, onFocus)
            AmarRoom.LIBRARY -> DataRoom("AMAR LIBRARY", listOf("CORE", "INDICATORS", "STRATEGIES", "EXTENSIONS"), Violet, focus, onFocus)
            AmarRoom.SETTINGS -> DataRoom("SYSTEM CONTROL", listOf("MOTION", "AURORA", "SECURITY", "DEMO MODE"), Cyan, focus, onFocus)
        }
    }
}

@Composable
private fun CommandRoom(flow: Float, pulse: Float, focus: Int, onFocus: (Int) -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Reactor("AMAR CORE", "MONITORING", Cyan, flow, pulse, Modifier.weight(1.25f))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SignalNode("MARKET", "BUY • 82%", Cyan, focus == 0) { onFocus(0) }
            SignalNode("GRID ENGINE", "ACTIVE / DEMO", Green, focus == 1) { onFocus(1) }
            SignalNode("RISK", "SAFE • 5%", Gold, focus == 2) { onFocus(2) }
            SignalNode("EQUITY", "$1,012.84", Violet, focus == 3) { onFocus(3) }
        }
    }
}

@Composable
private fun MarketRoom(flow: Float, focus: Int, onFocus: (Int) -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MarketChart(flow, Cyan, Modifier.weight(1.45f))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SignalNode("DIRECTION", "BUY", Cyan, focus == 0) { onFocus(0) }
            SignalNode("STRENGTH", "82%", Green, focus == 1) { onFocus(1) }
            SignalNode("VOLATILITY", "HIGH", Pink, focus == 2) { onFocus(2) }
            SignalNode("SESSION", "LONDON", Gold, focus == 3) { onFocus(3) }
        }
    }
}

@Composable
private fun ChartRoom(flow: Float, focus: Int, onFocus: (Int) -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MarketChart(flow, Cyan, Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("EMA", "VWAP", "FVG", "BOS", "TP", "SL").forEachIndexed { i, label ->
                SignalNode(label, if (i % 2 == 0) "ON" else "LIVE", if (i % 3 == 0) Cyan else Violet, focus == i) { onFocus(i) }
            }
        }
    }
}

@Composable
private fun GridRoom(flow: Float, pulse: Float, focus: Int, onFocus: (Int) -> Unit, onLegacy: () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Reactor("AMAR GRID", "ANALYZING", Gold, flow, pulse, Modifier.weight(1.15f))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            DialNode("LOT", "0.02", Gold, focus == 0) { onFocus(0) }
            DialNode("GRID STEP", "40", Cyan, focus == 1) { onFocus(1) }
            DialNode("MAX ORDERS", "30", Violet, focus == 2) { onFocus(2) }
            DialNode("MULTIPLIER", "2.00", Pink, focus == 3) { onFocus(3) }
            SignalNode("EXISTING GRID ENGINE", "OPEN", Gold, focus == 4) { onFocus(4); onLegacy() }
        }
    }
}

@Composable
private fun RiskRoom(flow: Float, pulse: Float, focus: Int, onFocus: (Int) -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Reactor("RISK RADAR", "PROTECTED", Green, flow, pulse, Modifier.weight(1.1f))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            DialNode("RISK", "5%", Green, focus == 0) { onFocus(0) }
            DialNode("DAILY LOSS", "10%", Gold, focus == 1) { onFocus(1) }
            DialNode("MAX LOT", "0.20", Cyan, focus == 2) { onFocus(2) }
            DialNode("BASKET SL", "$100", Red, focus == 3) { onFocus(3) }
        }
    }
}

@Composable
private fun DataRoom(title: String, values: List<String>, accent: Color, focus: Int, onFocus: (Int) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEachIndexed { i, value -> SignalNode("NODE ${i + 1}", value, accent, focus == i) { onFocus(i) } }
        }
        Spacer(Modifier.height(10.dp))
        Reactor(title, "LIVE DATA", accent, 0f, 1f, Modifier.fillMaxSize())
    }
}

@Composable
private fun Reactor(title: String, status: String, accent: Color, flow: Float, pulse: Float, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(Color.White.copy(alpha = 0.045f))) {
        Canvas(Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f + sin(flow * 6.283f) * 8f)
            for (i in 0..6) drawCircle(accent.copy(alpha = 0.025f + i * 0.014f), size.minDimension * (0.15f + i * 0.045f), center, style = Stroke(1.1f))
            drawCircle(accent.copy(alpha = 0.28f), size.minDimension * 0.17f * pulse, center, style = Stroke(3f))
            for (i in 0..8) {
                val a = flow * 6.283f + i * 0.70f
                drawCircle(accent.copy(alpha = 0.72f), 3f + pulse, androidx.compose.ui.geometry.Offset(center.x + cos(a) * size.minDimension * 0.22f, center.y + sin(a) * size.minDimension * 0.22f))
            }
        }
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("◈", color = accent, fontSize = 48.sp)
            Text(title, color = Ice, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text(status, color = accent, fontSize = 9.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(12.dp))
            Text("LIVE • REACTIVE", color = Green, fontSize = 9.sp)
        }
    }
}

@Composable
private fun MarketChart(flow: Float, accent: Color, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(26.dp)).background(Color.White.copy(alpha = 0.04f))) {
        Canvas(Modifier.fillMaxSize().padding(14.dp)) {
            for (i in 0..10) {
                val y = size.height * i / 10f
                drawLine(Color.White.copy(alpha = 0.035f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f)
            }
            val path = Path()
            for (i in 0..90) {
                val x = size.width * i / 90f
                val y = size.height * 0.58f - sin(i * 0.27f + flow * 5f) * size.height * 0.11f - i * size.height * 0.0023f
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, accent, style = Stroke(3f))
            drawCircle(Green, 5f, androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.34f))
        }
        Text("XAUUSD  •  M5", Modifier.align(Alignment.TopStart).padding(14.dp), color = Ice, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("BUY 82%", Modifier.align(Alignment.TopEnd).padding(14.dp), color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SignalNode(label: String, value: String, accent: Color, active: Boolean, onClick: () -> Unit) {
    Box(Modifier.width(175.dp).clip(RoundedCornerShape(18.dp)).background(if (active) accent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.045f)).clickable { onClick() }.padding(12.dp)) {
        Column {
            Text(label, color = accent, fontSize = 8.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(value, color = Ice, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Box(Modifier.fillMaxWidth().height(2.dp).clip(CircleShape).background(accent.copy(alpha = if (active) 0.9f else 0.35f)))
        }
    }
}

@Composable
private fun DialNode(label: String, value: String, accent: Color, active: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (active) accent.copy(alpha = 0.13f) else Color.White.copy(alpha = 0.045f)).clickable { onClick() }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f))) {
            Text("◌", Modifier.align(Alignment.Center), color = accent, fontSize = 20.sp)
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Color.White.copy(alpha = 0.48f), fontSize = 8.sp, letterSpacing = 1.sp)
            Text(value, color = Ice, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Text("DRAG", color = accent, fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GlassPill(text: String, accent: Color) {
    Box(Modifier.clip(CircleShape).background(accent.copy(alpha = 0.13f)).padding(horizontal = 30.dp, vertical = 14.dp)) {
        Text(text, color = Ice, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
