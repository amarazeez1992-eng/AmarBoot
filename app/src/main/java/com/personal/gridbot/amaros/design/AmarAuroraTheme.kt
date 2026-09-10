package com.personal.gridbot.amaros.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Compatibility theme kept for older entry points.
 * The approved home interface remains the reference HTML interface.
 */
@Composable
fun AmarAuroraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(content = content)
}
