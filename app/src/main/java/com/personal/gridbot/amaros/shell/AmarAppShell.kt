package com.personal.gridbot.amaros.shell

import androidx.compose.runtime.Composable
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.navigation.AmarRoomHostScreen

/** Compatibility entry point. The approved living home is hosted by MainActivity. */
@Composable
fun AmarAppShell(initialState: AmarAppState = AmarAppState()) {
    AmarRoomHostScreen(
        room = initialState.selectedRoom,
        onBackHome = {}
    )
}
