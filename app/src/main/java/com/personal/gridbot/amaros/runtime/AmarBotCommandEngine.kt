package com.personal.gridbot.amaros.runtime

import android.content.Context
import com.personal.gridbot.bridge.AmarBridgeContract
import com.personal.gridbot.bridge.AmarBridgeGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real command lifecycle boundary. It records an intent durably and hands
 * the resulting command id to the transport-agnostic MT5 bridge boundary.
 * Broker execution is never claimed here; MT5 must acknowledge/execute later.
 */
class AmarBotCommandEngine(context: Context) {
    private val dao = AmarOperationalDatabase.get(context.applicationContext).dao()
    private val bridge = AmarBridgeGateway()

    suspend fun queue(botNumber: Int, command: String): Long = withContext(Dispatchers.IO) {
        require(botNumber in 1..10) { "Invalid bot number" }
        require(command.isNotBlank()) { "Command is blank" }
        val commandId = dao.insertCommand(
            AmarRuntimeCommandRecord(
                botNumber = botNumber,
                command = command.trim(),
                status = AmarBridgeContract.PENDING_MT5
            )
        )
        check(bridge.submitForMt5(commandId.toString()) == AmarBridgeContract.PENDING_MT5) {
            "Bridge rejected pending hand-off"
        }
        commandId
    }
}
