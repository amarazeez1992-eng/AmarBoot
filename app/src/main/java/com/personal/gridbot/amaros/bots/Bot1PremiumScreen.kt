package com.personal.gridbot.amaros.bots

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF07121B)
private val Panel = Color(0xFF0D202B)
private val Panel2 = Color(0xFF102A37)
private val Ink = Color(0xFFE9FBFF)
private val Muted = Color(0xFF8CA9B5)
private val Cyan = Color(0xFF19E6FF)
private val Teal = Color(0xFF00F0A8)
private val Blue = Color(0xFF4D7CFF)
private val Purple = Color(0xFFB14DFF)
private val Pink = Color(0xFFFF4FA3)
private val Gold = Color(0xFFFFC84D)
private val Red = Color(0xFFFF5364)
private val Line = Color(0xFF214452)

@Composable
fun Bot1PremiumScreen() {
    var selectedBot by remember { mutableIntStateOf(1) }
    var selectedStrategy by remember { mutableIntStateOf(1) }
    var running by remember { mutableStateOf(false) }
    var buy by remember { mutableStateOf(true) }
    var sell by remember { mutableStateOf(true) }
    var lot by remember { mutableFloatStateOf(0.01f) }
    var step by remember { mutableFloatStateOf(30f) }
    var maxOrders by remember { mutableFloatStateOf(10f) }
    var multiplier by remember { mutableFloatStateOf(2f) }
    var basketTp by remember { mutableFloatStateOf(50f) }
    var basketSl by remember { mutableFloatStateOf(-30f) }
    var trailing by remember { mutableFloatStateOf(0f) }
    var tab by remember { mutableIntStateOf(0) }

    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { NeonHeader(running) }
            item { BotVault(selectedBot) { selectedBot = it } }
            item { NeonCore(running, selectedBot) }
            item { MainControls(running, { running = !running }) }
            item { DirectionControls(buy, sell, { buy = !buy }, { sell = !sell }) }
            item { StrategyVault(selectedStrategy) { selectedStrategy = it } }
            item {
                if (tab == 0) {
                    SettingsPanel(
                        lot, step, maxOrders, multiplier, basketTp, basketSl, trailing,
                        { lot = it }, { step = it }, { maxOrders = it }, { multiplier = it },
                        { basketTp = it }, { basketSl = it }, { trailing = it }
                    )
                } else MarketPanel()
            }
            item { BottomTabs(tab) { tab = it } }
            item { SafetyPanel() }
        }
    }
}

@Composable
private fun NeonHeader(running: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("عمار", color = Cyan, fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("مختبر البوتات الذكي", color = Muted, fontSize = 11.sp)
        }
        NeonDot(if (running) Teal else Red, 14.dp)
        Spacer(Modifier.width(7.dp))
        Text(if (running) "يعمل" else "متوقف", color = if (running) Teal else Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun BotVault(selected: Int, select: (Int) -> Unit) {
    Column {
        SectionTitle("خزنة البوتات", "٤ خانات مستقلة للحفظ والتشغيل")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..4).forEach { bot ->
                BotCard(bot, bot == selected, select)
            }
        }
    }
}

@Composable
private fun BotCard(bot: Int, selected: Boolean, select: (Int) -> Unit) {
    val accent = when (bot) { 1 -> Cyan; 2 -> Purple; 3 -> Pink; else -> Gold }
    Column(
        Modifier
            .width(88.dp)
            .background(if (selected) Panel2 else Panel, RoundedCornerShape(18.dp))
            .border(1.dp, if (selected) accent else Line, RoundedCornerShape(18.dp))
            .clickable { select(bot) }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NeonDot(accent, 25.dp)
        Spacer(Modifier.height(5.dp))
        Text("بوت $bot", color = Ink, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text(if (bot == 1) "مجهز" else "جاهز", color = if (bot == 1) accent else Muted, fontSize = 9.sp)
    }
}

@Composable
private fun NeonCore(running: Boolean, bot: Int) {
    val pulse by rememberInfiniteTransition(label = "core").animateFloat(
        0.94f, 1.06f,
        infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "corePulse"
    )
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(218.dp)
                .graphicsLayer { scaleX = pulse; scaleY = pulse }
                .shadow(28.dp, CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF173E4B), Bg)), CircleShape)
                .border(2.dp, Brush.sweepGradient(listOf(Cyan, Purple, Pink, Gold, Cyan)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                NeonDot(if (running) Teal else Red, 12.dp)
                Spacer(Modifier.height(8.dp))
                Text("بوت $bot", color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("عمار", color = Ink, fontSize = 36.sp, fontWeight = FontWeight.Black)
                Text(if (running) "المحرك يعمل" else "المحرك جاهز", color = if (running) Teal else Muted, fontSize = 12.sp)
                Text("شبكة • مضاعفة • سلة", color = Gold, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun MainControls(running: Boolean, toggle: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NeonButton(if (running) "إيقاف" else "تشغيل", if (running) Red else Teal, Modifier.weight(1f), toggle)
        NeonButton("إعادة بناء", Blue, Modifier.weight(1f)) { }
        NeonButton("إغلاق الكل", Red, Modifier.weight(1f)) { }
    }
}

@Composable
private fun DirectionControls(buy: Boolean, sell: Boolean, buyToggle: () -> Unit, sellToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NeonButton(if (buy) "شراء ●" else "شراء ○", Teal, Modifier.weight(1f), buyToggle)
        NeonButton(if (sell) "بيع ●" else "بيع ○", Pink, Modifier.weight(1f), sellToggle)
    }
}

@Composable
private fun StrategyVault(selected: Int, select: (Int) -> Unit) {
    Column {
        SectionTitle("استراتيجيات بوت 1", "١٠ خانات حفظ مستقلة")
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            (1..10).chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    pair.forEach { strategy ->
                        StrategyCard(strategy, strategy == selected, select, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyCard(number: Int, selected: Boolean, select: (Int) -> Unit, modifier: Modifier) {
    val accent = listOf(Cyan, Teal, Blue, Purple, Pink, Gold)[(number - 1) % 6]
    Column(
        modifier
            .background(if (selected) Panel2 else Panel, RoundedCornerShape(15.dp))
            .border(1.dp, if (selected) accent else Line, RoundedCornerShape(15.dp))
            .clickable { select(number) }
            .padding(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NeonDot(accent, 10.dp)
            Spacer(Modifier.width(6.dp))
            Text("استراتيجية $number", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(if (selected) "محددة • قابلة للتعديل" else "خانة حفظ", color = if (selected) accent else Muted, fontSize = 8.sp)
    }
}

@Composable
private fun SettingsPanel(
    lot: Float, step: Float, maxOrders: Float, multiplier: Float, tp: Float, sl: Float, trailing: Float,
    setLot: (Float) -> Unit, setStep: (Float) -> Unit, setMax: (Float) -> Unit, setMultiplier: (Float) -> Unit,
    setTp: (Float) -> Unit, setSl: (Float) -> Unit, setTrailing: (Float) -> Unit
) {
    Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            SectionTitle("لوحة الإعدادات", "اسحب المؤشر بدل الكتابة")
            NeonSlider("اللوت", lot, 0.01f, 1f, "%.2f", setLot, Teal)
            NeonSlider("مسافة الشبكة", step, 1f, 300f, "%.0f", setStep, Cyan)
            NeonSlider("الحد الأقصى للصفقات", maxOrders, 1f, 100f, "%.0f", setMax, Blue)
            NeonSlider("مضاعف اللوت", multiplier, 1f, 5f, "%.2f", setMultiplier, Purple)
            NeonSlider("هدف السلة — دولار", tp, 0f, 500f, "%.2f", setTp, Gold)
            NeonSlider("خسارة السلة — دولار", sl, -500f, 0f, "%.2f", setSl, Red)
            NeonSlider("التتبع المتحرك", trailing, 0f, 300f, "%.0f", setTrailing, Pink)
        }
    }
}

@Composable
private fun NeonSlider(label: String, value: Float, min: Float, max: Float, format: String, set: (Float) -> Unit, accent: Color) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
            Text(String.format(format, value), color = accent, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        Slider(value = value, onValueChange = set, valueRange = min..max)
    }
}

@Composable
private fun MarketPanel() {
    Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp)) {
            SectionTitle("حالة السوق", "تتصل تلقائياً عند توفر MT5")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MarketMetric("السعر", "—", Cyan)
                MarketMetric("الاتجاه", "—", Teal)
                MarketMetric("الصفقات", "—", Gold)
            }
        }
    }
}

@Composable
private fun MarketMetric(a: String, b: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        NeonDot(color, 8.dp)
        Text(a, color = Muted, fontSize = 9.sp)
        Text(b, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BottomTabs(tab: Int, set: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NeonButton("الإعدادات", if (tab == 0) Cyan else Muted, Modifier.weight(1f)) { set(0) }
        NeonButton("حالة السوق", if (tab == 1) Teal else Muted, Modifier.weight(1f)) { set(1) }
    }
}

@Composable
private fun SafetyPanel() {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF171A25), RoundedCornerShape(18.dp))
            .border(1.dp, Gold.copy(alpha = .6f), RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Column {
            Text("حاجز الأمان", color = Gold, fontWeight = FontWeight.Black)
            Text("التنفيذ الحقيقي يبقى مقفولاً حتى تكتمل المصادقة والتصريح وقناة MT5.", color = Ink, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(bottom = 6.dp)) {
        Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(subtitle, color = Muted, fontSize = 9.sp)
    }
}

@Composable
private fun NeonButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(50.dp)
            .background(Brush.linearGradient(listOf(Panel2, Panel)), RoundedCornerShape(15.dp))
            .border(1.3.dp, color.copy(alpha = .8f), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun NeonDot(color: Color, size: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier
            .size(size)
            .shadow(10.dp, CircleShape)
            .background(color, CircleShape)
    )
}
