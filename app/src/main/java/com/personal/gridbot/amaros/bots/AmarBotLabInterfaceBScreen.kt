package com.personal.gridbot.amaros.bots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.broker.AmarMt5RuntimeRegistry
import com.personal.gridbot.amaros.chart.AmarTimeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.round

private val B_BG = Color(0xFF050817)
private val B_PANEL = Color(0xFF0B1230)
private val B_PANEL2 = Color(0xFF101A3D)
private val B_TEXT = Color(0xFFF3FAFF)
private val B_MUTED = Color(0xFF9BAED0)
private val B_CYAN = Color(0xFF24E8FF)
private val B_GREEN = Color(0xFF18F2A4)
private val B_RED = Color(0xFFFF4F78)
private val B_YELLOW = Color(0xFFFFD166)
private val B_PINK = Color(0xFFFF6AD5)

private enum class BMode { BOT, MARKET }
private data class BFrame(val bias: Int?, val percent: Double?, val remainingMs: Long, val available: Boolean)
private data class BLive(val connected: Boolean, val balance: Double?, val equity: Double?, val positions: Int?, val pending: Int?, val floating: Double?)

@Composable
fun AmarBotLabInterfaceBScreen(
    onBackHome: () -> Unit,
    selectedBot: Int,
    onBotSelected: (Int) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember(context) { AmarBotVaultRepository(context) }
    var bots by remember { mutableStateOf(repo.load()) }
    var mode by remember { mutableStateOf(BMode.BOT) }
    var live by remember { mutableStateOf(BLive(false, null, null, null, null, null)) }
    var frames by remember { mutableStateOf<Map<AmarTimeframe, BFrame>>(emptyMap()) }
    var entryTf by remember { mutableStateOf(AmarTimeframe.M1) }
    var notice by remember { mutableStateOf("") }
    val runtime = AmarMt5RuntimeRegistry.current()
    val magic = if (selectedBot == 1) 20260908L else null
    val strategy = bots.firstOrNull { it.botNumber == selectedBot }?.strategies?.firstOrNull { it.number == 1 }

    LaunchedEffect(runtime, selectedBot) {
        while (true) {
            val client = runtime?.client
            live = if (client == null) {
                BLive(false, null, null, null, null, null)
            } else withContext(Dispatchers.IO) {
                runCatching {
                    val account = client.account()
                    val bot = client.botStatus("XAUUSD", magic)
                    BLive(account.connected, account.balance, account.equity, bot.positions, bot.pendingOrders, bot.floatingProfit)
                }.getOrDefault(BLive(false, null, null, null, null, null))
            }
            delay(1500)
        }
    }

    LaunchedEffect(runtime) {
        while (true) {
            if (runtime == null) {
                frames = emptyMap()
                delay(2500)
                continue
            }
            val next = mutableMapOf<AmarTimeframe, BFrame>()
            for (tf in AmarTimeframe.entries) {
                val candles = withContext(Dispatchers.IO) {
                    runCatching { runtime.marketData.refresh("XAUUSD", tf) }.getOrNull()
                }
                val previous = candles?.dropLast(1)?.lastOrNull()
                val last = candles?.lastOrNull()
                val delta = if (previous != null && last != null) last.close - previous.close else null
                val percent = if (delta != null && previous != null && previous.close != 0.0) {
                    (abs(delta) / previous.close * 100.0).coerceIn(0.0, 100.0)
                } else null
                next[tf] = BFrame(
                    bias = delta?.let { if (it > 0) 1 else if (it < 0) -1 else 0 },
                    percent = percent,
                    remainingMs = timeframeRemaining(tf, System.currentTimeMillis()),
                    available = !candles.isNullOrEmpty()
                )
            }
            frames = next
            delay(15000)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            frames = frames.mapValues { (tf, value) -> value.copy(remainingMs = timeframeRemaining(tf, System.currentTimeMillis())) }
        }
    }

    Box(Modifier.fillMaxSize().background(B_BG)) {
        Column(Modifier.fillMaxSize()) {
            BHeader(selectedBot, live, onBackHome)
            BModeTabs(mode) { mode = it }
            AnimatedContent(targetState = mode, label = "interface-b-mode") { selectedMode ->
                if (selectedMode == BMode.BOT) {
                    BBotContent(
                        selectedBot = selectedBot,
                        strategy = strategy,
                        entryTf = entryTf,
                        live = live,
                        notice = notice,
                        onBotSelected = { onBotSelected(it); notice = "تم اختيار V$it" },
                        onTfSelected = { entryTf = it },
                        onNotice = { notice = it },
                        onSave = { saved -> repo.saveStrategy(selectedBot, saved); bots = repo.load(); notice = "✓ تم حفظ الاستراتيجية" },
                        onDelete = { number -> repo.deleteStrategy(selectedBot, number); bots = repo.load(); notice = "تم حذف الاستراتيجية $number" }
                    )
                } else {
                    BMarketContent(frames, overallMarket(frames))
                }
            }
        }
    }
}

@Composable
private fun BHeader(bot: Int, live: BLive, onBack: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "live-pulse").animateFloat(
        0.45f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "alpha"
    )
    Column(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(B_PANEL, Color(0xFF28134A), B_PANEL)))) {
        Row(Modifier.fillMaxWidth().padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, shape = RoundedCornerShape(4.dp, 18.dp, 4.dp, 18.dp), colors = ButtonDefaults.buttonColors(containerColor = B_PINK, contentColor = Color.White)) { Text("⌂") }
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text("AMAR • INTERFACE B", color = B_CYAN, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text("V$bot • مختبر هاتف حي", color = B_MUTED, fontSize = 9.sp)
            }
            Box(Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(if (live.connected) B_GREEN.copy(alpha = pulse) else B_MUTED))
        }
        if (live.connected) {
            Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LiveMetric("الرصيد", money(live.balance), B_CYAN, Modifier.weight(1f))
                LiveMetric("صفقات", "${live.positions ?: 0}", B_GREEN, Modifier.weight(.75f))
                LiveMetric("معلقة", "${live.pending ?: 0}", B_YELLOW, Modifier.weight(.75f))
                LiveMetric("الربح/الخسارة", money(live.floating), if ((live.floating ?: 0.0) >= 0) B_GREEN else B_RED, Modifier.weight(1.25f))
            }
        }
    }
}

@Composable
private fun LiveMetric(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier.background(B_PANEL2, RoundedCornerShape(10.dp)).border(1.dp, color.copy(alpha = .3f), RoundedCornerShape(10.dp)).padding(6.dp)) {
        Text(label, color = B_MUTED, fontSize = 7.sp, maxLines = 1)
        Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun BModeTabs(mode: BMode, onSelect: (BMode) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(7.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BTab("🤖 البوت", mode == BMode.BOT, Modifier.weight(1f)) { onSelect(BMode.BOT) }
        BTab("◈ حالة السوق", mode == BMode.MARKET, Modifier.weight(1f)) { onSelect(BMode.MARKET) }
    }
}

@Composable
private fun BTab(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color by animateColorAsState(if (selected) B_CYAN else B_PANEL2, tween(220), label = "tab-color")
    Box(modifier.height(43.dp).background(color, RoundedCornerShape(13.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, color = if (selected) Color.Black else B_TEXT, fontWeight = FontWeight.Black, fontSize = 11.sp)
    }
}

@Composable
private fun BBotContent(
    selectedBot: Int,
    strategy: AmarSavedStrategy?,
    entryTf: AmarTimeframe,
    live: BLive,
    notice: String,
    onBotSelected: (Int) -> Unit,
    onTfSelected: (AmarTimeframe) -> Unit,
    onNotice: (String) -> Unit,
    onSave: (AmarSavedStrategy) -> Unit,
    onDelete: (Int) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { BCard("البوتات", "V1 إلى V10") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) { (1..10).forEach { n -> BChip("V$n", n == selectedBot, Modifier.weight(1f)) { onBotSelected(n) } } } } }
        item { BEntryFrame(entryTf, onTfSelected) }
        if (live.connected) item { BLivePanel(live) }
        item { BStrategyEditor(strategy, selectedBot, onSave, onDelete, onNotice) }
        item { BCommands(onNotice, live.connected) }
        if (notice.isNotBlank()) item { BNotice(notice) }
    }
}

@Composable
private fun BChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(34.dp).background(if (selected) B_GREEN else B_PANEL2, RoundedCornerShape(8.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, color = if (selected) Color.Black else B_TEXT, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BEntryFrame(selected: AmarTimeframe, onSelect: (AmarTimeframe) -> Unit) {
    BCard("فريم الدخول", "اختيار الفريم مع عداد حي") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(AmarTimeframe.M1, AmarTimeframe.M5, AmarTimeframe.M15, AmarTimeframe.M30, AmarTimeframe.H1).forEach { tf ->
                BChip(tf.shortLabel, tf == selected, Modifier.weight(1f)) { onSelect(tf) }
            }
        }
        Text("${selected.arabicLabel} • المتبقي ${formatRemaining(timeframeRemaining(selected, System.currentTimeMillis()))}", color = B_CYAN, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BLivePanel(live: BLive) {
    val active = (live.positions ?: 0) > 0 || (live.pending ?: 0) > 0
    BCard("المراقبة الحية", if (active) "صفقات نشطة — التفاصيل ظاهرة" else "متصل — بانتظار صفقة") {
        Text(if ((live.floating ?: 0.0) < 0) "⚠ تنبيه خسارة" else "✓ الوضع مستقر", color = if ((live.floating ?: 0.0) < 0) B_RED else B_GREEN, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Text("الصفقات ${live.positions ?: 0} • المعلقة ${live.pending ?: 0} • العائم ${money(live.floating)}", color = B_TEXT, fontSize = 9.sp)
    }
}

@Composable
private fun BStrategyEditor(strategy: AmarSavedStrategy?, bot: Int, save: (AmarSavedStrategy) -> Unit, delete: (Int) -> Unit, notice: (String) -> Unit) {
    var lot by remember(strategy?.number, strategy?.profile?.lot) { mutableStateOf((strategy?.profile?.lot ?: .01).toFloat()) }
    var multiplier by remember(strategy?.number, strategy?.profile?.multiplier) { mutableStateOf((strategy?.profile?.multiplier ?: 2.0).toFloat()) }
    var grid by remember(strategy?.number, strategy?.profile?.gridStep) { mutableStateOf((strategy?.profile?.gridStep ?: 30.0).toFloat()) }
    var maxOrders by remember(strategy?.number, strategy?.profile?.maxOrders) { mutableStateOf((strategy?.profile?.maxOrders ?: 10).toFloat()) }
    var tp by remember(strategy?.number, strategy?.profile?.basketTp) { mutableStateOf((strategy?.profile?.basketTp ?: 50.0).toFloat()) }
    var sl by remember(strategy?.number, strategy?.profile?.basketSl) { mutableStateOf((strategy?.profile?.basketSl ?: -30.0).toFloat()) }
    var buy by remember(strategy?.number, strategy?.profile?.buyEnabled) { mutableStateOf(strategy?.profile?.buyEnabled ?: true) }
    var sell by remember(strategy?.number, strategy?.profile?.sellEnabled) { mutableStateOf(strategy?.profile?.sellEnabled ?: true) }

    BCard("المحرّك — سحب بالإصبع", "اسحب المؤشر ثم تطبيق أو حفظ") {
        BSlider("اللوت", lot, .01f..5f, .01f) { lot = it }
        BSlider("مضاعف الشبكة", multiplier, .5f..5f, .05f) { multiplier = it }
        BSlider("مسافة الشبكة", grid, 0f..500f, 1f) { grid = it }
        BSlider("عدد الأوامر", maxOrders, 1f..50f, 1f) { maxOrders = it }
        BSlider("هدف السلة $", tp, 0f..1000f, 1f) { tp = it }
        BSlider("وقف السلة $", sl, -1000f..0f, 1f) { sl = it }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            BToggle("BUY", buy, Modifier.weight(1f)) { buy = !buy }
            BToggle("SELL", sell, Modifier.weight(1f)) { sell = !sell }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Button(onClick = { notice("✓ تم تطبيق القيم على V$bot") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = B_CYAN, contentColor = Color.Black)) { Text("تطبيق", fontWeight = FontWeight.Black) }
            Button(onClick = { save(AmarSavedStrategy(1, "واجهة B • استراتيجية 01", AmarBot1RuntimeConfig(lot.toDouble(), grid.toDouble(), maxOrders.toInt().coerceAtLeast(1), multiplier.toDouble(), tp.toDouble(), sl.toDouble(), 0.0, buy, sell))) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = B_GREEN, contentColor = Color.Black)) { Text("حفظ", fontWeight = FontWeight.Black) }
            Button(onClick = { delete(1) }, colors = ButtonDefaults.buttonColors(containerColor = B_RED, contentColor = Color.White)) { Text("حذف") }
        }
    }
}

@Composable
private fun BSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, step: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = B_TEXT, fontSize = 8.sp, modifier = Modifier.weight(1f))
            Text(if (value < 10f) "%.2f".format(java.util.Locale.US, value) else "%.0f".format(java.util.Locale.US, value), color = B_CYAN, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Slider(value = value, onValueChange = { onChange((round(it / step) * step).coerceIn(range.start, range.endInclusive)) }, valueRange = range)
    }
}

@Composable
private fun BToggle(label: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color by animateColorAsState(if (enabled) B_GREEN else B_RED, tween(180), label = "toggle")
    Button(onClick = onClick, modifier = modifier.height(38.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.Black), shape = RoundedCornerShape(6.dp, 17.dp, 6.dp, 17.dp)) { Text(if (enabled) "● $label ON" else "○ $label OFF", fontSize = 9.sp, fontWeight = FontWeight.Black) }
}

@Composable
private fun BCommands(notice: (String) -> Unit, connected: Boolean) {
    BCard("أوامر سريعة", "الأخضر/الأحمر يعبّر عن الحالة البصرية؛ النجاح لا يظهر قبل التحقق") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            BCommand("إغلاق الشراء", B_RED, Modifier.weight(1f)) { notice("طلب إغلاق الشراء — انتظار تأكيد التنفيذ") }
            BCommand("إغلاق البيع", B_PINK, Modifier.weight(1f)) { notice("طلب إغلاق البيع — انتظار تأكيد التنفيذ") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            BCommand("إغلاق الكل", B_RED, Modifier.weight(1f)) { notice("⚠ طلب إغلاق الكل — انتظار التحقق") }
            BCommand("إعادة البناء", B_CYAN, Modifier.weight(1f)) { notice("طلب إعادة البناء — انتظار Runtime") }
        }
        Text(if (connected) "MT5 متصل للقراءة الحية." else "MT5 غير متصل — UNKNOWN.", color = if (connected) B_GREEN else B_MUTED, fontSize = 8.sp)
    }
}

@Composable
private fun BCommand(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(42.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if (color == B_CYAN) Color.Black else Color.White), shape = RoundedCornerShape(18.dp, 6.dp, 18.dp, 6.dp)) { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black) }
}

@Composable
private fun BMarketContent(frames: Map<AmarTimeframe, BFrame>, overall: Pair<Int?, Double?>) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            val color = when (overall.first) { 1 -> B_GREEN; -1 -> B_RED; else -> B_YELLOW }
            BCard("التقييم الكامل", "تجميع الفريمات المتاحة فقط") {
                Text(when (overall.first) { 1 -> "السوق متفق شرائياً"; -1 -> "السوق متفق بيعياً"; else -> "السوق غير محسوم" }, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(if (overall.second == null) "—" else "اتفاق ${"%.1f".format(java.util.Locale.US, overall.second)}%", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("المتاح ${frames.values.count { it.available }} / ${AmarTimeframe.entries.size}", color = B_MUTED, fontSize = 8.sp)
            }
        }
        items(AmarTimeframe.entries) { tf ->
            val state = frames[tf]
            val color = when (state?.bias) { 1 -> B_GREEN; -1 -> B_RED; else -> B_MUTED }
            Card(colors = CardDefaults.cardColors(B_PANEL), shape = RoundedCornerShape(13.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(tf.shortLabel, color = B_CYAN, fontWeight = FontWeight.Black, fontSize = 12.sp); Text(tf.arabicLabel, color = B_MUTED, fontSize = 7.sp) }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(when (state?.bias) { 1 -> "شرائي"; -1 -> "بيعي"; else -> "UNKNOWN" }, color = color, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        Text(if (state?.percent == null) "—" else "${"%.1f".format(java.util.Locale.US, state.percent)}%", color = color, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text("متبقي ${formatRemaining(state?.remainingMs ?: timeframeRemaining(tf, System.currentTimeMillis()))}", color = B_MUTED, fontSize = 7.sp)
                    }
                    Spacer(Modifier.width(7.dp)); Box(Modifier.size(10.dp).background(color, RoundedCornerShape(50)))
                }
            }
        }
    }
}

@Composable
private fun BCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(B_PANEL), shape = RoundedCornerShape(17.dp), modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF6D7BFF).copy(alpha = .25f), RoundedCornerShape(17.dp))) {
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, color = B_TEXT, fontWeight = FontWeight.Black, fontSize = 12.sp); Text(subtitle, color = B_MUTED, fontSize = 7.sp); content() }
    }
}

@Composable private fun BNotice(text: String) { Box(Modifier.fillMaxWidth().background(B_PANEL2, RoundedCornerShape(12.dp)).padding(9.dp)) { Text(text, color = B_TEXT, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) } }
private fun money(value: Double?): String = value?.let { "%.2f".format(java.util.Locale.US, it) } ?: "—"

private fun timeframeRemaining(tf: AmarTimeframe, nowMs: Long): Long {
    val instant = Instant.ofEpochMilli(nowMs); val utc = instant.atZone(ZoneOffset.UTC)
    val next = when (tf) {
        AmarTimeframe.MN1 -> utc.withDayOfMonth(1).plusMonths(1).truncatedTo(ChronoUnit.DAYS)
        AmarTimeframe.W1 -> utc.toLocalDate().plusDays((8 - utc.dayOfWeek.value).toLong()).atStartOfDay(ZoneOffset.UTC)
        AmarTimeframe.D1 -> utc.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC)
        AmarTimeframe.H1 -> utc.truncatedTo(ChronoUnit.HOURS).plusHours(1)
        AmarTimeframe.H2 -> nextFixed(utc, 2, true); AmarTimeframe.H3 -> nextFixed(utc, 3, true); AmarTimeframe.H4 -> nextFixed(utc, 4, true)
        AmarTimeframe.H6 -> nextFixed(utc, 6, true); AmarTimeframe.H8 -> nextFixed(utc, 8, true); AmarTimeframe.H12 -> nextFixed(utc, 12, true)
        AmarTimeframe.M1 -> nextFixed(utc, 1, false); AmarTimeframe.M2 -> nextFixed(utc, 2, false); AmarTimeframe.M3 -> nextFixed(utc, 3, false); AmarTimeframe.M4 -> nextFixed(utc, 4, false)
        AmarTimeframe.M5 -> nextFixed(utc, 5, false); AmarTimeframe.M6 -> nextFixed(utc, 6, false); AmarTimeframe.M10 -> nextFixed(utc, 10, false); AmarTimeframe.M12 -> nextFixed(utc, 12, false)
        AmarTimeframe.M15 -> nextFixed(utc, 15, false); AmarTimeframe.M20 -> nextFixed(utc, 20, false); AmarTimeframe.M30 -> nextFixed(utc, 30, false)
    }
    return Duration.between(instant, next.toInstant()).toMillis().coerceAtLeast(0)
}

private fun nextFixed(utc: ZonedDateTime, size: Int, hours: Boolean): ZonedDateTime {
    val current = if (hours) utc.hour else utc.minute; val next = ((current / size) + 1) * size
    return if (hours) { if (next >= 24) utc.truncatedTo(ChronoUnit.DAYS).plusDays(1) else utc.truncatedTo(ChronoUnit.DAYS).plusHours(next.toLong()) }
    else { if (next >= 60) utc.truncatedTo(ChronoUnit.HOURS).plusHours(1) else utc.truncatedTo(ChronoUnit.HOURS).plusMinutes(next.toLong()) }
}

private fun formatRemaining(ms: Long): String { val s = (ms / 1000).coerceAtLeast(0); return if (s >= 3600) "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60) else "%02d:%02d".format(s / 60, s % 60) }
private fun overallMarket(frames: Map<AmarTimeframe, BFrame>): Pair<Int?, Double?> { val values = frames.values.mapNotNull { it.bias }.filter { it != 0 }; if (values.isEmpty()) return null to null; val buy = values.count { it > 0 }; val sell = values.count { it < 0 }; return (if (buy >= sell) 1 else -1) to (maxOf(buy, sell).toDouble() / values.size * 100.0) }
