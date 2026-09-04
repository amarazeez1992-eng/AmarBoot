package com.personal.gridbot.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.ui.theme.LocalAmarPalette

enum class AmarButtonType {
    DEFAULT,
    BUY,
    SELL,
    WARNING,
    DANGER,
    SUCCESS
}

@Composable
fun AmarButton(
    text: String,
    modifier: Modifier = Modifier,
    type: AmarButtonType = AmarButtonType.DEFAULT,
    enabled: Boolean = true,
    glowing: Boolean = false,
    onClick: () -> Unit
) {
    val colors = LocalAmarPalette.current

    val buttonColor = when (type) {
        AmarButtonType.BUY -> colors.buy
        AmarButtonType.SELL -> colors.sell
        AmarButtonType.WARNING -> colors.warning
        AmarButtonType.DANGER -> colors.danger
        AmarButtonType.SUCCESS -> colors.profit
        AmarButtonType.DEFAULT -> colors.surfaceElevated
    }

    val textColor = when (type) {
        AmarButtonType.DEFAULT -> colors.textPrimary
        else -> Color.Black
    }

    val transition = rememberInfiniteTransition(
        label = "amar_button_glow"
    )

    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (glowing) 1.025f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amar_button_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(pulse)
            .background(
                color = if (enabled) {
                    buttonColor
                } else {
                    colors.surface
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (enabled) {
                    buttonColor.copy(alpha = 0.75f)
                } else {
                    colors.border
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(
                vertical = 11.dp,
                horizontal = 14.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else colors.textMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
