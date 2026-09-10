package com.personal.gridbot.amaros.bots

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFFF6F8FC)
private val Ink = Color(0xFF172033)
private val Muted = Color(0xFF718096)
private val Blue = Color(0xFF246BFE)
private val Green = Color(0xFF12B76A)
private val Red = Color(0xFFE5484D)
private val Yellow = Color(0xFFF4B400)
private val Orange = Color(0xFFFF8A34)
private val Purple = Color(0xFF8B5CF6)
private val Border = Color(0xFFE1E7F0)

private enum class Bot1Page { BOT, MARKET }

@Composable
fun BotLabScreen() {
    var selectedBot by remember { mutableStateOf(1) }
    Surface(color = Background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            BotLabHeader()
            Spacer(modifier = Modifier.height(9.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                item { BotSlots(selectedBot) { selectedBot = it } }
                item {
                    if (selectedBot == 1) Bot1Workspace() else EmptyBotSlotPanel(selectedBot)
                }
            }
        }
    }
}

@Composable
private fun BotLabHeader() {
    val pulse by rememberInfiniteTransition(label = "headerPulse").animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp).background(Color(0xFFEAF2FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤖", fontSize = 25.sp, modifier = Modifier.alpha(pulse))
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "غرفة البوتات", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(text = "10 خانات مستقلة قابلة للتوسع", color = Muted, fontSize = 12.sp)
            }
            LiveBadge()
        }
    }
}

@Composable
private fun LiveBadge() {
    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "livePulse"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).alpha(pulse).background(Green, CircleShape))
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = "جاهز للربط", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BotSlots(selectedBot: Int, onSelect: (Int) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "اختيار البوت", color = Ink, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "•", color = Orange)
                Text(text = "10", color = Purple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            for (rowStart in 1..10 step 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    for (bot in rowStart..minOf(rowStart + 4, 10)) {
                        BotSlot(
                            number = bot,
                            selected = selectedBot == bot,
                            configured = bot == 1,
                            onClick = { onSelect(bot) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (rowStart < 10) Spacer(modifier = Modifier.height(7.dp))
            }
        }
    }
}

@Composable
private fun BotSlot(
    number: Int,
    selected: Boolean,
    configured: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val accent = if (selected) Blue else Border
    val bg = if (selected) Color(0xFFEAF2FF) else Color(0xFFFBFCFE)
    Column(
        modifier = modifier
            .height(66.dp)
            .background(bg, RoundedCornerShape(15.dp))
            .border(1.5.dp, accent, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(7.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (number < 10) "0$number" else "$number",
            color = if (selected) Blue else Ink,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (configured) "بوت 1" else "جاهز",
            color = if (configured) Green else Muted,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun Bot1Workspace() {
    var page by remember { mutableStateOf(Bot1Page.BOT) }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Bot1TopTabs(page) { page = it }
        if (page == Bot1Page.BOT) Bot1ControlPanel() else Bot1MarketStatus()
    }
}

@Composable
private fun Bot1TopTabs(page: Bot1Page, onPageChange: (Bot1Page) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            BotTab(
                title = "🤖 واجهة البوت",
                selected = page == Bot1Page.BOT,
                color = Blue,
                modifier = Modifier.weight(1f),
                onClick = { onPageChange(Bot1Page.BOT) }
            )
            BotTab(
                title = "📊 حالة السوق",
                selected = page == Bot1Page.MARKET,
                color = Green,
                modifier = Modifier.weight(1f),
                onClick = { onPageChange(Bot1Page.MARKET) }
            )
        }
    }
}

@Composable
private fun BotTab(
    title: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val background = if (selected) color.copy(alpha = 0.12f) else Color(0xFFF8FAFC)
    Row(
        modifier = modifier
            .height(48.dp)
            .background(background, RoundedCornerShape(15.dp))
            .border(1.5.dp, if (selected) color else Border, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (selected) {
            Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = title, color = if (selected) color else Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Bot1ControlPanel() {
    var enabled by remember { mutableStateOf(false) }
    var buyEnabled by remember { mutableStateOf(true) }
    var sellEnabled by remember { mutableStateOf(true) }
    var rebuildRequested by remember { mutableStateOf(false) }
    var closeRequested by remember { mutableStateOf(false) }
    var lotStart by remember { mutableStateOf("0.01") }
    var gridStep by remember { mutableStateOf("30") }
    var maxOrders by remember { mutableStateOf("10") }
    var martingale by remember { mutableStateOf("2.00") }
    var basketTp by remember { mutableStateOf("50.00") }
    var basketSl by remember { mutableStateOf("-30.00") }
    var trailing by remember { mutableStateOf("0") }
    var saved by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "بوت 1", color = Ink, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text(text = "شبكة + مضاعفة + سلة", color = Purple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "الإصدار 2.00", color = Muted, fontSize = 10.sp)
                }
                StateIndicator(enabled)
            }
            Spacer(modifier = Modifier.height(12.dp))
            DividerLine()
            Spacer(modifier = Modifier.height(11.dp))
            SectionTitle("إعدادات الاستراتيجية", Purple)
            Spacer(modifier = Modifier.height(7.dp))
            EditableSetting("اللوت الابتدائي", lotStart, "حجم العقد", Green) { lotStart = it; saved = false }
            EditableSetting("مسافة الشبكة", gridStep, "نقطة", Blue) { gridStep = it; saved = false }
            EditableSetting("الحد الأقصى", maxOrders, "صفقات", Orange) { maxOrders = it; saved = false }
            EditableSetting("مضاعف اللوت", martingale, "معامل", Purple) { martingale = it; saved = false }
            EditableSetting("هدف السلة", basketTp, "دولار", Green) { basketTp = it; saved = false }
            EditableSetting("خسارة السلة", basketSl, "دولار", Red) { basketSl = it; saved = false }
            EditableSetting("الستوب المتحرك", trailing, "نقطة", Yellow) { trailing = it; saved = false }
            Spacer(modifier = Modifier.height(11.dp))
            DividerLine()
            Spacer(modifier = Modifier.height(11.dp))
            SectionTitle("مفاتيح الاستراتيجية", Blue)
            Spacer(modifier = Modifier.height(7.dp))
            ToggleAction("الشراء", buyEnabled, Green) { buyEnabled = !buyEnabled }
            Spacer(modifier = Modifier.height(6.dp))
            ToggleAction("البيع", sellEnabled, Red) { sellEnabled = !sellEnabled }
            Spacer(modifier = Modifier.height(11.dp))
            SectionTitle("تحكم البوت", Orange)
            Spacer(modifier = Modifier.height(7.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                ActionButton(
                    text = if (enabled) "● يعمل" else "● متوقف",
                    color = if (enabled) Green else Red,
                    modifier = Modifier.weight(1f),
                    onClick = { enabled = !enabled }
                )
                ActionButton(
                    text = "🔄 إعادة بناء",
                    color = Blue,
                    modifier = Modifier.weight(1f),
                    onClick = { rebuildRequested = !rebuildRequested }
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            ActionButton(
                text = "🔴 إغلاق صفقات البوت",
                color = Red,
                modifier = Modifier.fillMaxWidth(),
                onClick = { closeRequested = !closeRequested }
            )
            Spacer(modifier = Modifier.height(9.dp))
            Button(
                onClick = { saved = true },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text(
                    text = if (saved) "✓ تم حفظ الإعدادات يدويًا" else "حفظ الإعدادات يدويًا",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (rebuildRequested || closeRequested) {
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = if (closeRequested) "تم اختيار أمر الإغلاق للمعاينة فقط." else "تم اختيار إعادة البناء للمعاينة فقط.",
                    color = Orange,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            InfoStrip()
        }
    }
}

@Composable
private fun SectionTitle(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StateIndicator(enabled: Boolean) {
    val color = if (enabled) Green else Red
    Column(horizontalAlignment = Alignment.End) {
        Text(text = "حالة البوت", color = Muted, fontSize = 9.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = if (enabled) "مفعّل" else "متوقف", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ToggleAction(label: String, enabled: Boolean, accent: Color, onClick: () -> Unit) {
    val bg = if (enabled) Color(0xFFF1FBF6) else Color(0xFFFFF1F2)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(13.dp))
            .border(1.dp, if (enabled) accent else Red, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(if (enabled) accent else Red, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, modifier = Modifier.weight(1f), color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(text = if (enabled) "مفعّل" else "مغلق", color = if (enabled) accent else Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(43.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EditableSetting(name: String, value: String, unit: String, accent: Color, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(5.dp).background(accent, CircleShape))
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = name,
            modifier = Modifier.width(108.dp),
            color = Color(0xFF334155),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).height(52.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            supportingText = {
                Text(text = unit, color = Muted, fontSize = 8.sp)
            }
        )
    }
}

@Composable
private fun DividerLine() {
    Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Border))
}

@Composable
private fun InfoStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF8EA), RoundedCornerShape(13.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "●", color = Yellow, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = "تحكم يدوي بالكامل. الربط مع MT5 سيأتي عبر طبقة آمنة منفصلة.",
            color = Color(0xFF805B12),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun EmptyBotSlotPanel(number: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "بوت $number", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "هذه الخانة جاهزة لإضافة بوت مستقل لاحقًا.", color = Muted, fontSize = 12.sp)
        }
    }
}
