package com.personal.gridbot.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * تأثير Glow متحرك خفيف.
 *
 * يستخدم لاحقًا للأزرار:
 * BUY / SELL / GRID / TRADING وغيرها.
 */

@Composable
fun rememberAmarGlowAlpha(
    enabled: Boolean = true,
    minAlpha: Float = 0.25f,
    maxAlpha: Float = 0.75f
): Float {

    if (!enabled) {
        return 0f
    }

    val transition = rememberInfiniteTransition(
        label = "amar_glow_transition"
    )

    val alpha by transition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amar_glow_alpha"
    )

    return alpha
}

/**
 * يضيف هالة ضوئية حول العنصر.
 */
@Composable
fun Modifier.amarGlow(
    color: Color,
    enabled: Boolean = true,
    radius: Float = 18f
): Modifier {

    val alpha = rememberAmarGlowAlpha(enabled)

    return this.drawBehind {

        if (alpha <= 0f) return@drawBehind

        val glowColor = color.copy(
            alpha = alpha * 0.28f
        )

        val strokeWidth = max(
            1f,
            radius / 3f
        )

        drawRoundRect(
            color = glowColor,
            cornerRadius = CornerRadius(
                radius,
                radius
            ),
            style = Stroke(
                width = strokeWidth
            )
        )
    }
}

/**
 * صندوق مساعد لعناصر AMAR المضيئة.
 */
@Composable
fun AmarGlowBox(
    color: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    Box(
        modifier = modifier.amarGlow(
            color = color,
            enabled = enabled
        )
    ) {
        content()
    }
}
