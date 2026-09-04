package com.personal.gridbot.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.gridbot.ui.components.AmarButton
import com.personal.gridbot.ui.components.AmarButtonType

@Composable
fun AmarControlRow(
    leftText: String,
    rightText: String,
    leftType: AmarButtonType,
    rightType: AmarButtonType,
    leftEnabled: Boolean = true,
    rightEnabled: Boolean = true,
    leftGlow: Boolean = false,
    rightGlow: Boolean = false,
    onLeftClick: () -> Unit = {},
    onRightClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        AmarButton(
            text = leftText,
            modifier = Modifier.weight(1f),
            type = leftType,
            enabled = leftEnabled,
            glowing = leftGlow,
            onClick = onLeftClick
        )

        AmarButton(
            text = rightText,
            modifier = Modifier.weight(1f),
            type = rightType,
            enabled = rightEnabled,
            glowing = rightGlow,
            onClick = onRightClick
        )
    }
}
