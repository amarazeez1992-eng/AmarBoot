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

private val Bg = Color(0xFF02040B)
private val Ice = Color(0xFFF4FBFF)
private val Cyan = Color(0xFF35E8FF)
private val Violet = Color(0xFFA66CFF)
private val Green = Color(0xFF42F0A0)
private val Gold = Color(0xFFFFD166)
private val Pink = Color(0xFFFF58C8)

@Composable
fun AmarAuroraShell(initialState: AmarAppState = AmarAppState(), onOpenLegacyGrid: () -> Unit = {}) {
    var entered by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(0) }
    var orbit by remember { mutableFloatStateOf(0f) }
    val rooms = AmarRoom.values().toList()
    val motion = rememberInfiniteTransition(label = "amar_motion")
    val flow by motion.animateFloat(0f, 1f, infiniteRepeatable(tween(7000), RepeatMode.Reverse), label = "flow")
    val pulse by motion.animateFloat(0.75f, 1.15f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "pulse")
    Box(Modifier.fillMaxSize().background(Bg)) {
        Aurora(flow, pulse)
        if (!entered) Entry(pulse) { entered = true }
        else Home(rooms, selected, orbit, flow, pulse, { orbit += it / 240f }, { selected = it }, onOpenLegacyGrid)
    }
}

@Composable private fun Aurora(flow: Float, pulse: Float) {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.radialGradient(listOf(Cyan.copy(alpha = .18f * pulse), Color.Transparent), center = androidx.compose.ui.geometry.Offset(size.width * (.18f + flow * .15f), size.height * .12f), radius = size.maxDimension * .72f))
        drawRect(Brush.radialGradient(listOf(Violet.copy(alpha = .16f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(size.width * (.82f - flow * .12f), size.height * .86f), radius = size.maxDimension * .70f))
        for (i in 0..18) {
            val y = size.height * i / 18f
            drawLine(Color.White.copy(alpha = .018f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f)
        }
    }
}

@Composable private fun Entry(pulse: Float, onEnter: () -> Unit) {
    Box(Modifier.fillMaxSize().clickable { onEnter() }) {
        Canvas(Modifier.fillMaxSize()) {
            val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension * .22f
            for (i in 0..7) drawCircle(Cyan.copy(alpha = .025f + i * .012f), r + i * 26f, c, style = Stroke(1f))
            drawCircle(Cyan.copy(alpha = .30f), r, c, style = Stroke(3f))
            drawCircle(Violet.copy(alpha = .22f), r * .68f, c, style = Stroke(2f))
            drawCircle(Cyan, 5f + pulse * 3f, c)
        }
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("عمار", color = Ice, fontSize = 42.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
            Spacer(Modifier.height(6.dp)); Text("نظام عمار المتقدم", color = Cyan, fontSize = 13.sp)
            Spacer(Modifier.height(28.dp)); GlassButton("دخول إلى النظام  ›", Cyan, onEnter)
            Spacer(Modifier.height(12.dp)); Text("تجريبي • محلي • آمن", color = Color.White.copy(alpha = .42f), fontSize = 9.sp)
        }
    }
}

@Composable private fun Home(rooms: List<AmarRoom>, selected: Int, orbit: Float, flow: Float, pulse: Float, onDrag: (Float) -> Unit, onRoom: (Int) -> Unit, onLegacy: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Header(rooms[selected], pulse); Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Rail(rooms, selected, orbit, flow, onDrag, onRoom, Modifier.width(126.dp).fillMaxSize())
            Stage(rooms[selected], flow, pulse, onLegacy, Modifier.weight(1f).fillMaxSize())
        }
    }
}

@Composable private fun Header(room: AmarRoom, pulse: Float) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color.White.copy(alpha = .055f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size((8f * pulse).dp).clip(CircleShape).background(Green)); Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) { Text("نظام عمار", color = Ice, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("${room.emoji} ${room.titleAr} • XAUUSD • M5", color = Color.White.copy(alpha = .48f), fontSize = 10.sp) }
        Text("تجريبي", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(14.dp)); Text("متصل", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun Rail(rooms: List<AmarRoom>, selected: Int, orbit: Float, flow: Float, onDrag: (Float) -> Unit, onRoom: (Int) -> Unit, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(30.dp)).background(Color.White.copy(alpha = .035f)).pointerInput(Unit) { detectDragGestures { _, drag -> onDrag(drag.y) } }) {
        rooms.forEachIndexed { index, room ->
            val angle = (index - selected) * .72f + orbit + flow * .08f
            val depth = (cos(angle) + 1f) / 2f
            val y = ((sin(angle) * .38f) + .5f) * 330f - 165f
            val active = index == selected
            Box(
                Modifier.align(Alignment.TopCenter).offset(y = y.dp).graphicsLayer {
                    val s = .72f + depth * .28f + if (active) .08f else 0f
                    scaleX = s; scaleY = s; alpha = .38f + depth * .62f; rotationZ = sin(angle) * 7f
                }.clip(RoundedCornerShape(18.dp)).background(if (active) Cyan.copy(alpha = .14f) else Color.Black.copy(alpha = .18f)).clickable { onRoom(index) }.padding(horizontal = 8.dp, vertical = 9.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(room.emoji, fontSize = if (active) 23.sp else 18.sp); Text(room.titleAr.take(9), color = if (active) Ice else Color.White.copy(alpha = .45f), fontSize = 8.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) }
            }
        }
    }
}

@Composable private fun Stage(room: AmarRoom, flow: Float, pulse: Float, onLegacy: () -> Unit, modifier: Modifier) {
    Column(modifier) {
        Text(room.titleAr, color = Ice, fontSize = 27.sp, fontWeight = FontWeight.Bold); Text("${room.titleAr} • مساحة العمل", color = Cyan, fontSize = 9.sp); Spacer(Modifier.height(10.dp))
        when (room) {
            AmarRoom.COMMAND_CENTER -> Command(flow, pulse)
            AmarRoom.MARKET -> Chart(flow, "مصفوفة السوق", Cyan)
            AmarRoom.CHART -> Chart(flow, "XAUUSD • M5", Cyan)
            AmarRoom.BOT_LAB -> Bot(flow, pulse, onLegacy)
            AmarRoom.RISK -> Risk(flow, pulse)
            AmarRoom.POSITIONS -> Matrix("مصفوفة الصفقات", listOf("6 مفتوحة", "18 معلقة", "+12.84 عائم", "فارق 0.12"), Cyan)
            AmarRoom.PERFORMANCE -> Matrix("الأداء", listOf("+18.40 اليوم", "+127.40 الإجمالي", "66.7% نجاح", "2.1% سحب"), Gold)
            AmarRoom.INDICATORS -> Matrix("مجموعة المؤشرات", listOf("اتجاه", "زخم", "سعر مرجعي", "تذبذب"), Violet)
            AmarRoom.ANALYSIS -> Matrix("مصفوفة التحليل", listOf("ميل شراء", "82 نتيجة", "زخم", "نظام M5"), Cyan)
            AmarRoom.TESTING -> Matrix("منصة الاختبار", listOf("اختبار خلفي", "اختبار أمامي", "مونت كارلو", "المعاملات"), Pink)
            AmarRoom.TOOLS -> Matrix("أدوات التداول", listOf("حجم المخاطرة", "حجم اللوت", "TP / SL", "حاسبة الشبكة"), Gold)
            AmarRoom.ALERTS -> Matrix("تيار التنبيهات", listOf("الاتصال", "الحماية مفعلة", "TP / SL", "إعادة البناء"), Pink)
            AmarRoom.LIBRARY -> Matrix("مكتبة عمار", listOf("النواة", "المؤشرات", "الاستراتيجيات", "الإضافات"), Violet)
            AmarRoom.ACCOUNTS -> Matrix("حسابات التداول", listOf("حسابات محفوظة", "بيانات محمية", "الاتصال", "التنفيذ محمي"), Gold)
            AmarRoom.SETTINGS -> Matrix("تحكم النظام", listOf("الحركة", "الإضاءة", "الأمان", "الوضع التجريبي"), Cyan)
        }
    }
}

@Composable private fun Command(flow: Float, pulse: Float) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Core("نواة عمار", "مراقبة", Cyan, flow, pulse, Modifier.weight(1.2f)); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { Node("السوق", "شراء • 82%", Cyan); Node("شبكة عمار", "جاهز • تجريبي", Green); Node("المخاطر", "آمن • 5%", Gold); Node("حقوق الملكية", "$1,012.84", Violet) } }
}

@Composable private fun Chart(flow: Float, title: String, accent: Color) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White.copy(alpha = .035f))) {
            Canvas(Modifier.fillMaxSize()) {
                val path = Path(); val count = 48
                for (i in 0 until count) { val x = size.width * i / (count - 1).toFloat(); val wave = sin(i * .43f + flow * 3.2f) * .15f + sin(i * .16f) * .08f; val y = size.height * (.52f - wave); if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }
                drawPath(path, accent, style = Stroke(3f))
                for (i in 0..6) { val y = size.height * i / 6f; drawLine(Color.White.copy(alpha = .035f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f) }
            }
            Text(title, Modifier.align(Alignment.TopStart).padding(14.dp), color = Ice, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text("شراء 82%", Modifier.align(Alignment.TopEnd).padding(14.dp), color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { Node("الاتجاه", "مفعل", accent); Node("السعر المرجعي", "مباشر", Violet); Node("الفجوة", "مفعل", Cyan); Node("الهيكل", "مباشر", Gold); Node("الهدف", "جاهز", Green); Node("الإيقاف", "جاهز", Pink) }
    }
}

@Composable private fun Bot(flow: Float, pulse: Float, onLegacy: () -> Unit) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(1.2f).fillMaxSize().clip(RoundedCornerShape(28.dp)).background(Color.White.copy(alpha = .045f))) {
            Canvas(Modifier.fillMaxSize()) { val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f + sin(flow * 6.28f) * 10f); drawCircle(Cyan.copy(alpha = .08f), size.minDimension * .34f, c); drawCircle(Cyan.copy(alpha = .24f), size.minDimension * .20f, c, style = Stroke(3f)); drawCircle(Violet.copy(alpha = .18f), size.minDimension * .28f, c, style = Stroke(1f)); drawCircle(Cyan, 7f * pulse, androidx.compose.ui.geometry.Offset(c.x - 24f, c.y - 8f)); drawCircle(Cyan, 7f * pulse, androidx.compose.ui.geometry.Offset(c.x + 24f, c.y - 8f)) }
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) { Text("شبكة عمار", color = Ice, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text("تحليل • تجريبي", color = Green, fontSize = 9.sp) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { Node("الحالة", "جاهز • تجريبي", Green); Node("اللوت", "0.02", Cyan); Node("خطوة الشبكة", "40 نقطة", Violet); Node("أقصى أوامر", "30", Gold); GlassButton("فتح وحدة التحكم ›", Cyan, onLegacy) }
    }
}

@Composable private fun Risk(flow: Float, pulse: Float) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Core("نواة المخاطر", "مفعلة", Gold, flow, pulse, Modifier.weight(1f)); Column(Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("إعدادات المخاطر", color = Color.White.copy(alpha = .45f), fontSize = 9.sp); Node("إعداد 01", "5%", Green); Node("إعداد 02", "10%", Gold); Node("إعداد 03", "25%", Gold); Node("إعداد 04", "27%", Pink) } }
}

@Composable private fun Matrix(title: String, values: List<String>, accent: Color) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(9.dp)) { Core(title, "مصفوفة مباشرة", accent, .25f, 1f, Modifier.fillMaxWidth().weight(1f)); Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { values.forEachIndexed { i, v -> Node("عنصر ${i + 1}", v, accent) } } }
}

@Composable private fun Core(title: String, subtitle: String, accent: Color, flow: Float, pulse: Float, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(accent.copy(alpha = .13f), Color.White.copy(alpha = .035f))))) {
        Canvas(Modifier.fillMaxSize()) { val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f); val r = size.minDimension * .23f; drawCircle(accent.copy(alpha = .06f), r * 1.9f, c); drawCircle(accent.copy(alpha = .18f), r * 1.25f, c, style = Stroke(2f)); drawCircle(accent.copy(alpha = .30f), r, c, style = Stroke(3f)); val a = flow * 6.28f; drawCircle(accent, 5f + pulse * 3f, androidx.compose.ui.geometry.Offset(c.x + cos(a) * r * 1.45f, c.y + sin(a) * r * 1.45f)) }
        Column(Modifier.align(Alignment.TopStart).padding(16.dp)) { Text(title, color = Ice, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = accent, fontSize = 8.sp) }
    }
}

@Composable private fun Node(title: String, value: String, accent: Color) {
    Column(Modifier.width(108.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = .045f)).padding(11.dp)) { Box(Modifier.size(7.dp).clip(CircleShape).background(accent)); Spacer(Modifier.height(7.dp)); Text(title, color = Color.White.copy(alpha = .42f), fontSize = 8.sp); Text(value, color = Ice, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
}

@Composable private fun GlassButton(text: String, accent: Color, onClick: (() -> Unit)? = null) {
    Row(Modifier.clip(RoundedCornerShape(50.dp)).background(accent.copy(alpha = .14f)).clickable(enabled = onClick != null) { onClick?.invoke() }.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).clip(CircleShape).background(accent)); Spacer(Modifier.width(8.dp)); Text(text, color = Ice, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
}
