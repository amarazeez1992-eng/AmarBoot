package com.personal.gridbot.amaros.bots

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

private fun cardColors(container: Color) = CardDefaults.cardColors(containerColor = container)

@Composable
fun AmarBotLabInterfaceBScreen(
    onBackHome: () -> Unit,
    selectedBot: Int,
    onBotSelected: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember(context) { AmarBotVaultRepository(context) }
    var bots by remember { mutableStateOf(repo.load()) }
    var selectedStrategy by remember { mutableStateOf(1) }
    var notice by remember { mutableStateOf("") }
    val strategy = bots
        .firstOrNull { it.botNumber == selectedBot }
        ?.strategies
        ?.firstOrNull { it.number == selectedStrategy }
    val tf = AmarTradingTimeframeContext.selected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(B0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(B1)
                .padding(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BP,
                    contentColor = Color.White
                )
            ) {
                Text("⌂")
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    "AMAR • INTERFACE B",
                    color = BC,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "V$selectedBot • مختبر الهاتف",
                    color = BM,
                    fontSize = 9.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .background(BG, RoundedCornerShape(50))
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(colors = cardColors(B1)) {
                    Column(Modifier.padding(9.dp)) {
                        Text(
                            "البوتات",
                            color = BT,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            (1..10).forEach { n ->
                                Chip(
                                    text = "V$n",
                                    selected = n == selectedBot,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onBotSelected(n) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(colors = cardColors(B1)) {
                    Column(Modifier.padding(9.dp)) {
                        Text(
                            "الاستراتيجيات",
                            color = BT,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            (1..10).forEach { n ->
                                Chip(
                                    text = n.toString().padStart(2, '0'),
                                    selected = n == selectedStrategy,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedStrategy = n }
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

            item { TimeframeStatus(tf) }

            item {
                Editor(
                    strategy = strategy,
                    strategyNumber = selectedStrategy,
                    save = { saved ->
                        repo.saveStrategy(selectedBot, saved)
                        bots = repo.load()
                        notice = "✓ تم حفظ الاستراتيجية $selectedStrategy"
                    },
                    delete = { number ->
                        repo.deleteStrategy(selectedBot, number)
                        bots = repo.load()
                        notice = "تم حذف الاستراتيجية $number"
                    },
                    apply = {
                        notice = "✓ تم تطبيق الإعدادات على واجهة B"
                    }
                )
            }

            item { Commands { notice = it } }

            if (notice.isNotBlank()) {
                item {
                    Card(colors = cardColors(B2)) {
                        Text(
                            text = notice,
                            color = BT,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(9.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeframeStatus(tf: AmarTimeframe) {
    var remaining by remember(tf) { mutableStateOf(tf.remainingMillis()) }

    LaunchedEffect(tf) {
        while (true) {
            remaining = tf.remainingMillis()
            delay(1_000)
        }
    }

    Card(colors = cardColors(B1)) {
        Column(Modifier.padding(9.dp)) {
            Text(
                "فريم الدخول المحدد",
                color = BT,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
            Text(
                "${tf.shortLabel} • ${tf.arabicLabel}",
                color = BC,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "بداية الشمعة التالية خلال ${formatTimeframeRemaining(remaining)}",
                color = BG,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "غيّر الفريم من الشريط العام أعلى المختبر — ويطبق على جميع الواجهات.",
                color = BM,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun Editor(
    strategy: AmarSavedStrategy?,
    strategyNumber: Int,
    save: (AmarSavedStrategy) -> Unit,
    delete: (Int) -> Unit,
    apply: () -> Unit
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

    Card(colors = cardColors(B1)) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                "الاستراتيجية ${strategyNumber.toString().padStart(2, '0')} • سحب بالإصبع",
                color = BT,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )

            Slide("اللوت", lot, 0.01f..5f, 0.01f) { lot = it }
            Slide("مضاعف الشبكة", multiplier, 0.5f..5f, 0.05f) { multiplier = it }
            Slide("مسافة الشبكة", gridStep, 0f..500f, 1f) { gridStep = it }
            Slide("عدد الأوامر", maxOrders, 1f..50f, 1f) { maxOrders = it }
            Slide("هدف السلة $", basketTp, 0f..1000f, 1f) { basketTp = it }
            Slide("وقف السلة $", basketSl, -1000f..0f, 1f) { basketSl = it }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Toggle("BUY", buyEnabled, Modifier.weight(1f)) {
                    buyEnabled = !buyEnabled
                }
                Toggle("SELL", sellEnabled, Modifier.weight(1f)) {
                    sellEnabled = !sellEnabled
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Button(
                    onClick = apply,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BC,
                        contentColor = Color.Black
                    )
                ) {
                    Text("تطبيق")
                }
                Button(
                    onClick = {
                        save(
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BG,
                        contentColor = Color.Black
                    )
                ) {
                    Text("حفظ")
                }
                Button(
                    onClick = { delete(strategyNumber) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BR,
                        contentColor = Color.White
                    )
                ) {
                    Text("حذف")
                }
            }
        }
    }
}

@Composable
private fun Slide(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    set: (Float) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                label,
                color = BT,
                fontSize = 8.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (value < 10f) {
                    String.format(Locale.US, "%.2f", value)
                } else {
                    String.format(Locale.US, "%.0f", value)
                },
                color = BC,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = {
                val stepped = round(it / step) * step
                set(stepped.coerceIn(range.start, range.endInclusive))
            },
            valueRange = range
        )
    }
}

@Composable
private fun Toggle(
    label: String,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (enabled) BG else BR,
        animationSpec = tween(180),
        label = "toggleColor"
    )
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.Black
        )
    ) {
        Text(
            if (enabled) "● $label ON" else "○ $label OFF",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun Commands(notice: (String) -> Unit) {
    Card(colors = cardColors(B1)) {
        Column(Modifier.padding(9.dp)) {
            Text(
                "أوامر سريعة",
                color = BT,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Cmd("إغلاق الشراء", BR, Modifier.weight(1f)) {
                    notice("طلب إغلاق الشراء — انتظار تأكيد Runtime")
                }
                Cmd("إغلاق البيع", BP, Modifier.weight(1f)) {
                    notice("طلب إغلاق البيع — انتظار تأكيد Runtime")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Cmd("إغلاق الكل", BR, Modifier.weight(1f)) {
                    notice("⚠ طلب إغلاق الكل — انتظار التحقق")
                }
                Cmd("إعادة البناء", BC, Modifier.weight(1f)) {
                    notice("طلب إعادة البناء — انتظار Runtime")
                }
            }
        }
    }
}

@Composable
private fun Cmd(
    label: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = if (color == BC) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(18.dp, 6.dp, 18.dp, 6.dp)
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun Chip(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .background(
                color = if (selected) BG else B2,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else BT,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black
        )
    }
}
