package com.personal.gridbot.amaros.runtime

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real command lifecycle boundary. It records an intent durably and refuses
 * to claim broker execution. MT5 will acknowledge/execute these commands later.
 */
class AmarBotCommandEngine(context: Context) {
    private val dao = AmarOperationalDatabase.get(context.applicationContext).dao()

    suspend fun queue(botNumber: Int, command: String): Long = withContext(Dispatchers.IO) {
        require(botNumber in 1..10) { "Invalid bot number" }
        require(command.isNotBlank()) { "Command is blank" }
        dao.insertCommand(AmarRuntimeCommandRecord(botNumber = botNumber, command = command.trim(), status = "PENDING_MT5"))
    }
}
