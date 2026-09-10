package com.personal.gridbot.amaros.shell

import androidx.compose.runtime.Composable
import com.personal.gridbot.amaros.core.AmarAppState

/**
 * Compatibility entry point. The approved white Aurora interface is the visual shell.
 * Room implementations remain available behind the existing architecture and are not replaced.
 */
@Composable
fun AmarAppShell(initialState: AmarAppState = AmarAppState(), onOpenLegacyGrid: () -> Unit = {}) {
    AmarAuroraShell(initialState, onOpenLegacyGrid)
}
