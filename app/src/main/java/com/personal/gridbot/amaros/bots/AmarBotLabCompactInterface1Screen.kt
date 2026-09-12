package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
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

private val B1Bg = Color(0xFF050C14)
private val B1Panel = Color(0xFF0A1722)
private val B1Panel2 = Color(0xFF0E2230)
private val B1Cyan = Color(0xFF1DE5FF)
private val B1Gold = Color(0xFFFFC84A)
private val B1Text = Color(0xFFE9FBFF)
private val B1Muted = Color(0xFF7896A5)

@Composable
fun AmarBotLabCompactInterface1Screen(onBackHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember(context) { AmarBotVaultRepository(context) }
    var bots by remember { mutableStateOf(repo.load()) }
    var selectedBot by remember { mutableIntStateOf(AmarBotLabSelectionContext.selectedBot.coerceIn(1, 10)) }
    var selectedStrategy by remember { mutableIntStateOf(1) }
    val bot = bots.firstOrNull { it.botNumber == selectedBot } ?: bots.firstOrNull()
    val strategy = bot?.strategies?.firstOrNull { it.number == selectedStrategy }

    var lot by remember(strategy?.number, strategy?.profile?.lot) { mutableDoubleStateOf(strategy?.profile?.lot ?: 0.01) }
    var grid by remember(strategy?.number, strategy?.profile?.gridStep) { mutableDoubleStateOf(strategy?.profile?.gridStep ?: 30.0) }
    var maxOrders by remember(strategy?.number, strategy?.profile?.maxOrders) { mutableDoubleStateOf((strategy?.profile?.maxOrders ?: 10).toDouble()) }
    var multiplier by remember(strategy?.number, strategy?.profile?.multiplier) { mutableDoubleStateOf(strategy?.profile?.multiplier ?: 2.0) }
    var tp by remember(strategy?.number, strategy?.profile?.basketTp) { mutableDoubleStateOf(strategy?.profile?.basketTp ?: 50.0) }
    var sl by remember(strategy?.number, strategy?.profile?.basketSl) { mutableDoubleStateOf(strategy?.profile?.basketSl ?: -30.0) }
    var trailing by remember(strategy?.number, strategy?.profile?.trailing) { mutableDoubleStateOf(strategy?.profile?.trailing ?: 0.0) }
    var buy by remember(strategy?.number, strategy?.profile?.buyEnabled) { mutableStateOf(strategy?.profile?.buyEnabled ?: true) }
    var sell by remember(strategy?.number, strategy?.profile?.sellEnabled) { mutableStateOf(strategy?.profile?.sellEnabled ?: true) }

    fun save() {
        repo.saveStrategy(selectedBot, AmarSavedStrategy(
            selectedStrategy,
            "Strategy $selectedStrategy",
            AmarBot1RuntimeConfig(lot, grid, maxOrders.toInt().coerceAtLeast(1), multiplier, tp, sl, trailing.coerceAtLeast(0.0), buy, sell)
        ))
        bots = repo.load()
    }

    Surface(Modifier.fillMaxSize(), color = B1Bg) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().background(B1Panel).padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBackHome, colors = ButtonDefaults.buttonColors(containerColor = B1Gold, contentColor = Color.Black)) { Text("⌂") }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("AMAR BOT LAB", color = B1Cyan, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("واجهة 1 • تحكم مباشر • V$selectedBot / V$selectedStrategy", color = B1Muted, fontSize = 9.sp)
                }
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(9.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { CompactStrip("البوتات", selectedBot) { selectedBot = it; AmarBotLabSelectionContext.selectedBot = it; selectedStrategy = 1 } }
                item { CompactStrip("الاستراتيجيات", selectedStrategy) { selectedStrategy = it } }
                item {
                    Card(colors = CardDefaults.cardColors(B1Panel), shape = RoundedCornerShape(15.dp)) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("الإعدادات الرقمية", color = B1Text, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            AmarDragValueControl("اللوت", lot, 0.01, 1.0, 0.01, B1Cyan, "%.2f", { lot = it }, Modifier.fillMaxWidth())
                            AmarDragValueControl("المسافة", grid, 1.0, 300.0, 1.0, B1Cyan, "%.0f", { grid = it }, Modifier.fillMaxWidth())
                            AmarDragValueControl("أقصى الأوامر", maxOrders, 1.0, 100.0, 1.0, B1Cyan, "%.0f", { maxOrders = it }, Modifier.fillMaxWidth())
                            AmarDragValueControl("مضاعف اللوت", multiplier, 1.0, 5.0, 0.01, B1Cyan, "%.2f", { multiplier = it }, Modifier.fillMaxWidth())
                            AmarDragValueControl("هدف السلة $", tp, 0.0, 500.0, 0.5, B1Cyan, "%.2f", { tp = it }, Modifier.fillMaxWidth())
                            AmarDragValueControl("وقف السلة $", sl, -500.0, 0.0, 0.5, B1Cyan, "%.2f", { sl = it }, Modifier.fillMaxWidth())
                            AmarDragValueControl("التريلينج $", trailing, 0.0, 300.0, 0.5, B1Cyan, "%.2f", { trailing = it }, Modifier.fillMaxWidth())
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                FilterChip(buy, { buy = !buy }, label = { Text("شراء") })
                                FilterChip(sell, { sell = !sell }, label = { Text("بيع") })
                                Spacer(Modifier.weight(1f))
                                Button(::save, colors = ButtonDefaults.buttonColors(containerColor = B1Cyan, contentColor = Color.Black)) { Text("حفظ") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactStrip(title: String, selected: Int, onSelect: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(B1Panel), shape = RoundedCornerShape(15.dp)) {
        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = B1Text, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text("V1–V10", color = B1Muted, fontSize = 8.sp)
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                (1..10).forEach { n ->
                    Button(
                        onClick = { onSelect(n) },
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (n == selected) B1Cyan else B1Panel2,
                            contentColor = if (n == selected) Color.Black else B1Text
                        )
                    ) { Text("V$n", fontSize = 8.sp, fontWeight = FontWeight.Black) }
                }
            }
        }
    }
}
