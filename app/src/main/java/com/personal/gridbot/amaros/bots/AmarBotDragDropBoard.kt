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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared Bot-Lab drag/drop surface.
 * Drop detection is target-based: the final pointer position is matched against
 * the actual rendered BOT card bounds, rather than estimating a row/column shift.
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
    val initialOrder = remember(botNumbers) { orderStore.load(botNumbers) }
    var working by remember(initialOrder) { mutableStateOf(initialOrder) }
    var dragging by remember { mutableStateOf<Int?>(null) }
    var lastPointerRoot by remember { mutableStateOf<Offset?>(null) }
    val bounds = remember { mutableStateMapOf<Int, Rect>() }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "اسحب BOT وأسقطه مباشرة فوق BOT آخر. سيُحفظ الترتيب ويُفتح البوت المسقَط للتحرير.",
            color = Color(0xFF8FEFFF),
            fontSize = 9.sp
        )

        working.chunked(5).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                row.forEach { number ->
                    val isDragging = dragging == number
                    val isSelected = selectedBot == number
                    val dropTarget = dragging != null && dragging != number && bounds[number]?.contains(lastPointerRoot ?: Offset.Unspecified) == true
                    val scale by animateFloatAsState(
                        targetValue = when {
                            isDragging -> 1.10f
                            dropTarget -> 1.045f
                            else -> 1f
                        },
                        animationSpec = tween(140),
                        label = "bot-drag-scale"
                    )
                    val borderColor = when {
                        isDragging -> Color(0xFFFFC84A)
                        dropTarget -> Color(0xFF00F0A8)
                        isSelected -> Color(0xFF8DFAFF)
                        else -> Color(0xFF1A3E4D)
                    }

                    Box(
                        Modifier
                            .weight(1f)
                            .height(42.dp)
                            .scale(scale)
                            .onGloballyPositioned { coordinates -> bounds[number] = coordinates.boundsInRoot() }
                            .background(
                                when {
                                    isDragging -> Color(0xFF17394A)
                                    dropTarget -> Color(0xFF123B35)
                                    isSelected -> Color(0xFF1DE5FF)
                                    else -> Color(0xFF0E2230)
                                },
                                RoundedCornerShape(11.dp)
                            )
                            .border(1.5.dp, borderColor, RoundedCornerShape(11.dp))
                            .clickable { onSelectBot(number) }
                            .pointerInput(number, working) {
                                detectDragGestures(
                                    onDragStart = { position ->
                                        dragging = number
                                        lastPointerRoot = bounds[number]?.let { it.topLeft + position }
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        val cardBounds = bounds[number]
                                        lastPointerRoot = cardBounds?.let { it.topLeft + change.position }
                                    },
                                    onDragEnd = {
                                        val dragged = dragging
                                        val pointer = lastPointerRoot
                                        if (dragged != null && pointer != null) {
                                            val from = working.indexOf(dragged)
                                            val targetNumber = working.firstOrNull { candidate ->
                                                candidate != dragged && bounds[candidate]?.contains(pointer) == true
                                            }
                                            val target = targetNumber?.let { working.indexOf(it) }
                                            if (from >= 0 && target != null && target >= 0 && target != from) {
                                                val reordered = working.toMutableList().apply {
                                                    add(target, removeAt(from))
                                                }.toList()
                                                working = reordered
                                                orderStore.save(reordered)
                                                onReorder(reordered)
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
                            .padding(horizontal = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "BOT $number",
                            color = if (isSelected) Color.Black else Color(0xFFE9FBFF),
                            fontSize = 9.sp
                        )
                    }
                }
                repeat(5 - row.size) { Box(Modifier.weight(1f).height(42.dp)) }
            }
        }
    }
}
