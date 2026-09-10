package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

private val UBg = Color(0xFF061019)
private val UPanel = Color(0xFF0B1C27)
private val UPanel2 = Color(0xFF102936)
private val ULine = Color(0xFF1D4555)
private val UCyan = Color(0xFF18E6FF)
private val UGreen = Color(0xFF00E7A0)
private val UGold = Color(0xFFFFC84A)
private val URed = Color(0xFFFF5264)
private val UText = Color(0xFFE9FBFF)
private val UMuted = Color(0xFF8AA7B4)

private enum class BotTab(val title: String, val icon: String) {
    BOT("البوت", "🤖"), MARKET("السوق", "📈"), STRATEGIES("الاستراتيجيات", "🧠"), BOTS("البوتات", "🗂️")
}

@Composable
fun Bot1UnifiedScreen(onBackHome: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { AmarBotVaultRepository(context) }
    var bots by remember { mutableStateOf(repository.load()) }
    var selectedBot by remember { mutableIntStateOf(1) }
    var tab by remember { mutableStateOf(BotTab.BOT) }
    var lastCommand by remember { mutableStateOf("لا يوجد أمر مرسل") }
    var strategySlot by remember { mutableIntStateOf(1) }

    fun persist(value: List<AmarSavedBot>) { bots = value; repository.save(value) }
    val current = bots.firstOrNull { it.botNumber == selectedBot } ?: bots.firstOrNull()
    val selectedStrategy = current?.strategies?.firstOrNull { it.number == strategySlot }

    Surface(color = UBg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().background(UPanel).padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onBackHome, colors = ButtonDefaults.buttonColors(containerColor = UGold, contentColor = Color.Black)) { Text("⌂") }
                Column(Modifier.weight(1f)) {
                    Text("AMAR BOT OS", color = UCyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("BOT 1 • ${current?.name ?: "غير محدد"} • unified control", color = UMuted, fontSize = 9.sp)
                }
                StatusPill("RUNTIME: UNKNOWN", UGold)
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BotTab.values().forEach { item ->
                    val active = item == tab
                    Button(
                        onClick = { tab = item },
                        colors = ButtonDefaults.buttonColors(containerColor = if (active) UCyan else UPanel2, contentColor = if (active) Color.Black else UText),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("${item.icon} ${item.title}", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                }
            }

            LazyColumn(contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                item { CommandBar(lastCommand) { command -> lastCommand = command } }
                when (tab) {
                    BotTab.BOT -> {
                        item { RuntimeHero() }
                        item { DetailGrid() }
                        item { BotConfiguration(current?.strategies?.firstOrNull()?.profile ?: AmarBot1RuntimeConfig()) }
                    }
                    BotTab.MARKET -> {
                        item { MarketState() }
                        item { MarketDetails() }
                        item { DetailGrid() }
                    }
                    BotTab.STRATEGIES -> {
                        item { StrategyPanel(current, strategySlot, { strategySlot = it }, persist, bots, selectedBot) }
                    }
                    BotTab.BOTS -> {
                        item { BotVaultPanel(bots, selectedBot, { selectedBot = it; tab = BotTab.BOT }, persist) }
                    }
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}

@Composable private fun CommandBar(last: String, send: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(UPanel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("لوحة الأوامر", color = UGold, fontWeight = FontWeight.Black, fontSize = 17.sp)
                Text(last, color = UMuted, fontSize = 9.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CommandButton("تشغيل", UGreen) { send("START • طلب تشغيل BOT 1") }
                CommandButton("إيقاف", UGold) { send("STOP • طلب إيقاف BOT 1") }
                CommandButton("إغلاق الكل", URed) { send("CLOSE_ALL • طلب إغلاق جميع صفقات BOT 1") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CommandButton("إعادة بناء", UCyan) { send("REBUILD • طلب Global Rebuild BOT 1") }
                CommandButton("Rebuild Grid", UCyan) { send("REBUILD_GRID • طلب إعادة بناء الشبكة") }
                CommandButton("Rebuild Track", UCyan) { send("REBUILD_TRACKING • طلب إعادة بناء التتبع") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CommandButton("BUY", UGreen) { send("SET_BUY_ENABLED • ON") }
                CommandButton("SELL", UGreen) { send("SET_SELL_ENABLED • ON") }
                CommandButton("طوارئ", URed) { send("EMERGENCY_LOCK • طلب قفل الطوارئ") }
            }
            Text("الأوامر تمر عبر طبقة الأمان والقناة الآمنة؛ حالة التشغيل الفعلية لا تتغير من زر الواجهة وحده.", color = UMuted, fontSize = 8.sp)
        }
    }
}

@Composable private fun RuntimeHero() {
    Card(colors = CardDefaults.cardColors(UPanel2), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("BOT 1 • Grid_Martingale_Basket_v2", color = UCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("الحالة: UNKNOWN / بانتظار runtime الحقيقي من MT5", color = UGold, fontSize = 11.sp)
            Text("Identity: BOT_1   •   Magic: 20260908   •   Version: 2.00   •   Strategy: STRATEGY_01", color = UMuted, fontSize = 9.sp)
        }
    }
}

@Composable private fun DetailGrid() {
    val details = listOf(
        "الحساب" to "Live/Read-only boundary",
        "الرصيد" to "—",
        "Equity" to "—",
        "الربح العائم" to "—",
        "الصفقات المفتوحة" to "0",
        "الأوامر المعلقة" to "0",
        "سعر متوسط السلة" to "—",
        "التعرض" to "—",
        "Basket TP" to "$50.00",
        "Basket SL" to "-$30.00",
        "Trailing" to "$0.00",
        "Martingale" to "2.00x",
        "Grid Step" to "30",
        "Max Orders" to "10",
        "Direction" to "BUY + SELL",
        "Reconciliation" to "UNKNOWN"
    )
    Card(colors = CardDefaults.cardColors(UPanel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("تفاصيل التشغيل — 16 نقطة مراقبة", color = UGold, fontWeight = FontWeight.Black)
            details.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pair.forEach { (label, value) ->
                        Metric(label, value, Modifier.weight(1f))
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable private fun MarketState() {
    Card(colors = CardDefaults.cardColors(UPanel2), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("XAUUSD", color = UCyan, fontSize = 24.sp, fontWeight = FontWeight.Black); Text("الذهب • السوق الحقيقي", color = UMuted, fontSize = 10.sp) }
                StatusPill("WAITING DATA", UGold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Metric("Bid", "—", Modifier.weight(1f)); Metric("Ask", "—", Modifier.weight(1f)); Metric("Spread", "—", Modifier.weight(1f))
            }
        }
    }
}

@Composable private fun MarketDetails() {
    val items = listOf("الاتجاه" to "—", "قوة الحركة" to "—", "الجلسة" to "—", "التذبذب" to "—", "آخر شمعة" to "—", "الفريم" to "M1 / M5", "حالة الاتصال" to "READ-ONLY", "آخر تحديث" to "—")
    Card(colors = CardDefaults.cardColors(UPanel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("حالة السوق", color = UGold, fontWeight = FontWeight.Black)
            items.forEach { (a,b) -> KeyValue(a,b) }
        }
    }
}

@Composable private fun BotConfiguration(config: AmarBot1RuntimeConfig) {
    Card(colors = CardDefaults.cardColors(UPanel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("إعدادات BOT 1 الحالية", color = UGold, fontWeight = FontWeight.Black)
            KeyValue("Lot", format(config.lot)); KeyValue("Grid", format(config.gridStep)); KeyValue("Max Orders", config.maxOrders.toString()); KeyValue("Multiplier", format(config.multiplier)); KeyValue("Basket TP", money(config.basketTp)); KeyValue("Basket SL", money(config.basketSl)); KeyValue("Trailing", money(config.trailing))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { FilterChip(config.buyEnabled, {}, label = { Text("BUY") }); FilterChip(config.sellEnabled, {}, label = { Text("SELL") }) }
        }
    }
}

@Composable private fun StrategyPanel(current: AmarSavedBot?, selected: Int, select: (Int) -> Unit, persist: (List<AmarSavedBot>) -> Unit, bots: List<AmarSavedBot>, botNumber: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("استراتيجيات BOT 1 — 10 خانات", color = UCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        (1..10).forEach { n ->
            val saved = current?.strategies?.firstOrNull { it.number == n }
            Row(Modifier.fillMaxWidth().background(if (n == selected) UPanel2 else UPanel, RoundedCornerShape(15.dp)).border(1.dp, if (n == selected) UCyan else ULine, RoundedCornerShape(15.dp)).clickable { select(n) }.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (saved == null) "○" else "✓", color = if (saved == null) UMuted else UGreen, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(8.dp)); Column(Modifier.weight(1f)) { Text(saved?.name ?: "استراتيجية رقم $n", color = UText, fontWeight = FontWeight.Bold); Text(if (saved == null) "غير محفوظ" else "محفوظة • ${saved.riskProfile}", color = if (saved == null) UMuted else UGreen, fontSize = 9.sp) }
                Text("$n", color = UGold, fontWeight = FontWeight.Black)
            }
        }
        if (current != null) {
            val saved = current.strategies.firstOrNull { it.number == selected }
            if (saved != null) {
                Text("الاستراتيجية المحددة: ${saved.name}", color = UGold, fontWeight = FontWeight.Bold)
                KeyValue("Risk", saved.riskProfile); KeyValue("Rebuild", saved.rebuildRule); KeyValue("Entry", saved.entryRule); KeyValue("Metadata", saved.metadata)
            }
        }
    }
}

@Composable private fun BotVaultPanel(bots: List<AmarSavedBot>, selected: Int, select: (Int) -> Unit, persist: (List<AmarSavedBot>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("خزنة البوتات", color = UCyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text("BOT 1 هو البوت الحقيقي المعتمد حالياً. بقية الخانات قابلة للحفظ ولا تُعتبر محركات تداول حقيقية حتى اعتمادها.", color = UMuted, fontSize = 9.sp)
        bots.forEach { bot ->
            Row(Modifier.fillMaxWidth().background(if (bot.botNumber == selected) UPanel2 else UPanel, RoundedCornerShape(16.dp)).border(1.dp, if (bot.botNumber == selected) UCyan else ULine, RoundedCornerShape(16.dp)).clickable { select(bot.botNumber) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(bot.name, color = UText, fontWeight = FontWeight.Black); Text("${bot.strategies.size}/10 استراتيجيات محفوظة", color = UMuted, fontSize = 9.sp) }
                StatusPill(if (bot.botNumber == 1) "REAL BOT" else "VAULT", if (bot.botNumber == 1) UGreen else UGold)
            }
        }
    }
}

@Composable private fun Metric(label: String, value: String, modifier: Modifier) { Column(modifier.background(UPanel2, RoundedCornerShape(10.dp)).padding(8.dp)) { Text(label, color = UMuted, fontSize = 8.sp); Text(value, color = UText, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun KeyValue(label: String, value: String) { Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = UMuted, fontSize = 9.sp); Text(value, color = UText, fontSize = 9.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun StatusPill(text: String, color: Color) { Text(text, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.background(color.copy(alpha = .12f), RoundedCornerShape(20.dp)).padding(horizontal = 9.dp, vertical = 6.dp)) }
@Composable private fun CommandButton(text: String, color: Color, onClick: () -> Unit) { Button(onClick = onClick, modifier = Modifier.weight(1f).height(42.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if (color == UGold) Color.Black else Color.White), contentPadding = PaddingValues(horizontal = 4.dp)) { Text(text, fontSize = 8.sp, fontWeight = FontWeight.Black) } }
private fun format(value: Double) = String.format(Locale.US, "%.2f", value)
private fun money(value: Double) = String.format(Locale.US, "%.2f $", value)
