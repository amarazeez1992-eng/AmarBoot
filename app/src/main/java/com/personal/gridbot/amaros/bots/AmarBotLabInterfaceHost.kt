package com.personal.gridbot.amaros.bots

import android.content.Context
import androidx.compose.runtime.Composable

private const val PREFS = "amar_bot_interfaces"

enum class AmarBotInterface(val label: String) {
    A("واجهة 1")
}

object AmarBotInterfaceRegistry {
    fun load(context: Context, botNumber: Int): AmarBotInterface = AmarBotInterface.A

    fun save(context: Context, botNumber: Int, interfaceId: AmarBotInterface) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("bot_$botNumber", AmarBotInterface.A.name)
            .apply()
    }
}

/**
 * Single active Bot-Lab interface.
 *
 * The enhanced visual prototype is intentionally not the runtime host:
 * production remains wired to the existing Bot Lab business logic,
 * persistence, command lifecycle and verified BOT1 gateway.
 */
@Composable
fun AmarBotLabInterfaceHost(onBackHome: () -> Unit) {
    AmarBotLabProfessionalScreen(onBackHome = onBackHome)
}
