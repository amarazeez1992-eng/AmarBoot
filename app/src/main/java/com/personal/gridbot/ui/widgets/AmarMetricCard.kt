package com.personal.gridbot.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.ui.theme.LocalAmarPalette

@Composable
fun AmarMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null,
    active: Boolean = false
) {
    val colors = LocalAmarPalette.current

    val scale by animateFloatAsState(
        targetValue = if (active) 1.02f else 1f,
        animationSpec = tween(350),
        label = "metric_scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .background(
                color = colors.surfaceElevated,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = if (active) {
                    valueColor ?: colors.accent
                } else {
                    colors.border
                },
                shape = RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {

        Text(
            text = title,
            color = colors.textMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = value,
            color = valueColor ?: colors.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
