package com.personal.gridbot.amaros.bots

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/** Shared Bot-Lab drag/drop surface: all ten bots can be selected, reordered and opened for editing. */
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
    val initialOrder = remember(botNumbers) { orderStore.load(botNumbers) }
    var dragging by remember { mutableStateOf<Int?>(null) }
    var dragDistanceX by remember { mutableStateOf(0f) }
    var dragDistanceY by remember { mutableStateOf(0f) }
    var working by remember(initialOrder) { mutableStateOf(initialOrder) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("اسحب أي BOT وأسقطه فوق BOT آخر لترتيبه. عند الإسقاط يُحدد البوت فورًا وتظهر معلوماته في محرر التعديل.", color = Color(0xFF78A9B8), fontSize = 9.sp)
        working.chunked(5).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                row.forEach { number ->
                    val isDragging = dragging == number
                    val scale by animateFloatAsState(if (isDragging) 1.10f else 1f, tween(140), label = "bot-drag-scale")
                    Box(
                        Modifier.weight(1f).height(42.dp).scale(scale)
                            .background(if (selectedBot == number) Color(0xFF1DE5FF) else Color(0xFF0E2230), RoundedCornerShape(11.dp))
                            .border(1.dp, if (isDragging) Color(0xFFFFC84A) else if (selectedBot == number) Color(0xFF8DFAFF) else Color(0xFF1A3E4D), RoundedCornerShape(11.dp))
                            .clickable { onSelectBot(number) }
                            .pointerInput(number, working) {
                                detectDragGestures(
                                    onDragStart = {
                                        dragging = number
                                        dragDistanceX = 0f
                                        dragDistanceY = 0f
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragDistanceX += amount.x
                                        dragDistanceY += amount.y
                                    },
                                    onDragEnd = {
                                        val from = working.indexOf(number)
                                        if (from >= 0 && working.isNotEmpty()) {
                                            val columns = 5
                                            val cellWidth = size.width.toFloat().coerceAtLeast(1f)
                                            val cellHeight = 48.dp.toPx().coerceAtLeast(1f)
                                            val colShift = (dragDistanceX / cellWidth).roundToInt()
                                            val rowShift = (dragDistanceY / cellHeight).roundToInt()
                                            val target = (from + rowShift * columns + colShift).coerceIn(0, working.lastIndex)
                                            if (target != from) {
                                                val reordered = working.toMutableList().apply { add(target, removeAt(from)) }.toList()
                                                working = reordered
                                                orderStore.save(reordered)
                                                onReorder(reordered)
                                            }
                                            onSelectBot(number)
                                        }
                                        dragging = null
                                        dragDistanceX = 0f
                                        dragDistanceY = 0f
                                    },
                                    onDragCancel = {
                                        dragging = null
                                        dragDistanceX = 0f
                                        dragDistanceY = 0f
                                    }
                                )
                            }
                            .padding(horizontal = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("BOT $number", color = if (selectedBot == number) Color.Black else Color(0xFFE9FBFF), fontSize = 9.sp)
                    }
                }
                repeat(5 - row.size) { Box(Modifier.weight(1f).height(42.dp)) }
            }
        }
    }
}
