package com.personal.gridbot.amaros.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Reusable Aurora glass surface. Future visual changes stay in the design layer. */
@Composable
fun AmarGlassSurface(
    modifier: Modifier = Modifier,
    colors: AmarAuroraColors = AmarAuroraPalette.default,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(AmarUiTokens.cardRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(colors.surface.copy(alpha = 0.88f), colors.surfaceStrong.copy(alpha = 0.70f))
                )
            )
            .border(1.dp, colors.primary.copy(alpha = 0.16f), shape),
        content = content
    )
}

fun Color.withAlpha(alpha: Float): Color = copy(alpha = alpha.coerceIn(0f, 1f))
