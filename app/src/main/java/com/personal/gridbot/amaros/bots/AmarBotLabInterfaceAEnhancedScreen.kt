package com.personal.gridbot.amaros.bots

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private val A0 = Color(0xFF03070D)
private val A1 = Color(0xFF081521)
private val A2 = Color(0xFF0D2330)
private val AC = Color(0xFF24E8FF)
private val AG = Color(0xFF19F2A6)
private val AR = Color(0xFFFF4F72)
private val AT = Color(0xFFEAFBFF)
private val AM = Color(0xFF8AA9B9)

private enum class TileType(val title: String) {
    SETTINGS("الإعدادات"), GRID("الشبكة"), COMMANDS("الأوامر"), ADDONS("الإضافات")
}

@Composable
fun AmarBotLabInterfaceAEnhancedScreen(onBackHome: () -> Unit) {
    val selectedBot = AmarBotLabSelectionContext.selectedBot
    var order by remember { mutableStateOf(TileType.entries.toList()) }
    var dragging by remember { mutableStateOf<TileType?>(null) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var selectedTile by remember { mutableStateOf(TileType.SETTINGS) }
    var lot by remember { mutableFloatStateOf(0.01f) }
    var grid by remember { mutableFloatStateOf(30f) }
    var maxOrders by remember { mutableFloatStateOf(10f) }
    var multiplier by remember { mutableFloatStateOf(2f) }

    Column(Modifier.fillMaxSize().background(A0)) {
        Header(selectedBot, onBackHome)
        VirtualClock()
        Text(
            "اسحب الأقسام لترتيب مختبر البوت • اضغط القسم لفتح إعداداته",
            color = AM,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(order, key = { _, item -> item.name }) { index, tile ->
                val active = tile == selectedTile
                DragTile(
                    tile = tile,
                    active = active,
                    dragging = dragging == tile,
                    offsetY = if (dragging == tile) offsetY else 0f,
                    onSelect = { selectedTile = tile },
                    onDragStart = { dragging = tile; offsetY = 0f },
                    onDrag = { dy ->
                        if (dragging == tile) {
                            offsetY += dy
                            val targetIndex = (index + (offsetY / 76f).roundToInt()).coerceIn(0, order.lastIndex)
                            if (targetIndex != index) {
                                val mutable = order.toMutableList()
                                val moved = mutable.removeAt(index)
                                mutable.add(targetIndex, moved)
                                order = mutable
                                offsetY = 0f
                            }
                        }
                    },
                    onDragEnd = { dragging = null; offsetY = 0f }
                )
            }

            item {
                when (selectedTile) {
                    TileType.SETTINGS -> SettingsPanel(lot, multiplier, { lot = it }, { multiplier = it })
                    TileType.GRID -> GridPanel(grid, maxOrders, { grid = it }, { maxOrders = it })
                    TileType.COMMANDS -> CommandsPanel()
                    TileType.ADDONS -> AddonsPanel()
                }
            }
        }
    }
}

@Composable
private fun Header(bot: Int, onBackHome: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(A1).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBackHome,
            colors = ButtonDefaults.buttonColors(containerColor = AC, contentColor = Color.Black),
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 5.dp)
        ) { Text("⌂", fontWeight = FontWeight.Black) }
        Column(Modifier.weight(1f).padding(horizontal = 9.dp)) {
            Text("AMAR BOT LAB", color = AC, fontSize = 19.sp, fontWeight = FontWeight.Black)
            Text("واجهة 1 • BOT $bot • نظام تفاعلي", color = AM, fontSize = 9.sp)
        }
        Box(Modifier.size(12.dp).clip(CircleShape).background(AG))
    }
}

@Composable
private fun VirtualClock() {
    var time by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = LocalTime.now()
            delay(1000)
        }
    }
    val secondAngle = time.second * 6f
    val minuteAngle = (time.minute + time.second / 60f) * 6f
    val hourAngle = ((time.hour % 12) + time.minute / 60f) * 30f
    val accent by animateColorAsState(
        if (time.second % 2 == 0) AC else AG,
        tween(450),
        label = "clock-accent"
    )

    Box(
        Modifier.fillMaxWidth().padding(top = 7.dp, bottom = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(104.dp).clip(CircleShape).background(A1),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(96.dp).clip(CircleShape).background(A2), contentAlignment = Alignment.Center) {
                ClockHand(hourAngle, 25.dp, 4.dp, AT)
                ClockHand(minuteAngle, 34.dp, 3.dp, accent)
                ClockHand(secondAngle, 40.dp, 1.dp, AR)
                Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
                Text(
                    String.format("%02d:%02d", time.hour, time.minute),
                    color = AT,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset(y = 27.dp)
                )
            }
        }
    }
}

@Composable
private fun ClockHand(angle: Float, length: androidx.compose.ui.unit.Dp, width: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        Modifier.size(width, length)
            .offset(y = (-length.value / 2f).dp)
            .background(color, RoundedCornerShape(50))
    )
}

@Composable
private fun DragTile(
    tile: TileType,
    active: Boolean,
    dragging: Boolean,
    offsetY: Float,
    onSelect: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val color by animateColorAsState(if (active) AC else A2, tween(220), label = "tile-color")
    val textColor = if (active) Color.Black else AT
    Box(
        Modifier.fillMaxWidth()
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .background(color, RoundedCornerShape(14.dp))
            .pointerInput(tile) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, dragAmount -> change.consume(); onDrag(dragAmount.y) },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd
                )
            }
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⠿", color = if (active) Color.Black else AC, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(tile.title, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(if (dragging) "جارٍ السحب… حرّك القسم إلى الموضع المطلوب" else "اضغط للفتح • اضغط مطولًا ثم اسحب للإفلات", color = if (active) Color.Black else AM, fontSize = 8.sp)
            }
            Text(if (dragging) "↕" else "⋮⋮", color = if (active) Color.Black else AC, fontSize = 18.sp)
        }
    }
    if (active) {
        Spacer(Modifier.height(1.dp))
    }
}

@Composable
private fun SettingsPanel(lot: Float, multiplier: Float, onLot: (Float) -> Unit, onMultiplier: (Float) -> Unit) {
    DynamicPanel("إعدادات البوت", "القيمة واللون يتحركان مع السحب") {
        DynamicSlider("اللوت", lot, 0.01f..1f, 0.01f, onLot)
        DynamicSlider("مضاعف اللوت", multiplier, 0.1f..5f, 0.1f, onMultiplier)
    }
}

@Composable
private fun GridPanel(grid: Float, maxOrders: Float, onGrid: (Float) -> Unit, onMax: (Float) -> Unit) {
    DynamicPanel("الشبكة", "تحكم مباشر وتفاعلي") {
        DynamicSlider("مسافة الشبكة", grid, 1f..500f, 1f, onGrid)
        DynamicSlider("عدد الأوامر", maxOrders, 1f..50f, 1f, onMax)
    }
}

@Composable
private fun DynamicSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, step: Float, onValue: (Float) -> Unit) {
    val fraction = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    val color by animateColorAsState(
        when {
            fraction < .35f -> AC
            fraction < .70f -> Color(0xFFB77CFF)
            else -> AG
        }, tween(180), label = "value-color"
    )
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = AT, fontSize = 9.sp, modifier = Modifier.weight(1f))
            Text(String.format("%.2f", value), color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
        Slider(value, { raw -> onValue((raw / step).roundToInt() * step) }, valueRange = range)
    }
}

@Composable
private fun CommandsPanel() = DynamicPanel("الأوامر", "أزرار جاهزة لقناة BOT1") {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Command("START", AG)
        Command("STOP", Color(0xFFFFC84A))
        Command("REBUILD", AC)
        Command("CLOSE", AR)
    }
}

@Composable
private fun AddonsPanel() = DynamicPanel("الإضافات", "مساحة توسعة مستقلة") {
    Text("الإضافات الجديدة تُضاف كوحدات مستقلة دون إعادة بناء مختبر البوتات.", color = AM, fontSize = 9.sp)
}

@Composable
private fun Command(label: String, color: Color) {
    Box(Modifier.weight(1f).height(42.dp).background(color, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
        Text(label, color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun DynamicPanel(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = A1), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = AC, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = AM, fontSize = 8.sp)
            content()
        }
    }
}
