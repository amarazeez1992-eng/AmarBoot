package com.personal.gridbot.amaros.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.cos
import kotlin.math.sin

/**
 * AMAR B4 visual core. This layer is intentionally independent from trading logic.
 * It provides a continuously living field of light, depth, particles and motion.
 */
@Composable
fun AmarLivingVisualEngine(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "amar-living-field")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "field-phase"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "field-pulse"
    )
    var touchX by remember { mutableStateOf(0f) }
    var touchY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    touchX = (touchX + drag.x).coerceIn(-700f, 700f)
                    touchY = (touchY + drag.y).coerceIn(-1200f, 1200f)
                }
            }
            .background(Color(0xFF030712))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = phase * 6.283185f
            val px = w * 0.50f + touchX
            val py = h * 0.45f + touchY
            val glow = (0.16f * intensity * pulse).coerceIn(0.05f, 0.34f)

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E6CFF).copy(alpha = glow), Color.Transparent),
                    center = Offset(px, py),
                    radius = w * 0.82f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CFF).copy(alpha = glow * 0.9f), Color.Transparent),
                    center = Offset(w * 0.18f + cos(t) * w * 0.16f, h * 0.25f + sin(t) * h * 0.12f),
                    radius = w * 0.52f
                ),
                radius = w * 0.52f,
                center = Offset(w * 0.18f + cos(t) * w * 0.16f, h * 0.25f + sin(t) * h * 0.12f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF22E6C7).copy(alpha = glow * 0.72f), Color.Transparent),
                    center = Offset(w * 0.84f + sin(t * 0.8f) * w * 0.12f, h * 0.70f + cos(t * 0.7f) * h * 0.10f),
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = Offset(w * 0.84f + sin(t * 0.8f) * w * 0.12f, h * 0.70f + cos(t * 0.7f) * h * 0.10f)
            )

            val horizon = h * 0.72f
            for (i in 0..14) {
                val y = horizon + i * h * 0.026f
                val shift = sin(t + i * 0.22f) * w * 0.018f
                drawLine(
                    color = Color(0xFF35D6FF).copy(alpha = 0.045f * intensity),
                    start = Offset(w * 0.04f + shift, y),
                    end = Offset(w * 0.96f + shift, y),
                    strokeWidth = 1f
                )
            }
            for (i in -12..12) {
                val x = w / 2f + i * w * 0.045f
                drawLine(
                    color = Color(0xFF8B5CFF).copy(alpha = 0.04f * intensity),
                    start = Offset(w / 2f, horizon),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }

            repeat(42) { i ->
                val seed = i * 1.618f
                val x = ((seed * 97f) % w + sin(t * 1.7f + seed) * 16f + w) % w
                val y = ((seed * 173f) % h + cos(t * 1.2f + seed) * 14f + h) % h
                val r = 1.1f + ((i % 4) * 0.55f)
                drawCircle(Color(0xFFB9F7FF).copy(alpha = 0.18f * intensity), r, Offset(x, y))
            }

            val ringCenter = Offset(w * 0.78f, h * 0.24f)
            val ring = w * (0.08f + 0.012f * sin(t))
            drawCircle(Color(0xFF35D6FF).copy(alpha = 0.10f * intensity), ring, ringCenter, style = Stroke(1.4f))
            drawCircle(Color(0xFF8B5CFF).copy(alpha = 0.07f * intensity), ring * 1.35f, ringCenter, style = Stroke(1f))
        }

        content()
    }
}

@Composable
fun Modifier.amarDepthMotion(
    phase: Float,
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this
    val x = sin(phase * 6.283185f) * 1.6f
    val y = cos(phase * 6.283185f) * 1.1f
    return graphicsLayer {
        translationX = x
        translationY = y
        rotationX = cos(phase * 6.283185f) * 0.45f
        rotationY = sin(phase * 6.283185f) * 0.55f
        shadowElevation = 10f
    }
}
