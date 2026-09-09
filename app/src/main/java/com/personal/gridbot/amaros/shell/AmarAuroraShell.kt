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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.navigation.AmarRoom
import kotlin.math.cos
import kotlin.math.sin

private val AmarBg = Color(0xFF02040B)
private val AmarIce = Color(0xFFF4FBFF)
private val AmarCyan = Color(0xFF35E8FF)
private val AmarViolet = Color(0xFFA66CFF)
private val AmarGreen = Color(0xFF42F0A0)
private val AmarGold = Color(0xFFFFD166)
private val AmarPink = Color(0xFFFF58C8)

@Composable
fun AmarAuroraShell(
    initialState: AmarAppState = AmarAppState(),
    onOpenLegacyGrid: () -> Unit = {}
) {
    var entered by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    var orbit by remember { mutableFloatStateOf(0f) }
    val rooms = AmarRoom.values().toList()
    val transition = rememberInfiniteTransition(label = "amar_motion")
    val flow by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
        label = "flow"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(Modifier.fillMaxSize().background(AmarBg)) {
        AuroraField(flow, pulse)
        if (!entered) {
            EntryPortal(flow, pulse) { entered = true }
        } else {
            NexusHome(
                rooms = rooms,
                selectedIndex = selectedIndex,
                orbit = orbit,
                flow = flow,
                pulse = pulse,
                onOrbitDrag = { orbit += it / 240f },
                onRoom = { selectedIndex = it },
                onLegacy = onOpenLegacyGrid
            )
        }
    }
}

@Composable
private fun AuroraField(flow: Float, pulse: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            Brush.radialGradient(
                listOf(AmarCyan.copy(alpha = 0.22f * pulse), Color.Transparent),
                androidx.compose.ui.geometry.Offset(w * (0.15f + flow * 0.18f), h * 0.15f),
                w * 0.75f
            )
        )
        drawRect(
            Brush.radialGradient(
                listOf(AmarViolet.copy(alpha = 0.18f), Color.Transparent),
                androidx.compose.ui.geometry.Offset(w * (0.85f - flow * 0.15f), h * 0.85f),
                w * 0.70f
            )
        )
        for (i in 0..16) {
            val y = h * i / 16f
            drawLine(Color.White.copy(alpha = 0.018f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(w, y), 1f)
        }
    }
}

@Composable
private fun EntryPortal(flow: Float, pulse: Float, onEnter: () -> Unit) {
    Box(Modifier.fillMaxSize().clickable { onEnter() }) {
        Canvas(Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension * 0.22f
            for (i in 0..7) {
                drawCircle(AmarCyan.copy(alpha = 0.025f + i * 0.012f), r + i * 28f, center, style = Stroke(1f))
            }
            drawCircle(AmarCyan.copy(alpha = 0.32f), r, center, style = Stroke(3f))
            drawCircle(AmarViolet.copy(alpha = 0.22f), r * 0.68f, center, style = Stroke(2f))
            for (i in 0..15) {
                val a = flow * 6.283f + i * 0.392f
                val p = androidx.compose.ui.geometry.Offset(center.x + cos(a) * r * 1.25f, center.y + sin(a) * r * 1.25f)
                drawCircle(if (i % 2 == 0) AmarCyan else AmarViolet, 3f + pulse * 2f, p)
            }
        }
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("A M A R", color = AmarIce, fontSize = 42.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
            Spacer(Modifier.height(6.dp))
            Text("AURORA NEXUS", color = AmarCyan, fontSize = 13.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(28.dp))
            GlassButton("دخول إلى النظام  ›", AmarCyan)
            Spacer(Modifier.height(12.dp))
            Text("DEMO • LOCAL • SAFE", color = Color.White.copy(alpha = 0.42f), fontSize = 9.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun NexusHome(
    rooms: List<AmarRoom>,
    selectedIndex: Int,
    orbit: Float,
    flow: Float,
    pulse: Float,
    onOrbitDrag: (Float) -> Unit,
    onRoom: (Int) -> Unit,
    onLegacy: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        val room = rooms[selectedIndex]
        Header(room, pulse)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OrbitRail(rooms, selectedIndex, orbit, flow, onOrbitDrag, onRoom, Modifier.width(124.dp).fillMaxSize())
            RoomStage(room, flow, pulse, onLegacy, Modifier.weight(1f).fillMaxSize())
        }
    }
}

@Composable
private fun Header(room: AmarRoom, pulse: Float) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color.White.copy(alpha = 0.055f)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(9.dp * pulse).clip(CircleShape).background(AmarGreen))
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text("AMAR / NEXUS", color = AmarIce, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("${room.emoji} ${room.titleAr}  •  XAUUSD  •  M5", color = Color.White.copy(alpha = 0.48f), fontSize = 10.sp)
        }
        Text("DEMO", color = AmarGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(14.dp))
        Text("ONLINE", color = AmarGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OrbitRail(
    rooms: List<AmarRoom>, selected: Int, orbit: Float, flow: Float,
    onDrag: (Float) -> Unit, onRoom: (Int) -> Unit, modifier: Modifier
) {
    Box(
        modifier.clip(RoundedCornerShape(30.dp)).background(Color.White.copy(alpha = 0.035f))
            .pointerInput(Unit) { detectDragGestures { _, drag -> onDrag(drag.y) } }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            drawCircle(AmarCyan.copy(alpha = 0.05f), size.minDimension * 0.34f, c, style = Stroke(1f))
            drawCircle(AmarViolet.copy(alpha = 0.05f), size.minDimension * 0.24f, c, style = Stroke(1f))
        }
        rooms.forEachIndexed { index, item ->
            val angle = (index - selected) * 0.72f + orbit + flow * 0.08f
            val depth = (cos(angle) + 1f) / 2f
            val y = 0.50f + sin(angle) * 0.40f
            val active = index == selected
            Box(
                Modifier.align(Alignment.TopCenter)
                    .offset(y = (y * 900f - 450f).dp)
                    .graphicsLayer {
                        val s = 0.70f + depth * 0.32f + if (active) 0.08f else 0f
                        scaleX = s
                        scaleY = s
                        alpha = 0.35f + depth * 0.65f
                        rotationY = sin(angle) * 22f
                        cameraDistance = 28f * density
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (active) AmarCyan.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.18f))
                    .clickable { onRoom(index) }
                    .padding(horizontal = 8.dp, vertical = 9.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(item.emoji, fontSize = if (active) 24.sp else 18.sp)
                    Text(item.titleAr.take(9), color = if (active) AmarIce else Color.White.copy(alpha = 0.45f), fontSize = 8.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun RoomStage(room: AmarRoom, flow: Float, pulse: Float, onLegacy: () -> Unit, modifier: Modifier) {
    Column(modifier) {
        Text(room.titleAr, color = AmarIce, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("${room.name.replace('_', ' ')}  •  LIVING SPACE", color = AmarCyan, fontSize = 9.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        when (room) {
            AmarRoom.COMMAND_CENTER -> CommandStage(flow, pulse)
            AmarRoom.MARKET -> ChartStage(flow, "MARKET MATRIX", AmarCyan)
            AmarRoom.CHART -> ChartStage(flow, "XAUUSD • M5", AmarCyan)
            AmarRoom.BOT_LAB -> BotStage(flow, pulse, onLegacy)
            AmarRoom.RISK -> RiskStage(flow, pulse)
            AmarRoom.POSITIONS -> MatrixStage("ORDER MATRIX", listOf("6 OPEN", "18 PENDING", "+12.84 FLOAT", "SPREAD 0.12"), AmarCyan)
            AmarRoom.PERFORMANCE -> MatrixStage("PERFORMANCE", listOf("+18.40 TODAY", "+127.40 TOTAL", "66.7% WIN", "2.1% DD"), AmarGold)
            AmarRoom.INDICATORS -> MatrixStage("SIGNAL CONSTELLATION", listOf("EMA TREND", "RSI 64", "VWAP ABOVE", "ATR HIGH"), AmarViolet)
            AmarRoom.ANALYSIS -> MatrixStage("ANALYSIS MATRIX", listOf("BUY BIAS", "82 SCORE", "MOMENTUM", "M5 REGIME"), AmarCyan)
            AmarRoom.TESTING -> MatrixStage("SIMULATION DECK", listOf("BACKTEST", "FORWARD", "MONTE CARLO", "PARAMETERS"), AmarPink)
            AmarRoom.TOOLS -> MatrixStage("TOOL DECK", listOf("RISK SIZE", "LOT SIZE", "TP / SL", "GRID CALC"), AmarGold)
            AmarRoom.ALERTS -> MatrixStage("ALERT STREAM", listOf("CONNECTION", "RISK ARMED", "TP / SL", "REBUILD"), AmarPink)
            AmarRoom.LIBRARY -> MatrixStage("AMAR LIBRARY", listOf("CORE", "INDICATORS", "STRATEGIES", "EXTENSIONS"), AmarViolet)
            AmarRoom.SETTINGS -> MatrixStage("SYSTEM CONTROL", listOf("MOTION", "AURORA", "SECURITY", "DEMO MODE"), AmarCyan)
        }
    }
}

@Composable
private fun CommandStage(flow: Float, pulse: Float) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CoreStage("AMAR CORE", "MONITORING", AmarCyan, flow, pulse, Modifier.weight(1.25f))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Node("MARKET", "BUY • 82%", AmarCyan)
            Node("AMAR GRID", "READY • DEMO", AmarGreen)
            Node("RISK", "SAFE • 5%", AmarGold)
            Node("EQUITY", "$1,012.84", AmarViolet)
        }
    }
}

@Composable
private fun ChartStage(flow: Float, title: String, accent: Color) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White.copy(alpha = 0.035f))) {
            Canvas(Modifier.fillMaxSize()) {
                val path = Path()
                val count = 48
                for (i in 0 until count) {
                    val x = size.width * i / (count - 1).toFloat()
                    val wave = sin(i * 0.43f + flow * 3.2f) * 0.15f + sin(i * 0.16f) * 0.08f
                    val y = size.height * (0.52f - wave)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, accent, style = Stroke(3f))
                for (i in 0..6) {
                    val y = size.height * i / 6f
                    drawLine(Color.White.copy(alpha = 0.035f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f)
                }
            }
            Text(title, Modifier.align(Alignment.TopStart).padding(14.dp), color = AmarIce, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("BUY 82%", Modifier.align(Alignment.TopEnd).padding(14.dp), color = AmarGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Node("EMA", "ON", accent)
            Node("VWAP", "LIVE", AmarViolet)
            Node("FVG", "ON", AmarCyan)
            Node("BOS", "LIVE", AmarGold)
            Node("TP", "READY", AmarGreen)
            Node("SL", "READY", AmarPink)
        }
    }
}

@Composable
private fun BotStage(flow: Float, pulse: Float, onLegacy: () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(1.2f).fillMaxSize().clip(RoundedCornerShape(28.dp)).background(Color.White.copy(alpha = 0.045f))) {
            Canvas(Modifier.fillMaxSize()) {
                val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f + sin(flow * 6.28f) * 10f)
                drawCircle(AmarCyan.copy(alpha = 0.08f), size.minDimension * 0.34f, c)
                drawCircle(AmarCyan.copy(alpha = 0.24f), size.minDimension * 0.20f, c, style = Stroke(3f))
                drawCircle(AmarViolet.copy(alpha = 0.18f), size.minDimension * 0.28f, c, style = Stroke(1f))
                drawCircle(AmarCyan, 7f * pulse, androidx.compose.ui.geometry.Offset(c.x - 24f, c.y - 8f))
                drawCircle(AmarCyan, 7f * pulse, androidx.compose.ui.geometry.Offset(c.x + 24f, c.y - 8f))
            }
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text("AMAR GRID", color = AmarIce, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("ANALYZING • DEMO", color = AmarGreen, fontSize = 9.sp, letterSpacing = 2.sp)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Node("STATUS", "READY • DEMO", AmarGreen)
            Node("LOT", "0.02", AmarCyan)
            Node("GRID STEP", "40 POINTS", AmarViolet)
            Node("MAX ORDERS", "30", AmarGold)
            GlassButton("فتح وحدة التحكم ›", AmarCyan, onLegacy)
        }
    }
}

@Composable
private fun RiskStage(flow: Float, pulse: Float) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CoreStage("RISK CORE", "ARMED", AmarGold, flow, pulse, Modifier.weight(1f))
        Column(Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("RISK PRESETS", color = Color.White.copy(alpha = 0.45f), fontSize = 9.sp, letterSpacing = 2.sp)
            Node("PRESET 01", "5%", AmarGreen)
            Node("PRESET 02", "10%", AmarGold)
            Node("PRESET 03", "25%", AmarGold)
            Node("PRESET 04", "27%", AmarPink)
        }
    }
}

@Composable
private fun MatrixStage(title: String, values: List<String>, accent: Color) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        CoreStage(title, "LIVE MATRIX", accent, 0.25f, 1f, Modifier.fillMaxWidth().weight(1f))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEachIndexed { index, value -> Node("NODE ${index + 1}", value, accent) }
        }
    }
}

@Composable
private fun CoreStage(title: String, subtitle: String, accent: Color, flow: Float, pulse: Float, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(accent.copy(alpha = 0.13f), Color.White.copy(alpha = 0.035f))))) {
        Canvas(Modifier.fillMaxSize()) {
            val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension * 0.23f
            drawCircle(accent.copy(alpha = 0.06f), r * 1.9f, c)
            drawCircle(accent.copy(alpha = 0.18f), r * 1.25f, c, style = Stroke(2f))
            drawCircle(accent.copy(alpha = 0.30f), r, c, style = Stroke(3f))
            val a = flow * 6.28f
            drawCircle(accent, 5f + pulse * 3f, androidx.compose.ui.geometry.Offset(c.x + cos(a) * r * 1.45f, c.y + sin(a) * r * 1.45f))
        }
        Column(Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Text(title, color = AmarIce, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text(subtitle, color = accent, fontSize = 8.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun Node(title: String, value: String, accent: Color) {
    Column(Modifier.width(108.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.045f)).padding(11.dp)) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.height(7.dp))
        Text(title, color = Color.White.copy(alpha = 0.42f), fontSize = 8.sp, letterSpacing = 1.sp)
        Text(value, color = AmarIce, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GlassButton(text: String, accent: Color, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.clip(RoundedCornerShape(50.dp)).background(accent.copy(alpha = 0.14f))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(8.dp))
        Text(text, color = AmarIce, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
