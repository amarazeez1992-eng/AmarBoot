package com.personal.gridbot.amaros.bots

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val PREFS = "amar_bot_interfaces"

enum class AmarBotInterface(val label: String) {
    A("واجهة 1")
}

object AmarBotInterfaceRegistry {
    fun load(context: Context, botNumber: Int): AmarBotInterface {
        val stored = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("bot_$botNumber", AmarBotInterface.A.name)
        return runCatching { AmarBotInterface.valueOf(stored ?: AmarBotInterface.A.name) }
            .getOrDefault(AmarBotInterface.A)
    }

    fun save(context: Context, botNumber: Int, interfaceId: AmarBotInterface) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("bot_$botNumber", interfaceId.name)
            .apply()
    }
}

/** Production Interface 1 host. Interface 2 is intentionally not referenced or modified. */
@Composable
fun AmarBotLabInterfaceHost(onBackHome: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        AmarBotLabProfessionalScreen(onBackHome = onBackHome)
        AmarBotLabEngineStatusOverlay()
    }
}
