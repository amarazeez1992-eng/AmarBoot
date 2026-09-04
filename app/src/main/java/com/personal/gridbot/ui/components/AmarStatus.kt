package com.personal.gridbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.ui.theme.LocalAmarPalette

enum class AmarStatusType {
    ONLINE,
    OFFLINE,
    RUNNING,
    PAUSED,
    PROFIT,
    LOSS,
    NEUTRAL
}

@Composable
fun AmarStatus(
    text: String,
    type: AmarStatusType = AmarStatusType.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val colors = LocalAmarPalette.current

    val statusColor = when (type) {
        AmarStatusType.ONLINE -> colors.profit
        AmarStatusType.OFFLINE -> colors.textMuted
        AmarStatusType.RUNNING -> colors.buy
        AmarStatusType.PAUSED -> colors.warning
        AmarStatusType.PROFIT -> colors.profit
        AmarStatusType.LOSS -> colors.loss
        AmarStatusType.NEUTRAL -> colors.neutral
    }

    Row(
        modifier = modifier
            .background(
                color = statusColor.copy(alpha = 0.10f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .background(
                    color = statusColor,
                    shape = CircleShape
                )
                .padding(4.dp)
        )

        Text(
            text = text,
            color = statusColor,
            fontSize = 11.sp
        )
    }
}
