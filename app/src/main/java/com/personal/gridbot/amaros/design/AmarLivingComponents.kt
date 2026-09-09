package com.personal.gridbot.amaros.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AmarLivingGlass(
    modifier: Modifier = Modifier,
    accent: Color = AmarAuroraPalette.default.primary,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "glass-light")
    val sweep by transition.animateFloat(
        initialValue = -0.25f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            tween(5200, easing = FastOutSlowInEasing),
            RepeatMode.Restart
        ),
        label = "glass-sweep"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(tween(2300), RepeatMode.Reverse),
        label = "glass-pulse"
    )

    Box(
        modifier = modifier
            .shadow(18.dp, RoundedCornerShape(24.dp), ambientColor = accent.copy(alpha = 0.22f), spotColor = accent.copy(alpha = 0.30f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.105f),
                        accent.copy(alpha = 0.045f + pulse * 0.025f),
                        Color.White.copy(alpha = 0.035f)
                    )
                )
            )
            .border(1.dp, accent.copy(alpha = 0.16f + pulse * 0.08f), RoundedCornerShape(24.dp))
    ) {
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.055f),
                            Color.Transparent
                        ),
                        start = androidx.compose.ui.geometry.Offset(sweep * 1200f, 0f),
                        end = androidx.compose.ui.geometry.Offset((sweep + 0.45f) * 1200f, 900f)
                    )
                )
        )
        Box(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun AmarLivingButton(
    label: String,
    active: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "button-light")
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1250), RepeatMode.Reverse),
        label = "button-pulse"
    )
    val interaction = remember { MutableInteractionSource() }
    val alpha = if (active) 0.16f + pulse * 0.13f else 0.055f
    Box(
        modifier = modifier
            .shadow(if (active) 18.dp else 6.dp, RoundedCornerShape(16.dp), spotColor = accent.copy(alpha = if (active) 0.45f else 0.15f))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = alpha), Color.White.copy(alpha = 0.055f), accent.copy(alpha = alpha * 0.55f))
                )
            )
            .border(1.dp, accent.copy(alpha = if (active) 0.65f else 0.20f), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .width(7.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (active) accent else Color(0xFF667085))
            )
            Text(label, color = Color.White.copy(alpha = if (active) 1f else 0.72f))
        }
    }
}

@Composable
fun AmarLivingMetric(
    title: String,
    value: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    AmarLivingGlass(modifier, accent) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color.White.copy(alpha = 0.68f))
                Text("LIVE", color = accent.copy(alpha = 0.95f))
            }
            Text(value, color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(1.dp))
            Text(detail, color = Color.White.copy(alpha = 0.48f))
        }
    }
}
