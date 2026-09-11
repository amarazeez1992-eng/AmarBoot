package com.personal.gridbot.amaros.bots

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

private const val PREFS = "amar_bot_interfaces"

enum class AmarBotInterface(val label: String) {
    A("واجهة 1")
}

object AmarBotInterfaceRegistry {
    fun load(context: Context, botNumber: Int): AmarBotInterface = AmarBotInterface.A

    fun save(context: Context, botNumber: Int, interfaceId: AmarBotInterface) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("bot_$botNumber", AmarBotInterface.A.name).apply()
    }
}

/** Single active Bot-Lab interface. Interface 2 has been retired. */
@Composable
fun AmarBotLabInterfaceHost(onBackHome: () -> Unit) {
    AmarBotLabInterfaceAEnhancedScreen(onBackHome = onBackHome)
}
