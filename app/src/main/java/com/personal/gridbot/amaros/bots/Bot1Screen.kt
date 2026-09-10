package com.personal.gridbot.amaros.bots

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BotBlue = Color(0xFF1769FF)
private val BotGreen = Color(0xFF16A36A)
private val BotRed = Color(0xFFE5484D)
private val BotGold = Color(0xFFE3A51A)
private val SoftBackground = Color(0xFFF7F9FC)
private val SoftBorder = Color(0xFFE2E8F0)

/** واجهة BOT 1 فقط. لا تنفذ تداولًا؛ الربط يأتي في مرحلة لاحقة. */
@Composable
fun BotLabScreen() {
    var selectedBot by remember { mutableStateOf(1) }

    Surface(color = SoftBackground, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            BotLabHeader()
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    BotSlots(selectedBot = selectedBot, onSelect = { selectedBot = it })
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    if (selectedBot == 1) Bot1ControlPanel()
                    else EmptyBotSlotPanel(selectedBot)
                }
            }
        }
    }
}

@Composable
private fun BotLabHeader() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(50.dp).height(50.dp)
                    .background(BotBlue, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🤖", fontSize = 25.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("غرفة البوتات", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF172033))
                Text("10 خانات مستقلة للتحكم بالبوتات", color = Color(0xFF687386), fontSize = 13.sp)
            }
            StatusChip("MT5", "غير مربوط", BotGold)
        }
    }
}

@Composable
private fun BotSlots(selectedBot: Int, onSelect: (Int) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("البوتات", fontWeight = FontWeight.Bold, color = Color(0xFF172033))
            Spacer(Modifier.height(8.dp))
            for (rowStart in 1..10 step 3) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (bot in rowStart..minOf(rowStart + 2, 10)) {
                        BotSlot(
                            number = bot,
                            selected = selectedBot == bot,
                            configured = bot == 1,
                            onClick = { onSelect(bot) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - (minOf(rowStart + 2, 10) - rowStart + 1)) { Spacer(Modifier.weight(1f)) }
                }
                if (rowStart < 10) Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BotSlot(number: Int, selected: Boolean, configured: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor = if (selected) BotBlue else SoftBorder
    val background = if (selected) Color(0xFFEAF2FF) else Color(0xFFFBFCFE)
    Column(
        modifier = modifier.height(70.dp).background(background, RoundedCornerShape(14.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("${if (number < 10) "0$number" else number}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (selected) BotBlue else Color(0xFF273246))
        Text(if (configured) "بوت 1" else "فارغ", fontSize = 10.sp, color = if (configured) BotGreen else Color(0xFF98A2B3))
    }
}

@Composable
private fun Bot1ControlPanel() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("بوت 1", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Color(0xFF172033))
                        Text("شبكة + مضاعفة + سلة", color = BotBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Grid_Martingale_Basket_v2 • الإصدار 2.00", color = Color(0xFF7A8495), fontSize = 11.sp)
                    }
                    StatusChip("الحالة", "جاهز للربط", BotGreen)
                }
                Spacer(Modifier.height(14.dp))
                Divider(color = SoftBorder)
                Spacer(Modifier.height(12.dp))
                Text("الإعدادات الحالية", fontWeight = FontWeight.Bold, color = Color(0xFF172033))
                Spacer(Modifier.height(8.dp))
                BotSettingGrid()
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("تحكم البوت", fontWeight = FontWeight.Bold, color = Color(0xFF172033), fontSize = 17.sp)
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = BotGreen)) { Text("تفعيل") }
                    OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) { Text("إيقاف") }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) { Text("إعادة بناء") }
                    Button(onClick = { }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = BotRed)) { Text("إغلاق صفقات البوت") }
                }
                Spacer(Modifier.height(8.dp))
                Text("الأزرار للعرض فقط الآن؛ لم يتم توصيلها بـ MT5 بعد.", color = Color(0xFF7A8495), fontSize = 11.sp)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
            shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("هوية البوت", fontWeight = FontWeight.Bold, color = Color(0xFF174A9B))
                Spacer(Modifier.height(5.dp))
                Text("المعرّف: BOT_01   •   السحر: 20260908", color = Color(0xFF526174), fontSize = 12.sp)
                Text("هذه الهوية ستُستخدم لاحقًا لعزل صفقات هذا البوت عن بقية البوتات.", color = Color(0xFF526174), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BotSettingGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        SettingRow("اللوت الابتدائي", "0.01", "حجم العقد")
        SettingRow("مسافة الشبكة", "30", "نقطة")
        SettingRow("الحد الأقصى", "10", "صفقات")
        SettingRow("مضاعف اللوت", "2.00", "معامل")
        SettingRow("هدف السلة", "50.00", "دولار")
        SettingRow("خسارة السلة", "-30.00", "دولار")
        SettingRow("الستوب المتحرك", "0", "نقطة")
        SettingRow("الشراء", "مفعّل", "")
        SettingRow("البيع", "مفعّل", "")
    }
}

@Composable
private fun SettingRow(name: String, value: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, modifier = Modifier.weight(1f), color = Color(0xFF334155), fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.Bold, color = BotBlue, fontSize = 14.sp)
        if (unit.isNotEmpty()) { Spacer(Modifier.width(6.dp)); Text(unit, color = Color(0xFF8A94A5), fontSize = 10.sp) }
    }
}

@Composable
private fun EmptyBotSlotPanel(number: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("بوت $number", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF172033))
            Spacer(Modifier.height(6.dp))
            Text("هذه الخانة جاهزة لإضافة بوت مستقل لاحقًا.", color = Color(0xFF7A8495))
        }
    }
}

@Composable
private fun StatusChip(label: String, status: String, color: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, fontSize = 9.sp, color = Color(0xFF8A94A5))
        Text(status, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
    }
}
