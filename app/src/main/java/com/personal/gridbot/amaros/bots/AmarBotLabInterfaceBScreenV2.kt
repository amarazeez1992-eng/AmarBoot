package com.personal.gridbot.amaros.bots

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.broker.AmarBot1RemoteCommandType
import com.personal.gridbot.amaros.broker.AmarBot1RemoteSettings
import com.personal.gridbot.amaros.broker.AmarBot1UiCommandGateway
import com.personal.gridbot.amaros.chart.AmarTimeframe
import java.util.Locale
import kotlin.math.round
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val B0 = Color(0xFF050817)
private val B1 = Color(0xFF0B1230)
private val B2 = Color(0xFF101A3D)
private val BT = Color(0xFFF3FAFF)
private val BM = Color(0xFF9BAED0)
private val BC = Color(0xFF24E8FF)
private val BG = Color(0xFF18F2A4)
private val BR = Color(0xFFFF4F78)
private val BP = Color(0xFFFF6AD5)

@Composable
fun AmarBotLabInterfaceBScreenV2(
    onBackHome: () -> Unit,
    selectedBot: Int,
    onBotSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val repo = remember(context) { AmarBotVaultRepository(context) }
    val scope = rememberCoroutineScope()
    var bots by remember { mutableStateOf(repo.load()) }
    var selectedStrategy by remember { mutableStateOf(1) }
    var notice by remember { mutableStateOf("") }
    val strategy = bots.firstOrNull { it.botNumber == selectedBot }
        ?.strategies?.firstOrNull { it.number == selectedStrategy }
    val timeframe = AmarTradingTimeframeContext.selected
    val symbol = AmarTradingSymbolContext.selected.brokerSymbol

    Column(Modifier.fillMaxSize().background(B0)) {
        Row(
            Modifier.fillMaxWidth().background(B1).padding(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackHome,
                colors = ButtonDefaults.buttonColors(containerColor = BP, contentColor = Color.White)
            ) { Text("⌂") }
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text("AMAR • INTERFACE B", color = BC, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text("V$selectedBot • مختبر الهاتف • ${symbol.ifBlank { "لا يوجد رمز" }}", color = BM, fontSize = 9.sp)
            }
            Box(Modifier.size(11.dp).background(BG, RoundedCornerShape(50)))
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { BotSelector(selectedBot, onBotSelected) }
            item { StrategySelector(selectedStrategy) { selectedStrategy = it } }
            item { TimeframeStatus(timeframe) }
            item {
                StrategyEditor(
                    strategy = strategy,
                    strategyNumber = selectedStrategy,
                    onSave = { saved ->
                        repo.saveStrategy(selectedBot, saved)
                        bots = repo.load()
                        notice = "✓ تم حفظ الاستراتيجية $selectedStrategy محليًا"
                    },
                    onDelete = { number ->
                        repo.deleteStrategy(selectedBot, number)
                        bots = repo.load()
                        notice = "تم حذف الاستراتيجية $number"
                    },
                    onApply = { settings ->
                        if (settings == null) {
                            notice = "الإعدادات الحالية صفرية/غير صالحة للتنفيذ؛ لم يتم إرسال أي أمر"
                        } else {
                            scope.launch {
                                notice = "جاري إرسال الإعدادات والتحقق من MT5…"
                                val result = AmarBot1UiCommandGateway.execute(
                                    symbol = symbol,
                                    command = AmarBot1RemoteCommandType.UPDATE_SETTINGS,
                                    targetSymbol = symbol,
                                    settings = settings,
                                )
                                notice = result.message
                            }
                        }
                    }
                )
            }
            item {
                QuickCommands(symbol) { command ->
                    scope.launch {
                        notice = "جاري تنفيذ ${command.label} والتحقق…"
                        val result = AmarBot1UiCommandGateway.execute(
                            symbol = symbol,
                            command = command.type,
                            targetSymbol = if (command.type == AmarBot1RemoteCommandType.REBUILD) symbol else null,
                        )
                        notice = result.message
                    }
                }
            }
            if (notice.isNotBlank()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = B2)) {
                        Text(
                            notice,
                            color = BT,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(9.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) BC else B2,
            contentColor = if (selected) Color.Black else BT
        )
    ) { Text(text, fontSize = 8.sp, fontWeight = FontWeight.Black) }
}

@Composable
private fun BotSelector(selectedBot: Int, onBotSelected: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = B1)) {
        Column(Modifier.padding(9.dp)) {
            Text("البوتات", color = BT, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                (1..10).forEach { bot ->
                    BChip("V$bot", bot == selectedBot, Modifier.weight(1f)) { onBotSelected(bot) }
                }
            }
        }
    }
}

@Composable
private fun StrategySelector(selectedStrategy: Int, onStrategySelected: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = B1)) {
        Column(Modifier.padding(9.dp)) {
            Text("الاستراتيجيات", color = BT, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                (1..10).forEach { number ->
                    BChip(number.toString().padStart(2, '0'), number == selectedStrategy, Modifier.weight(1f)) {
                        onStrategySelected(number)
                    }
                }
            }
            Text("الاستراتيجية ${selectedStrategy.toString().padStart(2, '0')}", color = BC, fontSize = 10.sp)
        }
    }
}

@Composable
private fun TimeframeStatus(timeframe: AmarTimeframe) {
    var remaining by remember(timeframe) { mutableStateOf(timeframe.remainingMillis()) }
    LaunchedEffect(timeframe) {
        while (true) {
            remaining = timeframe.remainingMillis()
            delay(1_000)
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = B1)) {
        Column(Modifier.padding(9.dp)) {
            Text("فريم الدخول المحدد", color = BT, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Text("${timeframe.shortLabel} • ${timeframe.arabicLabel}", color = BC, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text("بداية الشمعة التالية خلال ${formatTimeframeRemaining(remaining)}", color = BG, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("الفريم مشترك بين A وB وC وD وجميع الواجهات المستقبلية.", color = BM, fontSize = 8.sp)
        }
    }
}

@Composable
private fun StrategyEditor(
    strategy: AmarSavedStrategy?,
    strategyNumber: Int,
    onSave: (AmarSavedStrategy) -> Unit,
    onDelete: (Int) -> Unit,
    onApply: (AmarBot1RemoteSettings?) -> Unit
) {
    var lot by remember(strategy?.number, strategy?.profile?.lot) { mutableStateOf((strategy?.profile?.lot ?: 0.0).toFloat()) }
    var multiplier by remember(strategy?.number, strategy?.profile?.multiplier) { mutableStateOf((strategy?.profile?.multiplier ?: 0.0).toFloat()) }
    var gridStep by remember(strategy?.number, strategy?.profile?.gridStep) { mutableStateOf((strategy?.profile?.gridStep ?: 0.0).toFloat()) }
    var maxOrders by remember(strategy?.number, strategy?.profile?.maxOrders) { mutableStateOf((strategy?.profile?.maxOrders ?: 0).toFloat()) }
    var basketTp by remember(strategy?.number, strategy?.profile?.basketTp) { mutableStateOf((strategy?.profile?.basketTp ?: 0.0).toFloat()) }
    var basketSl by remember(strategy?.number, strategy?.profile?.basketSl) { mutableStateOf((strategy?.profile?.basketSl ?: 0.0).toFloat()) }
    var buyEnabled by remember(strategy?.number, strategy?.profile?.buyEnabled) { mutableStateOf(strategy?.profile?.buyEnabled ?: false) }
    var sellEnabled by remember(strategy?.number, strategy?.profile?.sellEnabled) { mutableStateOf(strategy?.profile?.sellEnabled ?: false) }

    fun remoteSettings(): AmarBot1RemoteSettings? = runCatching {
        AmarBot1RemoteSettings(
            lotStart = lot.toDouble(),
            gridStep = gridStep.toDouble(),
            maxOrders = maxOrders.toInt().coerceAtLeast(0),
            martingale = multiplier.toDouble(),
            basketTp = basketTp.toDouble(),
            basketSl = basketSl.toDouble(),
            trailing = 0.0,
            buyEnabled = buyEnabled,
            sellEnabled = sellEnabled,
        )
    }.getOrNull()

    Card(colors = CardDefaults.cardColors(containerColor = B1)) {
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("الاستراتيجية ${strategyNumber.toString().padStart(2, '0')} • تحكم باللمس", color = BT, fontWeight = FontWeight.Black, fontSize = 12.sp)
            ValueSlider("اللوت", lot, 0f..5f, 0.01f) { lot = it }
            ValueSlider("مضاعف الشبكة", multiplier, 0f..5f, 0.05f) { multiplier = it }
            ValueSlider("مسافة الشبكة", gridStep, 0f..500f, 1f) { gridStep = it }
            ValueSlider("عدد الأوامر", maxOrders, 0f..50f, 1f) { maxOrders = it }
            ValueSlider("هدف السلة \\$", basketTp, 0f..1000f, 1f) { basketTp = it }
            ValueSlider("وقف السلة \\$", basketSl, -1000f..0f, 1f) { basketSl = it }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ToggleButton("BUY", buyEnabled, Modifier.weight(1f)) { buyEnabled = !buyEnabled }
                ToggleButton("SELL", sellEnabled, Modifier.weight(1f)) { sellEnabled = !sellEnabled }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Button(onClick = { onApply(remoteSettings()) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = BC, contentColor = Color.Black)) { Text("تطبيق + تحقق") }
                Button(
                    onClick = {
                        onSave(
                            AmarSavedStrategy(
                                number = strategyNumber,
                                name = "واجهة B • استراتيجية ${strategyNumber.toString().padStart(2, '0')}",
                                profile = AmarBot1RuntimeConfig(
                                    lot = lot.toDouble(),
                                    gridStep = gridStep.toDouble(),
                                    maxOrders = maxOrders.toInt().coerceAtLeast(0),
                                    multiplier = multiplier.toDouble(),
                                    basketTp = basketTp.toDouble(),
                                    basketSl = basketSl.toDouble(),
                                    trailing = 0.0,
                                    buyEnabled = buyEnabled,
                                    sellEnabled = sellEnabled
                                )
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BG, contentColor = Color.Black)
                ) { Text("حفظ") }
                Button(onClick = { onDelete(strategyNumber) }, colors = ButtonDefaults.buttonColors(containerColor = BR, contentColor = Color.White)) { Text("حذف") }
            }
        }
    }
}

@Composable
private fun ValueSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, step: Float, onValueChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, color = BT, fontSize = 8.sp, modifier = Modifier.weight(1f))
            Text(
                if (value < 10f) String.format(Locale.US, "%.2f", value) else String.format(Locale.US, "%.0f", value),
                color = BC,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = { raw ->
                val stepped = round(raw / step) * step
                onValueChange(stepped.coerceIn(range.start, range.endInclusive))
            },
            valueRange = range
        )
    }
}

@Composable
private fun ToggleButton(label: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color by animateColorAsState(targetValue = if (enabled) BG else BR, animationSpec = tween(180), label = "toggle-$label")
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.Black)
    ) { Text(if (enabled) "● $label ON" else "○ $label OFF", fontSize = 9.sp, fontWeight = FontWeight.Black) }
}

private data class UiCommand(val label: String, val type: AmarBot1RemoteCommandType)

@Composable
private fun QuickCommands(symbol: String, onCommand: (UiCommand) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = B1)) {
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("أوامر سريعة • ${symbol.ifBlank { "لا يوجد رمز" }}", color = BT, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                CommandButton("إغلاق الشراء", BR, Modifier.weight(1f)) { onCommand(UiCommand("إغلاق الشراء", AmarBot1RemoteCommandType.CLOSE_BUY)) }
                CommandButton("إغلاق البيع", BP, Modifier.weight(1f)) { onCommand(UiCommand("إغلاق البيع", AmarBot1RemoteCommandType.CLOSE_SELL)) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                CommandButton("إغلاق الكل", BR, Modifier.weight(1f)) { onCommand(UiCommand("إغلاق الكل", AmarBot1RemoteCommandType.CLOSE_ALL)) }
                CommandButton("إعادة البناء", BC, Modifier.weight(1f)) { onCommand(UiCommand("إعادة البناء", AmarBot1RemoteCommandType.REBUILD)) }
            }
        }
    }
}

@Composable
private fun CommandButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if (color == BC) Color.Black else Color.White)
    ) { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black) }
}
