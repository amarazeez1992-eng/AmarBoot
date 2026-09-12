package com.personal.gridbot.amaros.bots

import android.content.Context
import androidx.compose.runtime.Composable

private const val PREFS = "amar_bot_interfaces"

enum class AmarBotInterface(val label: String) {
    A("واجهة 1"),
    B("واجهة 2 — سحب وإفلات")
}

object AmarBotInterfaceRegistry {
    fun load(context: Context, botNumber: Int): AmarBotInterface {
        val stored = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("bot_$botNumber", AmarBotInterface.B.name)
        return runCatching { AmarBotInterface.valueOf(stored ?: AmarBotInterface.B.name) }
            .getOrDefault(AmarBotInterface.B)
    }

    fun save(context: Context, botNumber: Int, interfaceId: AmarBotInterface) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("bot_$botNumber", interfaceId.name)
            .apply()
    }
}

/**
 * Bot-Lab interface host.
 * Interface 2 is now the active visual surface; Interface 1 remains intact
 * and can still be selected through the existing registry contract.
 */
@Composable
fun AmarBotLabInterfaceHost(onBackHome: () -> Unit) {
    AmarBotLabInterface2Screen(onBackHome = onBackHome)
}
