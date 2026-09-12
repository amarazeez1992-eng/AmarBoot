package com.personal.gridbot.amaros.bots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.runtime.AmarBotCommandEngine
import com.personal.gridbot.amaros.runtime.AmarBotOperationalEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val LabBg = Color(0xFF050C14)
private val LabPanel = Color(0xFF0A1722)
private val LabPanel2 = Color(0xFF0E2230)
private val LabLine = Color(0xFF1A3E4D)
private val LabCyan = Color(0xFF1DE5FF)
private val LabGreen = Color(0xFF00E6A0)
private val LabRed = Color(0xFFFF4F67)
private val LabGold = Color(0xFFFFC84A)
private val LabBlue = Color(0xFF5C9DFF)
private val LabText = Color(0xFFE9FBFF)
private val LabMuted = Color(0xFF7896A5)

private enum class LabMode { BOT, MARKET }
private enum class CommandVisualState { READY, RUNNING, SUCCESS, ERROR }

@Composable
fun AmarBotLabProfessionalScreen(onBackHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember(context) { AmarBotVaultRepository(context) }
    val commandEngine = remember(context) { AmarBotCommandEngine(context) }
    val operationalEngine = remember(context) { AmarBotOperationalEngine(context) }
    val scope = rememberCoroutineScope()
    var bots by remember { mutableStateOf(repo.load()) }
    val selectedBot = AmarBotLabSelectionContext.selectedBot
    var selectedStrategy by remember { mutableIntStateOf(1) }
    var mode by remember { mutableStateOf(LabMode.BOT) }
    var notice by remember { mutableStateOf("") }
    var commandState by remember { mutableStateOf(CommandVisualState.READY) }
    var commandName by remember { mutableStateOf("") }
    val currentBot = bots.firstOrNull { it.botNumber == selectedBot } ?: bots.firstOrNull()
    val currentStrategy = currentBot?.strategies?.firstOrNull { it.number == selectedStrategy }

    fun refresh() { bots = repo.load() }

    LaunchedEffect(Unit) { operationalEngine.ensureBotCatalog() }
    LaunchedEffect(commandState) {
        if (commandState == CommandVisualState.SUCCESS || commandState == CommandVisualState.ERROR) {
            delay(2600)
            commandState = CommandVisualState.READY
            commandName = ""
        }
    }

    Surface(color = LabBg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            LabHeader(currentBot, onBackHome)
            LabTopSwitch(mode) { mode = it }
            AnimatedContent(targetState = mode, label = "lab-mode") { selectedMode ->
                when (selectedMode) {
                    LabMode.BOT -> BotLabContent(
                        bots = bots,
                        selectedBot = selectedBot,
                        selectedStrategy = selectedStrategy,
                        currentBot = currentBot,
                        currentStrategy = currentStrategy,
                        commandState = commandState,
                        commandName = commandName,
                        notice = notice,
                        onSelectBot = { AmarBotLabSelectionContext.selectedBot = it.coerceIn(1, 10) },
                        onSelectStrategy = { selectedStrategy = it },
                        onCommand = { name ->
                            commandName = name
                            commandState = CommandVisualState.RUNNING
                            scope.launch {
                                runCatching { commandEngine.queue(selectedBot, name) }
                                    .onSuccess { id ->
                                        commandState = CommandVisualState.READY
                                        notice = "تم حفظ الطلب #$id للبوت ${selectedBot.toString().padStart(2, '0')} — PENDING_MT5. لم يُدّعَ تنفيذ الوسيط."
                                    }
                                    .onFailure {
                                        commandState = CommandVisualState.ERROR
                                        notice = "فشل حفظ طلب $name: ${it.message ?: "خطأ غير معروف"}"
                                    }
                            }
                        },
                        onSaveStrategy = { strategy ->
                            repo.saveStrategy(selectedBot, strategy)
                            refresh()
                            notice = "✓ تم حفظ الاستراتيجية ${strategy.number} في مخزن البوت"
                        },
                        onDeleteStrategy = { number ->
                            repo.deleteStrategy(selectedBot, number)
                            refresh()
                            notice = "تم حذف الاستراتيجية $number"
                        }
                    )
                    LabMode.MARKET -> MarketLabContent()
                }
            }
        }
    }
}

@Composable
private fun LabHeader(currentBot: AmarSavedBot?, onBackHome: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(LabPanel).padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Button(onClick = onBackHome, colors = ButtonDefaults.buttonColors(containerColor = LabGold, contentColor = Color.Black), contentPadding = PaddingValues(horizontal = 13.dp, vertical = 4.dp)) { Text("⌂", fontWeight = FontWeight.Black) }
        Column(Modifier.weight(1f)) {
            Text("AMAR BOT LAB", color = LabCyan, fontSize = 19.sp, fontWeight = FontWeight.Black)
            Text("${currentBot?.name ?: "BOT 1"} • ${currentBot?.botNumber ?: 1}/10", color = LabMuted, fontSize = 9.sp)
        }
        LiveDot()
    }
}

@Composable
private fun LiveDot() {
    val transition = rememberInfiniteTransition(label = "live")
    val alpha by transition.animateFloat(0.35f, 1f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "alpha")
    Box(Modifier.size(11.dp).alpha(alpha).background(LabGreen, CircleShape))
}

@Composable
private fun LabTopSwitch(mode: LabMode, select: (LabMode) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        LabTopTab("🤖  البوت", mode == LabMode.BOT, Modifier.weight(1f)) { select(LabMode.BOT) }
        LabTopTab("📊  حالة السوق", mode == LabMode.MARKET, Modifier.weight(1f)) { select(LabMode.MARKET) }
    }
}

@Composable
private fun LabTopTab(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color by animateColorAsState(if (selected) LabCyan else LabPanel2, tween(220), label = "tab-color")
    Box(modifier.height(49.dp).background(color, RoundedCornerShape(15.dp)).border(1.dp, if (selected) LabCyan else LabLine, RoundedCornerShape(15.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, color = if (selected) Color.Black else LabText, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}

@Composable
private fun BotLabContent(
    bots: List<AmarSavedBot>, selectedBot: Int, selectedStrategy: Int, currentBot: AmarSavedBot?, currentStrategy: AmarSavedStrategy?,
    commandState: CommandVisualState, commandName: String, notice: String,
    onSelectBot: (Int) -> Unit, onSelectStrategy: (Int) -> Unit, onCommand: (String) -> Unit,
    onSaveStrategy: (AmarSavedStrategy) -> Unit, onDeleteStrategy: (Int) -> Unit
) {
    val catalogCount = bots.size
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { AmarBotLabGlobalContext() }
        item { BotPicker(selectedBot, onSelectBot) }
        item { Text("كتالوج البوتات المحفوظ: $catalogCount/10", color = if (catalogCount >= 10) LabGreen else LabGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) }
        item { StrategyPicker(currentBot, selectedStrategy, onSelectStrategy) }
        item { StrategyEditor(currentStrategy, selectedStrategy, onSaveStrategy, onDeleteStrategy) }
        item { BotCommandPanel(commandState, commandName, onCommand) }
        if (notice.isNotBlank()) item { NoticeBanner(notice) }
    }
}

@Composable
private fun BotPicker(selected: Int, select: (Int) -> Unit) {
    LabCard("إدارة البوتات", "10 بطاقات • سحب وإفلات فعلي") {
        AmarBotDragDropBoard(botNumbers = (1..10).toList(), selectedBot = selected, onSelectBot = select, onReorder = { })
    }
}

@Composable
private fun StrategyPicker(bot: AmarSavedBot?, selected: Int, select: (Int) -> Unit) {
    LabCard("استراتيجيات ${bot?.name ?: "البوت"}", "10 خانات مستقلة") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..10).forEach { n -> SelectChip("${n.toString().padStart(2, '0')}", selected == n, Modifier.weight(1f)) { select(n) } }
        }
        Spacer(Modifier.height(7.dp))
        Text("الاستراتيجية المحددة: ${selected.toString().padStart(2, '0')}", color = LabCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun SelectChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color by animateColorAsState(if (selected) LabCyan else LabPanel2, tween(180), label = "chip")
    Box(modifier.height(37.dp).background(color, RoundedCornerShape(10.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(text, color = if (selected) Color.Black else LabText, fontSize = 9.sp, fontWeight = FontWeight.Black) }
}

@Composable
private fun StrategyEditor(strategy: AmarSavedStrategy?, number: Int, save: (AmarSavedStrategy) -> Unit, delete: (Int) -> Unit) {
    var name by remember(strategy?.number, strategy?.name) { mutableStateOf(strategy?.name ?: "Strategy ${number.toString().padStart(2, '0')}") }
    var lot by remember(strategy?.number, strategy?.profile?.lot) { mutableStateOf((strategy?.profile?.lot ?: 0.01).toString()) }
    var step by remember(strategy?.number, strategy?.profile?.gridStep) { mutableStateOf((strategy?.profile?.gridStep ?: 30.0).toString()) }
    var max by remember(strategy?.number, strategy?.profile?.maxOrders) { mutableStateOf((strategy?.profile?.maxOrders ?: 10).toString()) }
    var multiplier by remember(strategy?.number, strategy?.profile?.multiplier) { mutableStateOf((strategy?.profile?.multiplier ?: 2.0).toString()) }
    var tp by remember(strategy?.number, strategy?.profile?.basketTp) { mutableStateOf((strategy?.profile?.basketTp ?: 50.0).toString()) }
    var sl by remember(strategy?.number, strategy?.profile?.basketSl) { mutableStateOf((strategy?.profile?.basketSl ?: -30.0).toString()) }
    var trailing by remember(strategy?.number, strategy?.profile?.trailing) { mutableStateOf((strategy?.profile?.trailing ?: 0.0).toString()) }
    var buy by remember(strategy?.number, strategy?.profile?.buyEnabled) { mutableStateOf(strategy?.profile?.buyEnabled ?: true) }
    var sell by remember(strategy?.number, strategy?.profile?.sellEnabled) { mutableStateOf(strategy?.profile?.sellEnabled ?: true) }

    LabCard("إعدادات الاستراتيجية ${number.toString().padStart(2, '0')}", if (strategy == null) "جديدة" else "محفوظة") {
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("اسم الاستراتيجية") }, singleLine = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CompactField("Lot", lot, { lot = it }, Modifier.weight(1f)); CompactField("Grid", step, { step = it }, Modifier.weight(1f)); CompactField("Max", max, { max = it }, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CompactField("Multiplier", multiplier, { multiplier = it }, Modifier.weight(1f)); CompactField("Basket TP", tp, { tp = it }, Modifier.weight(1f)); CompactField("Basket SL", sl, { sl = it }, Modifier.weight(1f))
        }
        CompactField("Trailing", trailing, { trailing = it }, Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { ToggleState("BUY", buy, { buy = !buy }, Modifier.weight(1f)); ToggleState("SELL", sell, { sell = !sell }, Modifier.weight(1f)) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = {
                val p = AmarBot1RuntimeConfig(lot.toDoubleOrNull() ?: 0.01, step.toDoubleOrNull() ?: 30.0, (max.toIntOrNull() ?: 10).coerceAtLeast(1), multiplier.toDoubleOrNull() ?: 2.0, tp.toDoubleOrNull() ?: 50.0, sl.toDoubleOrNull() ?: -30.0, (trailing.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0), buy, sell)
                save(AmarSavedStrategy(number, name.ifBlank { "Strategy $number" }, p))
            }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = LabGreen, contentColor = Color.Black)) { Text(if (strategy == null) "＋ إضافة / حفظ" else "✓ حفظ التعديل", fontWeight = FontWeight.Black, fontSize = 10.sp) }
            Button(onClick = { delete(number) }, modifier = Modifier.weight(.55f), colors = ButtonDefaults.buttonColors(containerColor = LabRed, contentColor = Color.White)) { Text("حذف", fontWeight = FontWeight.Black, fontSize = 10.sp) }
        }
    }
}

@Composable
private fun CompactField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) { OutlinedTextField(value, onChange, modifier, label = { Text(label, fontSize = 9.sp) }, singleLine = true) }

@Composable
private fun ToggleState(label: String, enabled: Boolean, toggle: () -> Unit, modifier: Modifier) {
    val color by animateColorAsState(if (enabled) LabGreen else LabRed, tween(220), label = "toggle")
    Button(onClick = toggle, modifier = modifier.height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.Black), shape = RoundedCornerShape(11.dp)) { Text(if (enabled) "● $label  ON" else "○ $label  OFF", fontWeight = FontWeight.Black, fontSize = 10.sp) }
}

@Composable
private fun BotCommandPanel(state: CommandVisualState, name: String, command: (String) -> Unit) {
    LabCard("أوامر البوت", "محرك أوامر حقيقي • حالة PENDING حتى ربط MT5") {
        CommandButton("▶ تشغيل", LabGreen, state, name == "تشغيل") { command("تشغيل") }
        CommandButton("■ إطفاء", LabGold, state, name == "إطفاء") { command("إطفاء") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CommandButton("إغلاق الشراء", LabRed, state, name == "إغلاق الشراء", Modifier.weight(1f)) { command("إغلاق الشراء") }
            CommandButton("إغلاق البيع", LabRed, state, name == "إغلاق البيع", Modifier.weight(1f)) { command("إغلاق البيع") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CommandButton("✕ إغلاق الكل", LabRed, state, name == "إغلاق الكل", Modifier.weight(1f)) { command("إغلاق الكل") }
            CommandButton("↻ إعادة البناء", LabCyan, state, name == "إعادة البناء", Modifier.weight(1f)) { command("إعادة البناء") }
        }
        CommandButton("⚠ إغلاق الطوارئ", LabRed, state, name == "إغلاق الطوارئ") { command("إغلاق الطوارئ") }
    }
}

@Composable
private fun CommandButton(label: String, base: Color, state: CommandVisualState, active: Boolean, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    val color = when { active && state == CommandVisualState.RUNNING -> LabGold; active && state == CommandVisualState.SUCCESS -> LabGreen; active && state == CommandVisualState.ERROR -> LabRed; else -> base }
    val shown = when { active && state == CommandVisualState.RUNNING -> "◌ حفظ الطلب..."; active && state == CommandVisualState.SUCCESS -> "✓ تم حفظ الطلب"; active && state == CommandVisualState.ERROR -> "✕ فشل"; else -> label }
    Button(onClick = onClick, modifier = modifier.height(43.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if (color == LabGold || color == LabCyan || color == LabGreen) Color.Black else Color.White), shape = RoundedCornerShape(12.dp)) { Text(shown, fontWeight = FontWeight.Black, fontSize = 10.sp) }
}

@Composable
private fun MarketLabContent() {
    val provider = com.personal.gridbot.amaros.broker.AmarMt5RuntimeRegistry.provider()
    val frames = com.personal.gridbot.amaros.chart.AmarTimeframe.values().toList()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { AmarBotLabGlobalContext() }
        item { MarketSummaryCard(provider != null, frames.size) }
        items(frames) { tf -> MarketFrameCard(tf, provider) }
    }
}

@Composable
private fun MarketSummaryCard(connected: Boolean, count: Int) {
    LabCard("حالة السوق العامة", if (connected) "LIVE / READ-ONLY" else "WAITING DATA") {
        Text("جميع الفريمات: $count", color = LabMuted, fontSize = 9.sp)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (connected) "بيانات متصلة" else "لا توجد بيانات MT5 مؤكدة", color = if (connected) LabGreen else LabGold, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("—", color = LabMuted, fontSize = 32.sp, fontWeight = FontWeight.Black)
        }
        Text("النسبة العامة لا تُحتسب إلا من بيانات حقيقية؛ لا توجد أرقام وهمية.", color = LabMuted, fontSize = 9.sp)
    }
}

@Composable
private fun MarketFrameCard(tf: com.personal.gridbot.amaros.chart.AmarTimeframe, provider: com.personal.gridbot.amaros.broker.AmarMarketDataProvider?) {
    val candles = remember(provider, tf) { runCatching { provider?.candles("XAUUSD", tf).orEmpty() }.getOrDefault(emptyList()) }
    val last = candles.lastOrNull(); val previous = candles.dropLast(1).lastOrNull()
    val buy = if (last != null && previous != null) ((last.close - previous.close) >= 0.0) else null
    val pct = if (last != null && previous != null && previous.close != 0.0) ((kotlin.math.abs(last.close - previous.close) / previous.close) * 100.0).coerceIn(0.0, 100.0) else null
    val stateText = when (buy) { true -> "شرائي"; false -> "بيعي"; null -> "UNKNOWN" }
    val stateColor = when (buy) { true -> LabGreen; false -> LabRed; null -> LabGold }
    Card(colors = CardDefaults.cardColors(LabPanel), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(tf.shortLabel, color = LabCyan, fontWeight = FontWeight.Black, fontSize = 15.sp); Text(tf.arabicLabel, color = LabMuted, fontSize = 8.sp) }
            Text(stateText, color = stateColor, fontWeight = FontWeight.Black, fontSize = 12.sp); Spacer(Modifier.width(10.dp)); Text(if (pct == null) "—" else String.format(java.util.Locale.US, "%.1f%%", pct), color = stateColor, fontWeight = FontWeight.Black, fontSize = 15.sp); Box(Modifier.padding(start = 9.dp).size(12.dp).background(stateColor, CircleShape))
        }
    }
}

@Composable
private fun LabCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(LabPanel), shape = RoundedCornerShape(19.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title, color = LabText, fontSize = 16.sp, fontWeight = FontWeight.Black); Text(subtitle, color = LabMuted, fontSize = 8.sp) }
            content()
        }
    }
}

@Composable
private fun NoticeBanner(text: String) {
    val transition = rememberInfiniteTransition(label = "notice")
    val alpha by transition.animateFloat(.65f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "notice-alpha")
    Box(Modifier.fillMaxWidth().alpha(alpha).background(LabPanel2, RoundedCornerShape(14.dp)).border(1.dp, LabCyan, RoundedCornerShape(14.dp)).padding(12.dp)) { Text(text, color = LabCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
}
