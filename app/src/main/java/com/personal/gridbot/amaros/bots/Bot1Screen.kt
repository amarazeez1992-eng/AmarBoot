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
import androidx.compose.material3.OutlinedButton
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

/** واجهة BOT 1: تصميم فقط. لا توجد أوامر MT5 فعلية في هذه المرحلة. */
@Composable
fun BotLabScreen() {
    var selectedBot by remember { mutableStateOf(1) }
    Surface(color = Background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            BotLabHeader()
            Spacer(Modifier.height(9.dp))
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                item { BotSlots(selectedBot) { selectedBot = it } }
                item { if (selectedBot == 1) Bot1ControlPanel() else EmptyBotSlotPanel(selectedBot) }
            }
        }
    }
}

@Composable
private fun BotLabHeader() {
    val pulse by rememberInfiniteTransition(label = "headerPulse").animateFloat(
        0.72f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse"
    )
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(3.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(Color(0xFFEAF2FF), CircleShape), Alignment.Center) {
                Text("🤖", fontSize = 25.sp, modifier = Modifier.alpha(pulse))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text("غرفة البوتات", 21.sp, FontWeight.Bold, Ink)
                Text("10 خانات مستقلة قابلة للتوسع", color = Muted, fontSize = 12.sp)
            }
            LiveBadge()
        }
    }
}

@Composable
private fun LiveBadge() {
    val pulse by rememberInfiniteTransition(label = "live").animateFloat(0.45f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "livePulse")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).alpha(pulse).background(Green, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text("جاهز للربط", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BotSlots(selectedBot: Int, onSelect: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("اختيار البوت", fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.width(8.dp)); Text("•", color = Orange); Text("10", color = Purple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            for (rowStart in 1..10 step 5) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    for (bot in rowStart..minOf(rowStart + 4, 10)) BotSlot(bot, selectedBot == bot, bot == 1, { onSelect(bot) }, Modifier.weight(1f))
                }
                if (rowStart < 10) Spacer(Modifier.height(7.dp))
            }
        }
    }
}

@Composable
private fun BotSlot(number: Int, selected: Boolean, configured: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val accent = if (selected) Blue else Border
    val bg = if (selected) Color(0xFFEAF2FF) else Color(0xFFFBFCFE)
    Column(modifier.height(66.dp).background(bg, RoundedCornerShape(15.dp)).border(1.5.dp, accent, RoundedCornerShape(15.dp)).clickable(onClick = onClick).padding(7.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("${if (number < 10) "0$number" else number}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (selected) Blue else Ink)
        Text(if (configured) "بوت 1" else "جاهز", fontSize = 9.sp, color = if (configured) Green else Muted)
    }
}

@Composable
private fun Bot1ControlPanel() {
    var enabled by remember { mutableStateOf(true) }
    var buyEnabled by remember { mutableStateOf(true) }
    var sellEnabled by remember { mutableStateOf(true) }
    var rebuildRequested by remember { mutableStateOf(false) }
    var closeRequested by remember { mutableStateOf(false) }

    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(3.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("بوت 1", 23.sp, FontWeight.Bold, Ink)
                    Text("شبكة + مضاعفة + سلة", color = Purple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Grid_Martingale_Basket_v2 • الإصدار 2.00", color = Muted, fontSize = 10.sp)
                }
                StateIndicator(enabled)
            }
            Spacer(Modifier.height(12.dp)); DividerLine(); Spacer(Modifier.height(11.dp))
            Text("إعدادات الاستراتيجية", fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(7.dp))
            BotSettingGrid()
            Spacer(Modifier.height(11.dp)); DividerLine(); Spacer(Modifier.height(11.dp))
            Text("مفاتيح الاستراتيجية", fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(7.dp))
            ToggleAction("الشراء", buyEnabled, Green) { buyEnabled = !buyEnabled }
            Spacer(Modifier.height(6.dp))
            ToggleAction("البيع", sellEnabled, Red) { sellEnabled = !sellEnabled }
            Spacer(Modifier.height(11.dp))
            Text("تحكم البوت", fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ActionButton(if (enabled) "● يعمل" else "● متوقف", if (enabled) Green else Red, Modifier.weight(1f)) { enabled = !enabled }
                ActionButton("🔄 إعادة بناء", Blue, Modifier.weight(1f)) { rebuildRequested = !rebuildRequested }
            }
            Spacer(Modifier.height(7.dp))
            ActionButton("🔴 إغلاق صفقات البوت", Red, Modifier.fillMaxWidth()) { closeRequested = !closeRequested }
            if (rebuildRequested || closeRequested) {
                Spacer(Modifier.height(7.dp))
                Text(if (closeRequested) "تم اختيار أمر الإغلاق للمعاينة فقط." else "تم اختيار إعادة البناء للمعاينة فقط.", color = Orange, fontSize = 10.sp)
            }
            Spacer(Modifier.height(10.dp))
            InfoStrip()
        }
    }
}

@Composable
private fun StateIndicator(enabled: Boolean) {
    val color = if (enabled) Green else Red
    Column(horizontalAlignment = Alignment.End) {
        Text("حالة البوت", color = Muted, fontSize = 9.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(color, CircleShape)); Spacer(Modifier.width(5.dp)); Text(if (enabled) "مفعّل" else "متوقف", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ToggleAction(label: String, enabled: Boolean, accent: Color, onClick: () -> Unit) {
    val bg = if (enabled) Color(0xFFF1FBF6) else Color(0xFFFFF1F2)
    val state = if (enabled) "مفعّل" else "مغلق"
    Row(Modifier.fillMaxWidth().background(bg, RoundedCornerShape(13.dp)).border(1.dp, if (enabled) accent else Border, RoundedCornerShape(13.dp)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(if (enabled) accent else Red, CircleShape)); Spacer(Modifier.width(8.dp)); Text(label, Modifier.weight(1f), color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(state, color = if (enabled) accent else Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(43.dp), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) { Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun DividerLine() { Spacer(Modifier.fillMaxWidth().height(1.dp).background(Border)) }

@Composable
private fun InfoStrip() {
    Row(Modifier.fillMaxWidth().background(Color(0xFFFFF8EA), RoundedCornerShape(13.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("●", color = Yellow, fontSize = 14.sp); Spacer(Modifier.width(7.dp)); Text("الواجهة تجريبية حاليًا؛ الربط مع MT5 يأتي بعد اعتماد التصميم.", color = Color(0xFF805B12), fontSize = 10.sp)
    }
}

@Composable
private fun BotSettingGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SettingRow("اللوت الابتدائي", "0.01", "حجم العقد", Green)
        SettingRow("مسافة الشبكة", "30", "نقطة", Blue)
        SettingRow("الحد الأقصى", "10", "صفقات", Orange)
        SettingRow("مضاعف اللوت", "2.00", "معامل", Purple)
        SettingRow("هدف السلة", "50.00", "دولار", Green)
        SettingRow("خسارة السلة", "-30.00", "دولار", Red)
        SettingRow("الستوب المتحرك", "0", "نقطة", Yellow)
    }
}

@Composable
private fun SettingRow(name: String, value: String, unit: String, accent: Color) {
    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(5.dp).background(accent, CircleShape)); Spacer(Modifier.width(8.dp)); Text(name, Modifier.weight(1f), color = Color(0xFF334155), fontSize = 12.sp); Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp); Spacer(Modifier.width(5.dp)); Text(unit, color = Muted, fontSize = 9.sp)
    }
}

@Composable
private fun EmptyBotSlotPanel(number: Int) {
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(22.dp), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("بوت $number", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(6.dp)); Text("هذه الخانة جاهزة لإضافة بوت مستقل لاحقًا.", color = Muted, fontSize = 12.sp)
        }
    }
}