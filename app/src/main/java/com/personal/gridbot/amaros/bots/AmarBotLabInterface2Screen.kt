package com.personal.gridbot.amaros.bots

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.runtime.AmarBotCommandEngine
import com.personal.gridbot.amaros.runtime.AmarBotOperationalEngine
import kotlinx.coroutines.launch

private val I2Bg = Color(0xFF050C14)
private val I2Panel = Color(0xFF0A1722)
private val I2Panel2 = Color(0xFF0E2230)
private val I2Line = Color(0xFF1A3E4D)
private val I2Text = Color(0xFFE9FBFF)
private val I2Muted = Color(0xFF7896A5)
private val I2Cyan = Color(0xFF1DE5FF)
private val I2Green = Color(0xFF00E6A0)
private val I2Gold = Color(0xFFFFC84A)
private val I2Red = Color(0xFFFF4F67)
private val I2Purple = Color(0xFFB14DFF)
private val I2Pink = Color(0xFFFF4FA3)

@Composable
fun AmarBotLabInterface2Screen(onBackHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember(context) { AmarBotVaultRepository(context) }
    val commandEngine = remember(context) { AmarBotCommandEngine(context) }
    val operationalEngine = remember(context) { AmarBotOperationalEngine(context) }
    val scope = rememberCoroutineScope()
    var bots by remember { mutableStateOf(repo.load()) }
    var selectedBot by remember { mutableIntStateOf(AmarBotLabSelectionContext.selectedBot.coerceIn(1, 10)) }
    var selectedStrategy by remember { mutableIntStateOf(1) }
    var notice by remember { mutableStateOf("") }
    val currentBot = bots.firstOrNull { it.botNumber == selectedBot } ?: bots.firstOrNull()
    val currentStrategy = currentBot?.strategies?.firstOrNull { it.number == selectedStrategy }
    fun refresh() { bots = repo.load() }
    LaunchedEffect(Unit) { operationalEngine.ensureBotCatalog() }

    Surface(color = I2Bg, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onBackHome, colors = ButtonDefaults.buttonColors(containerColor = I2Gold, contentColor = Color.Black), contentPadding = PaddingValues(horizontal = 13.dp, vertical = 5.dp)) { Text("⌂", fontWeight = FontWeight.Black) }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text("AMAR BOT LAB", color = I2Cyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("الواجهة 2 • نظام السحب والإفلات", color = I2Muted, fontSize = 9.sp)
                    }
                    Text("LIVE UI", color = I2Green, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
            item { BotBoard2(selectedBot) { selectedBot = it; AmarBotLabSelectionContext.selectedBot = it } }
            item { StrategySelector2(currentBot, selectedStrategy) { selectedStrategy = it } }
            item {
                Interface2Editor(
                    strategy = currentStrategy,
                    number = selectedStrategy,
                    onSave = { strategy -> repo.saveStrategy(selectedBot, strategy); refresh(); notice = "✓ تم حفظ القيم الجديدة للاستراتيجية ${strategy.number} — من دون تغيير محرك البوت" },
                    onDelete = { repo.deleteStrategy(selectedBot, selectedStrategy); refresh(); notice = "تم حذف الاستراتيجية $selectedStrategy" }
                )
            }
            item {
                CommandPanel2 { command ->
                    scope.launch {
                        runCatching { commandEngine.queue(selectedBot, command) }
                            .onSuccess { id -> notice = "✓ الطلب #$id محفوظ كـ PENDING_MT5 — لا يوجد ادعاء بتنفيذ MT5" }
                            .onFailure { notice = "✕ تعذر حفظ الطلب: ${it.message ?: "خطأ غير معروف"}" }
                    }
                }
            }
            if (notice.isNotBlank()) item { Notice2(notice) }
        }
    }
}

@Composable
private fun BotBoard2(selected: Int, onSelect: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(I2Panel), shape = RoundedCornerShape(19.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("خزنة البوتات", color = I2Text, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text("اسحب بطاقة البوت نفسها لإعادة ترتيبها • هذا لا يغير الاستراتيجية أو محرك التنفيذ", color = I2Muted, fontSize = 8.sp)
            AmarBotDragDropBoard((1..10).toList(), selected, onSelect, onReorder = { })
        }
    }
}

@Composable
private fun StrategySelector2(bot: AmarSavedBot?, selected: Int, onSelect: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(I2Panel), shape = RoundedCornerShape(19.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("استراتيجيات ${bot?.name ?: "البوت"}", color = I2Text, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..10).forEach { n ->
                    val selectedColor by animateColorAsState(if (n == selected) I2Cyan else I2Panel2, tween(160), label = "strategy-$n")
                    Box(Modifier.weight(1f).height(36.dp).background(selectedColor, RoundedCornerShape(9.dp)).border(1.dp, if (n == selected) I2Cyan else I2Line, RoundedCornerShape(9.dp)).clickable { onSelect(n) }, contentAlignment = Alignment.Center) {
                        Text(n.toString().padStart(2, '0'), color = if (n == selected) Color.Black else I2Text, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun Interface2Editor(strategy: AmarSavedStrategy?, number: Int, onSave: (AmarSavedStrategy) -> Unit, onDelete: () -> Unit) {
    var name by remember(strategy?.number, strategy?.name) { mutableStateOf(strategy?.name ?: "Strategy ${number.toString().padStart(2, '0')}") }
    var lot by remember(strategy?.number, strategy?.profile?.lot) { mutableDoubleStateOf(strategy?.profile?.lot ?: 0.01) }
    var grid by remember(strategy?.number, strategy?.profile?.gridStep) { mutableDoubleStateOf(strategy?.profile?.gridStep ?: 30.0) }
    var maxOrders by remember(strategy?.number, strategy?.profile?.maxOrders) { mutableDoubleStateOf((strategy?.profile?.maxOrders ?: 10).toDouble()) }
    var multiplier by remember(strategy?.number, strategy?.profile?.multiplier) { mutableDoubleStateOf(strategy?.profile?.multiplier ?: 2.0) }
    var basketTp by remember(strategy?.number, strategy?.profile?.basketTp) { mutableDoubleStateOf(strategy?.profile?.basketTp ?: 50.0) }
    var basketSl by remember(strategy?.number, strategy?.profile?.basketSl) { mutableDoubleStateOf(strategy?.profile?.basketSl ?: -30.0) }
    var trailing by remember(strategy?.number, strategy?.profile?.trailing) { mutableDoubleStateOf(strategy?.profile?.trailing ?: 0.0) }
    var buy by remember(strategy?.number, strategy?.profile?.buyEnabled) { mutableStateOf(strategy?.profile?.buyEnabled ?: true) }
    var sell by remember(strategy?.number, strategy?.profile?.sellEnabled) { mutableStateOf(strategy?.profile?.sellEnabled ?: true) }

    Card(colors = CardDefaults.cardColors(I2Panel), shape = RoundedCornerShape(21.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text("التحكم الرقمي بالسحب والإفلات", color = I2Gold, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text("حرّك الخط بإصبعك. أثناء السحب يصبح المؤشر أبيض/ناصع وتزداد الإضاءة؛ بعد الإفلات يعود إلى توهج خافت. القيم نفسها تُحفظ في AmarBot1RuntimeConfig.", color = I2Muted, fontSize = 8.sp)
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("اسم الاستراتيجية") }, singleLine = true)
            AmarDragValueControl("حجم اللوت", lot, 0.01, 1.00, 0.01, I2Green, "%.2f", { lot = it })
            AmarDragValueControl("مسافة الشبكة", grid, 1.0, 300.0, 1.0, I2Cyan, "%.0f", { grid = it })
            AmarDragValueControl("الحد الأقصى للأوامر", maxOrders, 1.0, 100.0, 1.0, I2Gold, "%.0f", { maxOrders = it })
            AmarDragValueControl("مضاعف اللوت", multiplier, 1.0, 5.0, 0.01, I2Purple, "%.2f", { multiplier = it })
            AmarDragValueControl("هدف السلة — دولار", basketTp, 0.0, 500.0, 0.5, I2Green, "%.2f", { basketTp = it })
            AmarDragValueControl("خسارة السلة — دولار", basketSl, -500.0, 0.0, 0.5, I2Red, "%.2f", { basketSl = it })
            AmarDragValueControl("التتبع — دولار", trailing, 0.0, 300.0, 0.5, I2Pink, "%.2f", { trailing = it })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Toggle2("BUY", buy, I2Green, { buy = !buy }, Modifier.weight(1f))
                Toggle2("SELL", sell, I2Pink, { sell = !sell }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Button(onClick = {
                    val p = AmarBot1RuntimeConfig(lot, grid, maxOrders.toInt().coerceAtLeast(1), multiplier, basketTp, basketSl, trailing.coerceAtLeast(0.0), buy, sell)
                    onSave(AmarSavedStrategy(number, name.ifBlank { "Strategy $number" }, p))
                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = I2Green, contentColor = Color.Black)) { Text("✓ حفظ القيم", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                Button(onClick = onDelete, modifier = Modifier.weight(.5f), colors = ButtonDefaults.buttonColors(containerColor = I2Red)) { Text("حذف", fontWeight = FontWeight.Black, fontSize = 10.sp) }
            }
        }
    }
}

@Composable
private fun Toggle2(label: String, enabled: Boolean, color: Color, onClick: () -> Unit, modifier: Modifier) {
    val animated by animateColorAsState(if (enabled) color else I2Panel2, tween(180), label = "toggle-$label")
    Button(onClick = onClick, modifier = modifier.height(42.dp), colors = ButtonDefaults.buttonColors(containerColor = animated, contentColor = if (enabled) Color.Black else I2Muted)) { Text(if (enabled) "● $label ON" else "○ $label OFF", fontWeight = FontWeight.Black, fontSize = 10.sp) }
}

@Composable
private fun CommandPanel2(onCommand: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(I2Panel), shape = RoundedCornerShape(19.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("أوامر البوت", color = I2Text, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Text("تبقى الأوامر محكومة وتُحفظ PENDING_MT5؛ هذه الواجهة لا تغيّر كود البوت.", color = I2Muted, fontSize = 8.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { onCommand("تشغيل") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = I2Green, contentColor = Color.Black)) { Text("تشغيل", fontSize = 9.sp, fontWeight = FontWeight.Black) }
                Button(onClick = { onCommand("إطفاء") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = I2Gold, contentColor = Color.Black)) { Text("إطفاء", fontSize = 9.sp, fontWeight = FontWeight.Black) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { onCommand("إغلاق الكل") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = I2Red)) { Text("إغلاق الكل", fontSize = 9.sp, fontWeight = FontWeight.Black) }
                Button(onClick = { onCommand("إعادة البناء") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = I2Cyan, contentColor = Color.Black)) { Text("إعادة البناء", fontSize = 9.sp, fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
private fun Notice2(text: String) {
    val transition = rememberInfiniteTransition(label = "notice2")
    val alpha by transition.animateFloat(.55f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "notice-alpha")
    Text(text, color = I2Cyan.copy(alpha = alpha), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().background(I2Panel2, RoundedCornerShape(14.dp)).border(1.dp, I2Cyan.copy(alpha = .7f), RoundedCornerShape(14.dp)).padding(12.dp))
}
