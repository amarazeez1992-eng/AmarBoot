package com.personal.gridbot.amaros.bots

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/** UI-only numeric drag control. It changes presentation/input only; the bot contract is untouched. */
@Composable
fun AmarDragValueControl(
    label: String,
    value: Double,
    min: Double,
    max: Double,
    step: Double,
    accent: Color,
    format: String,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    var dragging by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "drag-glow-$label")
    val pulse by transition.animateFloat(.42f, .92f, infiniteRepeatable(tween(1250, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val glow = if (dragging) 1f else pulse
    val activeColor by animateColorAsState(if (dragging) Color.White else accent, tween(120), label = "drag-color")
    val safeMax = if (max > min) max else min + 1.0

    fun quantize(raw: Double): Double {
        val s = if (step > 0.0) step else 0.0001
        val q = ((raw - min) / s).roundToInt() * s + min
        return q.coerceIn(min, safeMax)
    }
    fun fromX(x: Float): Double {
        val fraction = (x / widthPx).coerceIn(0f, 1f)
        return quantize(min + (safeMax - min) * fraction)
    }
    val fraction = ((value.coerceIn(min, safeMax) - min) / (safeMax - min)).toFloat()

    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, color = Color(0xFF7896A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("اسحب للمستوى المطلوب ثم اترك إصبعك", color = Color(0xFF536B78), fontSize = 7.sp)
            }
            Text(String.format(java.util.Locale.US, format, value), color = activeColor, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        Box(
            Modifier.fillMaxWidth().height(34.dp)
                .onGloballyPositioned { widthPx = it.size.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(min, safeMax, step) {
                    detectDragGestures(
                        onDragStart = { position -> dragging = true; onValueChange(fromX(position.x)) },
                        onDrag = { change, _ -> change.consume(); onValueChange(fromX(change.position.x)) },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false }
                    )
                }
                .background(Color(0xFF081722), RoundedCornerShape(17.dp))
                .border(1.dp, activeColor.copy(alpha = if (dragging) 1f else .45f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(7.dp).background(accent.copy(alpha = .32f + .38f * glow), RoundedCornerShape(7.dp)))
            Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(30.dp), contentAlignment = Alignment.CenterEnd) {
                Box(
                    Modifier.size(if (dragging) 24.dp else 20.dp)
                        .shadow(if (dragging) 16.dp else 7.dp, CircleShape, ambientColor = accent, spotColor = accent)
                        .background(activeColor, CircleShape)
                        .border(2.dp, accent, CircleShape)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(String.format(java.util.Locale.US, format, min), color = Color(0xFF536B78), fontSize = 7.sp)
            Text(String.format(java.util.Locale.US, format, safeMax), color = Color(0xFF536B78), fontSize = 7.sp)
        }
    }
}
