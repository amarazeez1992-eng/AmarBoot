package com.personal.gridbot.amaros.bots

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
private val I2Pink = Color(0xFFFF4FA3)

@Composable
fun AmarBotLabInterface2Screen(onBackHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember(context) { AmarBotVaultRepository(context) }
    val command = remember(context) { AmarBotCommandEngine(context) }
    val operational = remember(context) { AmarBotOperationalEngine(context) }
    val scope = rememberCoroutineScope()
    var bots by remember { mutableStateOf(repo.load()) }
    var selectedBot by remember { mutableIntStateOf(AmarBotLabSelectionContext.selectedBot.coerceIn(1, 10)) }
    var selectedStrategy by remember { mutableIntStateOf(1) }
    var notice by remember { mutableStateOf("") }
    val bot = bots.firstOrNull { it.botNumber == selectedBot } ?: bots.firstOrNull()
    val strategy = bot?.strategies?.firstOrNull { it.number == selectedStrategy }

    LaunchedEffect(Unit) { runCatching { operational.ensureBotCatalog() } }

    Surface(Modifier.fillMaxSize(), color = I2Bg) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onBackHome, colors = ButtonDefaults.buttonColors(containerColor = I2Gold, contentColor = Color.Black)) { Text("⌂") }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text("AMAR BOT LAB", color = I2Cyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("الواجهة 2 • السحب والإفلات", color = I2Muted, fontSize = 9.sp)
                    }
                    Text("LIVE", color = I2Green, fontWeight = FontWeight.Black, fontSize = 9.sp)
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(I2Panel), shape = RoundedCornerShape(19.dp)) {
                    Column(Modifier.padding(11.dp)) {
                        Text("خزنة البوتات", color = I2Text, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(7.dp))
                        AmarBotDragDropBoard((1..10).toList(), selectedBot, { value ->
                            selectedBot = value
                            AmarBotLabSelectionContext.selectedBot = value
                            selectedStrategy = 1
                        }, {})
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(I2Panel), shape = RoundedCornerShape(19.dp)) {
                    Column(Modifier.padding(11.dp)) {
                        Text("استراتيجيات ${bot?.name ?: "البوت"}", color = I2Text, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(7.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..10).forEach { n ->
                                val color by animateColorAsState(if (n == selectedStrategy) I2Cyan else I2Panel2, label = "strategy_$n")
                                Box(
                                    Modifier.weight(1f).height(36.dp).background(color, RoundedCornerShape(9.dp))
                                        .border(1.dp, if (n == selectedStrategy) I2Cyan else I2Line, RoundedCornerShape(9.dp))
                                        .clickable { selectedStrategy = n },
                                    contentAlignment = Alignment.Center
                                ) { Text("V$n", color = if (n == selectedStrategy) Color.Black else I2Text, fontSize = 9.sp, fontWeight = FontWeight.Black) }
                            }
                        }
                    }
                }
            }
            item {
                Interface2Editor(strategy, selectedStrategy,
                    save = { value -> repo.saveStrategy(selectedBot, value); bots = repo.load(); notice = "✓ تم الحفظ" },
                    delete = { repo.deleteStrategy(selectedBot, selectedStrategy); bots = repo.load(); notice = "تم الحذف" }
                )
            }
            item {
                Card(colors = CardDefaults.cardColors(I2Panel), shape = RoundedCornerShape(19.dp)) {
                    Column(Modifier.padding(11.dp)) {
                        Text("أوامر البوت", color = I2Text, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(7.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            listOf("تشغيل" to I2Green, "إطفاء" to I2Gold, "إغلاق الكل" to I2Red, "إعادة البناء" to I2Cyan).forEach { (label, color) ->
                                Button(
                                    onClick = {
                                        scope.launch {
                                            runCatching { command.queue(selectedBot, label) }
                                                .onSuccess { notice = "✓ الطلب محفوظ #$it" }
                                                .onFailure { notice = "✕ ${it.message ?: "تعذر حفظ الطلب"}" }
                                        }
                                    },
                                    Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.Black)
                                ) { Text(label, fontSize = 7.sp, fontWeight = FontWeight.Black) }
                            }
                        }
                    }
                }
            }
            if (notice.isNotBlank()) {
                item { Text(notice, color = I2Cyan, modifier = Modifier.fillMaxWidth().background(I2Panel2, RoundedCornerShape(14.dp)).padding(12.dp)) }
            }
        }
    }
}

@Composable
private fun Interface2Editor(s: AmarSavedStrategy?, number: Int, save: (AmarSavedStrategy) -> Unit, delete: () -> Unit) {
    var name by remember(s?.number, s?.name) { mutableStateOf(s?.name ?: "Strategy $number") }
    var lot by remember(s?.number, s?.profile?.lot) { mutableDoubleStateOf(s?.profile?.lot ?: 0.01) }
    var grid by remember(s?.number, s?.profile?.gridStep) { mutableDoubleStateOf(s?.profile?.gridStep ?: 30.0) }
    var maxOrders by remember(s?.number, s?.profile?.maxOrders) { mutableDoubleStateOf((s?.profile?.maxOrders ?: 10).toDouble()) }
    var multiplier by remember(s?.number, s?.profile?.multiplier) { mutableDoubleStateOf(s?.profile?.multiplier ?: 2.0) }
    var tp by remember(s?.number, s?.profile?.basketTp) { mutableDoubleStateOf(s?.profile?.basketTp ?: 50.0) }
    var sl by remember(s?.number, s?.profile?.basketSl) { mutableDoubleStateOf(s?.profile?.basketSl ?: -30.0) }
    var trailing by remember(s?.number, s?.profile?.trailing) { mutableDoubleStateOf(s?.profile?.trailing ?: 0.0) }
    var buy by remember(s?.number, s?.profile?.buyEnabled) { mutableStateOf(s?.profile?.buyEnabled ?: true) }
    var sell by remember(s?.number, s?.profile?.sellEnabled) { mutableStateOf(s?.profile?.sellEnabled ?: true) }

    Card(colors = CardDefaults.cardColors(I2Panel), shape = RoundedCornerShape(21.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("التحكم الرقمي بالسحب والإفلات", color = I2Gold, fontSize = 17.sp, fontWeight = FontWeight.Black)
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("اسم الاستراتيجية") }, singleLine = true)
            D2("اللوت", lot, 0.01, 1.0, 0.01) { lot = it }
            D2("المسافة", grid, 1.0, 300.0, 1.0) { grid = it }
            D2("أقصى عدد أوامر", maxOrders, 1.0, 100.0, 1.0) { maxOrders = it }
            D2("مضاعف اللوت", multiplier, 1.0, 5.0, 0.01) { multiplier = it }
            D2("هدف السلة $", tp, 0.0, 500.0, 0.5) { tp = it }
            D2("وقف السلة $", sl, -500.0, 0.0, 0.5) { sl = it }
            D2("التريلينج $", trailing, 0.0, 300.0, 0.5) { trailing = it }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button({ buy = !buy }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (buy) I2Green else I2Panel2)) { Text("شراء") }
                Button({ sell = !sell }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (sell) I2Pink else I2Panel2)) { Text("بيع") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button({
                    save(AmarSavedStrategy(number, name.ifBlank { "Strategy $number" }, AmarBot1RuntimeConfig(lot, grid, maxOrders.toInt().coerceAtLeast(1), multiplier, tp, sl, trailing.coerceAtLeast(0.0), buy, sell)))
                }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = I2Green, contentColor = Color.Black)) { Text("✓ حفظ") }
                Button(delete, Modifier.weight(0.5f), colors = ButtonDefaults.buttonColors(containerColor = I2Red)) { Text("حذف") }
            }
        }
    }
}

@Composable
private fun D2(label: String, value: Double, min: Double, max: Double, step: Double, onValue: (Double) -> Unit) {
    AmarDragValueControl(label, value, min, max, step, I2Cyan, "%.2f", onValue, Modifier.fillMaxWidth())
}
