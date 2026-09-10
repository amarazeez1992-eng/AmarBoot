package com.personal.gridbot.amaros.design

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium analog AMAR clock. Presentation-only: it never participates in trading decisions.
 * Black/white/gold palette, live second hand, subtle 3D tilt and interactive depth animation.
 */
@Composable
fun AmarAnalogClock(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val inspection = LocalInspectionMode.current

    LaunchedEffect(inspection) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(250L)
        }
    }

    val pulse = rememberInfiniteTransition(label = "amar-clock-pulse")
    val glow by pulse.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "clock-glow"
    )
    val tilt by pulse.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse),
        label = "clock-tilt"
    )

    val calendar = Calendar.getInstance().apply { timeInMillis = nowMs }
    val hour = calendar.get(Calendar.HOUR).toFloat() + calendar.get(Calendar.MINUTE) / 60f
    val minute = calendar.get(Calendar.MINUTE).toFloat() + calendar.get(Calendar.SECOND) / 60f
    val second = calendar.get(Calendar.SECOND).toFloat() + calendar.get(Calendar.MILLISECOND) / 1000f

    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .graphicsLayer {
                rotationX = 2.2f
                rotationY = tilt
                cameraDistance = 22f
                shadowElevation = 20f * glow
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF343434), Color(0xFF090909), Color.Black)
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(142.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension * 0.46f

            // Deep outer case and metallic gold bezel.
            drawCircle(Color.Black, radius = radius * 1.08f)
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(Color(0xFF6B4A12), Color(0xFFFFE7A0), Color(0xFF9B711D), Color(0xFFFFD66B), Color(0xFF6B4A12))
                ),
                radius = radius * 1.045f
            )
            drawCircle(Color(0xFF0B0B0B), radius = radius * 0.985f)
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFF202020), Color.Black)),
                radius = radius * 0.94f
            )

            // Hour marks.
            for (i in 0 until 60) {
                val angle = Math.toRadians(i * 6.0 - 90.0)
                val major = i % 5 == 0
                val outer = radius * 0.87f
                val inner = if (major) radius * 0.76f else radius * 0.82f
                val x1 = cx + cos(angle).toFloat() * inner
                val y1 = cy + sin(angle).toFloat() * inner
                val x2 = cx + cos(angle).toFloat() * outer
                val y2 = cy + sin(angle).toFloat() * outer
                drawLine(
                    color = if (major) Color(0xFFFFE7A0) else Color.White.copy(alpha = 0.55f),
                    start = androidx.compose.ui.geometry.Offset(x1, y1),
                    end = androidx.compose.ui.geometry.Offset(x2, y2),
                    strokeWidth = if (major) 2.7f else 1.0f,
                    cap = StrokeCap.Round
                )
            }

            // Hands: smooth sweep second hand, classic white/gold hour/minute hands.
            drawHand(cx, cy, radius * 0.52f, hour / 12f * 360f, Color.White, 5.5f)
            drawHand(cx, cy, radius * 0.69f, minute / 60f * 360f, Color.White, 3.8f)
            drawHand(cx, cy, radius * 0.76f, second / 60f * 360f, Color(0xFFFFD66B), 1.8f)
            drawCircle(Color(0xFFFFD66B), radius = 5.0f)
            drawCircle(Color.Black, radius = 2.0f)

            // Small AMAR signature plate.
            val plate = Path().apply {
                moveTo(cx - radius * 0.19f, cy + radius * 0.34f)
                lineTo(cx + radius * 0.19f, cy + radius * 0.34f)
                lineTo(cx + radius * 0.15f, cy + radius * 0.41f)
                lineTo(cx - radius * 0.15f, cy + radius * 0.41f)
                close()
            }
            drawPath(plate, Color.White.copy(alpha = 0.10f), style = Stroke(1f))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHand(
    cx: Float,
    cy: Float,
    length: Float,
    degrees: Float,
    color: Color,
    width: Float
) {
    val angle = Math.toRadians(degrees.toDouble() - 90.0)
    val endX = cx + cos(angle).toFloat() * length
    val endY = cy + sin(angle).toFloat() * length
    drawLine(
        color = Color.Black.copy(alpha = 0.65f),
        start = androidx.compose.ui.geometry.Offset(cx + 1.8f, cy + 2.0f),
        end = androidx.compose.ui.geometry.Offset(endX + 1.8f, endY + 2.0f),
        strokeWidth = width + 2.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(cx, cy),
        end = androidx.compose.ui.geometry.Offset(endX, endY),
        strokeWidth = width,
        cap = StrokeCap.Round
    )
}
