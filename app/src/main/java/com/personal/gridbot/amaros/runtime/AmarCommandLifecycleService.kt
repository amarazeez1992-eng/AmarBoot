package com.personal.gridbot.amaros.runtime

import com.personal.gridbot.bridge.AmarBridgeContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Application-side command lifecycle. It records bridge acknowledgements and
 * verification results without performing or claiming broker execution.
 */
class AmarCommandLifecycleService(private val dao: AmarOperationalDao) {
    suspend fun acknowledge(commandId: Long): Boolean = updateNonTerminalStatus(
        commandId = commandId,
        status = AmarBridgeContract.ACKNOWLEDGED,
        error = null
    )

    suspend fun verify(commandId: Long): Boolean = updateTerminalStatus(
        commandId = commandId,
        status = AmarBridgeContract.VERIFIED,
        error = null
    )

    suspend fun reject(commandId: Long, reason: String): Boolean {
        require(reason.isNotBlank()) { "reason must not be blank" }
        return updateTerminalStatus(
            commandId = commandId,
            status = AmarBridgeContract.REJECTED,
            error = reason.trim()
        )
    }

    suspend fun fail(commandId: Long, reason: String): Boolean {
        require(reason.isNotBlank()) { "reason must not be blank" }
        return updateTerminalStatus(
            commandId = commandId,
            status = AmarBridgeContract.FAILED,
            error = reason.trim()
        )
    }

    suspend fun get(commandId: Long): AmarRuntimeCommandRecord? = withContext(Dispatchers.IO) {
        require(commandId > 0) { "Invalid command id" }
        dao.commandById(commandId)
    }

    private suspend fun updateNonTerminalStatus(
        commandId: Long,
        status: String,
        error: String?
    ): Boolean = updateStatus(commandId, status, error)

    private suspend fun updateTerminalStatus(
        commandId: Long,
        status: String,
        error: String?
    ): Boolean = updateStatus(commandId, status, error)

    private suspend fun updateStatus(
        commandId: Long,
        status: String,
        error: String?
    ): Boolean = withContext(Dispatchers.IO) {
        require(commandId > 0) { "Invalid command id" }
        val current = dao.commandById(commandId) ?: return@withContext false
        if (AmarBridgeContract.isTerminal(current.status)) return@withContext false

        dao.updateCommandStatusIfCurrent(
            commandId = commandId,
            expectedStatus = current.status,
            status = status,
            acknowledgedAt = System.currentTimeMillis(),
            error = error
        ) == 1
    }
}
