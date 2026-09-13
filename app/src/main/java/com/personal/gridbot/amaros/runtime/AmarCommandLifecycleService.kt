package com.personal.gridbot.amaros.runtime

import com.personal.gridbot.bridge.AmarBridgeContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Durable application-side command lifecycle.
 *
 * The service is the command-state authority for persisted records. It never performs or
 * claims broker execution. ACK and execution are deliberately separate from verification.
 */
class AmarCommandLifecycleService(private val dao: AmarOperationalDao) {
    suspend fun acknowledge(commandId: Long): Boolean = transition(
        commandId = commandId,
        allowedCurrent = setOf(AmarBridgeContract.PENDING_MT5),
        nextStatus = AmarBridgeContract.ACKNOWLEDGED,
        error = null,
        stampAcknowledgement = true,
    )

    /** Records that the governed bridge has actually executed the command. */
    suspend fun markExecuted(commandId: Long): Boolean = transition(
        commandId = commandId,
        allowedCurrent = setOf(AmarBridgeContract.ACKNOWLEDGED),
        nextStatus = AmarBridgeContract.EXECUTED,
        error = null,
        stampAcknowledgement = false,
    )

    /** Verification is valid only after an explicit EXECUTED state. */
    suspend fun verify(commandId: Long): Boolean = transition(
        commandId = commandId,
        allowedCurrent = setOf(AmarBridgeContract.EXECUTED),
        nextStatus = AmarBridgeContract.VERIFIED,
        error = null,
        stampAcknowledgement = false,
    )

    suspend fun reject(commandId: Long, reason: String): Boolean {
        require(reason.isNotBlank()) { "reason must not be blank" }
        return transition(
            commandId = commandId,
            allowedCurrent = setOf(
                AmarBridgeContract.PENDING_MT5,
                AmarBridgeContract.ACKNOWLEDGED,
            ),
            nextStatus = AmarBridgeContract.REJECTED,
            error = reason.trim(),
            stampAcknowledgement = false,
        )
    }

    suspend fun fail(commandId: Long, reason: String): Boolean {
        require(reason.isNotBlank()) { "reason must not be blank" }
        return transition(
            commandId = commandId,
            allowedCurrent = NON_TERMINAL_STATES,
            nextStatus = AmarBridgeContract.FAILED,
            error = reason.trim(),
            stampAcknowledgement = false,
        )
    }

    suspend fun markStale(commandId: Long, reason: String = "Command became stale"): Boolean {
        require(reason.isNotBlank()) { "reason must not be blank" }
        return transition(
            commandId = commandId,
            allowedCurrent = NON_TERMINAL_STATES,
            nextStatus = AmarBridgeContract.STALE,
            error = reason.trim(),
            stampAcknowledgement = false,
        )
    }

    suspend fun get(commandId: Long): AmarRuntimeCommandRecord? = withContext(Dispatchers.IO) {
        require(commandId > 0) { "Invalid command id" }
        dao.commandById(commandId)
    }

    private suspend fun transition(
        commandId: Long,
        allowedCurrent: Set<String>,
        nextStatus: String,
        error: String?,
        stampAcknowledgement: Boolean,
    ): Boolean = withContext(Dispatchers.IO) {
        require(commandId > 0) { "Invalid command id" }
        require(AmarBridgeContract.isKnown(nextStatus)) { "Unknown next command state" }
        val current = dao.commandById(commandId) ?: return@withContext false
        if (current.status !in allowedCurrent || AmarBridgeContract.isTerminal(current.status)) return@withContext false

        dao.updateCommandStatusIfCurrent(
            commandId = commandId,
            expectedStatus = current.status,
            status = nextStatus,
            acknowledgedAt = if (stampAcknowledgement) System.currentTimeMillis() else current.acknowledgedAt,
            error = error
        ) == 1
    }

    private companion object {
        val NON_TERMINAL_STATES = setOf(
            AmarBridgeContract.PENDING_MT5,
            AmarBridgeContract.ACKNOWLEDGED,
            AmarBridgeContract.EXECUTED,
        )
    }
}
