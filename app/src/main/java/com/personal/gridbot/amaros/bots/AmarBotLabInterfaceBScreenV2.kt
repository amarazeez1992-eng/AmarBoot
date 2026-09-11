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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.chart.AmarTimeframe
import java.util.Locale
import kotlin.math.round
import kotlinx.coroutines.delay

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
    var bots by remember { mutableStateOf(repo.load()) }
    var selectedStrategy by remember { mutableStateOf(1) }
    var notice by remember { mutableStateOf("") }
    val strategy = bots.firstOrNull { it.botNumber == selectedBot }
        ?.strategies?.firstOrNull { it.number == selectedStrategy }
    val timeframe = AmarTradingTimeframeContext.selected

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
                Text("V$selectedBot • مختبر الهاتف", color = BM, fontSize = 9.sp)
            }
            Box(Modifier.size(11.dp).background(BG, RoundedCornerShape(50)))
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { BotSelector(selectedBot, onBotSelected) }
            item {
                StrategySelector(selectedStrategy) { selectedStrategy = it }
            }
            item { TimeframeStatus(timeframe) }
            item {
                StrategyEditor(
                    strategy = strategy,
                    strategyNumber = selectedStrategy,
                    onSave = { saved ->
                        repo.saveStrategy(selectedBot, saved)
                        bots = repo.load()
                        notice = "✓ تم حفظ الاستراتيجية $selectedStrategy"
                    },
                    onDelete = { number ->
                        repo.deleteStrategy(selectedBot, number)
                        bots = repo.load()
                        notice = "تم حذف الاستراتيجية $number"
                    },
                    onApply = { notice = "✓ تم تطبيق الإعدادات على واجهة B" }
                )
            }
            item { QuickCommands { notice = it } }
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
private fun BotSelector(selectedBot: Int, onBotSelected: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = B1)) {
        Column(Modifier.padding(9.dp)) {
            Text("البوتات", color = BT, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                (1..10).forEach { bot ->
                    Chip(
                        text = "V$bot",
                        selected = bot == selectedBot,
                        modifier = Modifier.weight(1f),
                        onClick = { onBotSelected(bot) }
                    )
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
                    Chip(
                        text = number.toString().padStart(2, '0'),
                        selected = number == selectedStrategy,
                        modifier = Modifier.weight(1f),
                        onClick = { onStrategySelected(number) }
                    )
                }
            }
            Text(
                "الاستراتيجية ${selectedStrategy.toString().padStart(2, '0')}",
                color = BC,
                fontSize = 10.sp
            )
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
            Text(
                "بداية الشمعة التالية خلال ${formatTimeframeRemaining(remaining)}",
                color = BG,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "الفريم مشترك بين A وB وC وD وجميع الواجهات المستقبلية.",
                color = BM,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun StrategyEditor(
    strategy: AmarSavedStrategy?,
    strategyNumber: Int,
    onSave: (AmarSavedStrategy) -> Unit,
    onDelete: (Int) -> Unit,
    onApply: () -> Unit
) {
    var lot by remember(strategy?.number, strategy?.profile?.lot) {
        mutableStateOf((strategy?.profile?.lot ?: 0.01).toFloat())
    }
    var multiplier by remember(strategy?.number, strategy?.profile?.multiplier) {
        mutableStateOf((strategy?.profile?.multiplier ?: 2.0).toFloat())
    }
    var gridStep by remember(strategy?.number, strategy?.profile?.gridStep) {
        mutableStateOf((strategy?.profile?.gridStep ?: 30.0).toFloat())
    }
    var maxOrders by remember(strategy?.number, strategy?.profile?.maxOrders) {
        mutableStateOf((strategy?.profile?.maxOrders ?: 10).toFloat())
    }
    var basketTp by remember(strategy?.number, strategy?.profile?.basketTp) {
        mutableStateOf((strategy?.profile?.basketTp ?: 50.0).toFloat())
    }
    var basketSl by remember(strategy?.number, strategy?.profile?.basketSl) {
        mutableStateOf((strategy?.profile?.basketSl ?: -30.0).toFloat())
    }
    var buyEnabled by remember(strategy?.number, strategy?.profile?.buyEnabled) {
        mutableStateOf(strategy?.profile?.buyEnabled ?: true)
    }
    var sellEnabled by remember(strategy?.number, strategy?.profile?.sellEnabled) {
        mutableStateOf(strategy?.profile?.sellEnabled ?: true)
    }

    Card(colors = CardDefaults.cardColors(containerColor = B1)) {
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                "الاستراتيجية ${strategyNumber.toString().padStart(2, '0')} • تحكم باللمس",
                color = BT,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
            ValueSlider("اللوت", lot, 0.01f..5f, 0.01f) { lot = it }
            ValueSlider("مضاعف الشبكة", multiplier, 0.5f..5f, 0.05f) { multiplier = it }
            ValueSlider("مسافة الشبكة", gridStep, 0f..500f, 1f) { gridStep = it }
            ValueSlider("عدد الأوامر", maxOrders, 1f..50f, 1f) { maxOrders = it }
            ValueSlider("هدف السلة \$", basketTp, 0f..1000f, 1f) { basketTp = it }
            ValueSlider("وقف السلة \$", basketSl, -1000f..0f, 1f) { basketSl = it }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ToggleButton("BUY", buyEnabled, Modifier.weight(1f)) { buyEnabled = !buyEnabled }
                ToggleButton("SELL", sellEnabled, Modifier.weight(1f)) { sellEnabled = !sellEnabled }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BC, contentColor = Color.Black)
                ) { Text("تطبيق") }
                Button(
                    onClick = {
                        onSave(
                            AmarSavedStrategy(
                                number = strategyNumber,
                                name = "واجهة B • استراتيجية ${strategyNumber.toString().padStart(2, '0')}",
                                profile = AmarBot1RuntimeConfig(
                                    lot = lot.toDouble(),
                                    gridStep = gridStep.toDouble(),
                                    maxOrders = maxOrders.toInt().coerceAtLeast(1),
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
                Button(
                    onClick = { onDelete(strategyNumber) },
                    colors = ButtonDefaults.buttonColors(containerColor = BR, contentColor = Color.White)
                ) { Text("حذف") }
            }
        }
    }
}

@Composable
private fun ValueSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    onValueChange: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, color = BT, fontSize = 8.sp, modifier = Modifier.weight(1f))
            Text(
                if (value < 10f) String.format(Locale.US, "%.2f", value)
                else String.format(Locale.US, "%.0f", value),
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
    val color by animateColorAsState(
        targetValue = if (enabled) BG else BR,
        animationSpec = tween(180),
        label = "toggle-$label"
    )
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.Black)
    ) {
        Text(if (enabled) "● $label ON" else "○ $label OFF", fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun QuickCommands(notice: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = B1)) {
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("أوامر سريعة", color = BT, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                CommandButton("إغلاق الشراء", BR, Modifier.weight(1f)) {
                    notice("طلب إغلاق الشراء — انتظار تأكيد Runtime")
                }
                CommandButton("إغلاق البيع", BP, Modifier.weight(1f)) {
                    notice("طلب إغلاق البيع — انتظار تأكيد Runtime")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                CommandButton("إغلاق الكل", BR, Modifier.weight(1f)) {
                    notice("⚠ طلب إغلاق الكل — انتظار التحقق")
                }
                CommandButton("إعادة البناء", BC, Modifier.weight(1f)) {
                    notice("طلب إعادة البناء — انتظار Runtime")
                }
            }
        }
    }
}

@Composable
private fun CommandButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = if (color == BC) Color.Black else Color.White
        )
    ) { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black) }
}
