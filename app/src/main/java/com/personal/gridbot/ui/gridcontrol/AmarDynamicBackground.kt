package com.personal.gridbot.ui.gridcontrol

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.sin

@Composable
fun AmarDynamicBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(
        label = "amar_dynamic_background"
    )

    val movement by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 9000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background_movement"
    )

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF05070A),
                    Color(0xFF0A0F16),
                    Color(0xFF080A10),
                    Color(0xFF050609)
                ),
                start = Offset(0f, 0f),
                end = Offset(width, height)
            )
        )

        val glowX = width * (0.15f + movement * 0.70f)
        val glowY = height * (0.15f + movement * 0.20f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x3340C4FF),
                    Color.Transparent
                ),
                center = Offset(glowX, glowY),
                radius = width * 0.55f
            ),
            radius = width * 0.55f,
            center = Offset(glowX, glowY)
        )

        val goldX = width * (0.80f - movement * 0.30f)
        val goldY = height * (0.72f - movement * 0.15f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x22FFC107),
                    Color.Transparent
                ),
                center = Offset(goldX, goldY),
                radius = width * 0.45f
            ),
            radius = width * 0.45f,
            center = Offset(goldX, goldY)
        )

        val gridSpacing = 70f

        var x = 0f
        while (x <= width) {
            drawLine(
                color = Color(0x1200BFFF),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += gridSpacing
        }

        var y = 0f
        while (y <= height) {
            drawLine(
                color = Color(0x1200BFFF),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += gridSpacing
        }

        val waveBase = height * 0.70f
        val amplitude = 20f

        var previous = Offset(
            0f,
            waveBase
        )

        for (i in 1..80) {
            val px = width * (i / 80f)
            val py = waveBase +
                    sin(
                        (i * 0.32f) + movement * 5f
                    ) * amplitude

            val current = Offset(px, py)

            drawLine(
                color = Color(0x1600E5FF),
                start = previous,
                end = current,
                strokeWidth = 2f
            )

            previous = current
        }
    }
}
