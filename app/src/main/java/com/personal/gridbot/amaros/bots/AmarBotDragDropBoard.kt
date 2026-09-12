package com.personal.gridbot.amaros.bots

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.runtime.AmarBotOperationalEngine

/**
 * Real Bot-Lab ordering surface.
 * Ten stable BOT identities are always rendered. Reordering changes only the
 * visual order; runtime statistics come from the operational database.
 */
@Composable
fun AmarBotDragDropBoard(
    botNumbers: List<Int>,
    selectedBot: Int,
    onSelectBot: (Int) -> Unit,
    onReorder: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orderStore = remember(context) { AmarBotOrderStore(context) }
    val runtimeEngine = remember(context) { AmarBotOperationalEngine(context) }
    val runtimeRows by runtimeEngine.observeBots().collectAsState(initial = emptyList())
    val allowedBots = remember(botNumbers) { (1..10).filter(botNumbers::contains).ifEmpty { (1..10).toList() } }
    val initialOrder = remember(allowedBots) { orderStore.load(allowedBots) }
    var working by remember(initialOrder) { mutableStateOf(initialOrder) }
    var dragging by remember { mutableStateOf<Int?>(null) }
    var lastPointerRoot by remember { mutableStateOf<Offset?>(null) }
    val bounds = remember { mutableStateMapOf<Int, Rect>() }

    LaunchedEffect(allowedBots) { runtimeEngine.ensureBotCatalog() }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "10 بوتات مستقلة • اسحب البطاقة نفسها إلى بطاقة أخرى لإعادة الترتيب",
            color = Color(0xFF8FEFFF),
            fontSize = 9.sp
        )

        working.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { number ->
                    val runtime = runtimeRows.firstOrNull { it.botNumber == number }
                    val isDragging = dragging == number
                    val isSelected = selectedBot == number
                    val pointer = lastPointerRoot
                    val dropTarget = dragging != null && dragging != number && pointer != null && bounds[number]?.contains(pointer) == true
                    val transition = rememberInfiniteTransition(label = "bot-card-$number")
                    val glow by transition.animateFloat(.55f, 1f, infiniteRepeatable(tween(1500 + number * 45), RepeatMode.Reverse), label = "bot-glow-$number")
                    val scale by androidx.compose.animation.core.animateFloatAsState(
                        when {
                            isDragging -> 1.055f
                            dropTarget -> 1.035f
                            else -> 1f
                        }, tween(140), label = "bot-drag-scale-$number"
                    )
                    val borderColor = when {
                        isDragging -> Color(0xFFFFC84A)
                        dropTarget -> Color(0xFF00F0A8)
                        isSelected -> Color(0xFF8DFAFF)
                        else -> Color(0xFF1A3E4D)
                    }
                    val background = when {
                        isDragging -> Color(0xFF17394A)
                        dropTarget -> Color(0xFF123B35)
                        isSelected -> Color(0xFF163B49)
                        else -> Color(0xFF0E2230)
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(86.dp)
                            .scale(scale)
                            .onGloballyPositioned { bounds[number] = it.boundsInRoot() }
                            .background(background, RoundedCornerShape(14.dp))
                            .border(1.5.dp, borderColor.copy(alpha = if (isSelected || isDragging || dropTarget) 1f else glow), RoundedCornerShape(14.dp))
                            .clickable { onSelectBot(number) }
                            .pointerInput(number, working) {
                                detectDragGestures(
                                    onDragStart = { position ->
                                        dragging = number
                                        lastPointerRoot = bounds[number]?.let { it.topLeft + position }
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        lastPointerRoot = bounds[number]?.let { it.topLeft + change.position }
                                    },
                                    onDragEnd = {
                                        val dragged = dragging
                                        val pointerAtEnd = lastPointerRoot
                                        if (dragged != null && pointerAtEnd != null) {
                                            val targetNumber = working.firstOrNull { candidate -> candidate != dragged && bounds[candidate]?.contains(pointerAtEnd) == true }
                                            if (targetNumber != null) {
                                                val reordered = AmarBotDragDropOrder.move(working, dragged, targetNumber)
                                                if (reordered != working) {
                                                    working = reordered
                                                    orderStore.save(reordered)
                                                    onReorder(reordered)
                                                }
                                            }
                                            onSelectBot(dragged)
                                        }
                                        dragging = null
                                        lastPointerRoot = null
                                    },
                                    onDragCancel = {
                                        dragging = null
                                        lastPointerRoot = null
                                    }
                                )
                            }
                            .padding(7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("BOT ${number.toString().padStart(2, '0')}", color = if (isSelected) Color(0xFF8DFAFF) else Color(0xFFE9FBFF), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                Box(Modifier.size(7.dp).background(if (runtime?.status == "ACTIVE") Color(0xFF00E6A0) else if (runtime?.status == "ARMED") Color(0xFFFFC84A) else Color(0xFF536B78), CircleShape))
                            }
                            Text(runtime?.name ?: "بوت $number", color = Color(0xFF7896A5), fontSize = 8.sp, maxLines = 1)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MiniStat("است", "—")
                                MiniStat("صفقات", "${runtime?.openPositions ?: 0}")
                                MiniStat("أوامر", "${runtime?.pendingOrders ?: 0}")
                                MiniStat("لوت", String.format(java.util.Locale.US, "%.2f", runtime?.totalLots ?: 0.0))
                            }
                        }
                    }
                }
                if (row.size < 2) Box(Modifier.weight(1f).height(86.dp))
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color(0xFFE9FBFF), fontSize = 9.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color(0xFF7896A5), fontSize = 6.sp)
    }
}
