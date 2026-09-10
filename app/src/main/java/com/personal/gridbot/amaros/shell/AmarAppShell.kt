package com.personal.gridbot.amaros.shell

import androidx.compose.runtime.Composable
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.navigation.AmarRoomHostScreen
import com.personal.gridbot.ui.theme.AmarThemeMode

/** Compatibility entry point. The approved living home is hosted by MainActivity. */
@Composable
fun AmarAppShell(
    initialState: AmarAppState = AmarAppState(),
    themeMode: AmarThemeMode = AmarThemeMode.DARK,
    onThemeModeChange: (AmarThemeMode) -> Unit = {}
) {
    AmarRoomHostScreen(
        room = initialState.selectedRoom,
        onBackHome = {},
        themeMode = themeMode,
        onThemeModeChange = onThemeModeChange
    )
}
