package com.personal.gridbot.amaros.shell

import androidx.compose.runtime.Composable
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.navigation.AmarRoom
import com.personal.gridbot.amaros.navigation.AmarRoomHostScreen

/** Compatibility entry point. The approved living home is hosted by MainActivity. */
@Composable
fun AmarAppShell(initialState: AmarAppState = AmarAppState(), onOpenLegacyGrid: () -> Unit = {}) {
    AmarRoomHostScreen(
        room = initialState.selectedRoom,
        onBackHome = {},
        onOpenLegacyGrid = onOpenLegacyGrid
    )
}
