package com.personal.gridbot.amaros.bots

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Independent drag/drop ordering surface for Bot-Lab settings. UI ordering only; it never executes trades. */
@Composable
fun AmarBotOrderSettingsPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { AmarBotOrderSettingsStore(context) }
    val defaults = remember { listOf("اتجاه الأوامر", "اللوت", "مسافة الشبكة", "الحد الأقصى", "مضاعف اللوت", "هدف السلة", "خسارة السلة", "التتبع") }
    var order by remember { mutableStateOf(store.load(defaults)) }
    var dragging by remember { mutableStateOf<String?>(null) }
    var offsetY by remember { mutableStateOf(0f) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("ترتيب إعدادات البوت — اسحب العنصر وأسقطه فوق عنصر آخر", color = Color(0xFF8FEFFF), fontSize = 10.sp)
        order.forEachIndexed { index, item ->
            val active = dragging == item
            val scale by animateFloatAsState(if (active) 1.035f else 1f, tween(120), label = "order-scale")
            val accent = listOf(Color(0xFF19E6FF), Color(0xFF00F0A8), Color(0xFF4D7CFF), Color(0xFFB14DFF), Color(0xFFFF4FA3), Color(0xFFFFC84D), Color(0xFFFF5364), Color(0xFF62D9FF))[index % 8]
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .scale(scale)
                    .background(Brush.linearGradient(listOf(Color(0xFF102A37), Color(0xFF0B1824))), RoundedCornerShape(13.dp))
                    .border(1.dp, if (active) accent else accent.copy(alpha = .35f), RoundedCornerShape(13.dp))
                    .pointerInput(item, order) {
                        detectDragGestures(
                            onDragStart = { dragging = item; offsetY = 0f },
                            onDrag = { change, amount ->
                                change.consume()
                                offsetY += amount.y
                            },
                            onDragEnd = {
                                val from = order.indexOf(item)
                                if (from >= 0) {
                                    val shift = (offsetY / 49.dp.toPx()).toInt()
                                    val target = (from + shift).coerceIn(0, order.lastIndex)
                                    if (target != from) {
                                        order = order.toMutableList().apply { add(target, removeAt(from)) }
                                        store.save(order)
                                    }
                                }
                                dragging = null
                                offsetY = 0f
                            },
                            onDragCancel = { dragging = null; offsetY = 0f }
                        )
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("☷", color = accent, fontSize = 20.sp)
                Text("${index + 1}", color = Color(0xFF7897A5), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp))
                Text(item, color = Color(0xFFE9FBFF), fontSize = 11.sp)
            }
        }
    }
}

private class AmarBotOrderSettingsStore(private val context: android.content.Context) {
    private val prefs = context.getSharedPreferences("amar_bot_order_settings_v1", android.content.Context.MODE_PRIVATE)
    fun load(defaults: List<String>): List<String> {
        val saved = prefs.getString("order", null)?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
        return saved.filter { it in defaults }.distinct() + defaults.filter { it !in saved }
    }
    fun save(order: List<String>) {
        prefs.edit().putString("order", order.joinToString("|")).apply()
    }
}
