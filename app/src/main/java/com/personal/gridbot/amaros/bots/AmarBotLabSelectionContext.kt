package com.personal.gridbot.amaros.bots

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/** Shared Bot-Lab navigation state. Bot selection is shared across all interfaces. */
object AmarBotLabSelectionContext {
    var selectedBot: Int by mutableIntStateOf(1)
}
