package com.personal.gridbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.personal.gridbot.ui.theme.LocalAmarPalette

@Composable
fun AmarPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalAmarPalette.current

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                color = colors.panel,
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        content()
    }
}
