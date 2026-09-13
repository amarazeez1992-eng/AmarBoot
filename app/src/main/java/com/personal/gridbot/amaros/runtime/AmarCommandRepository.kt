package com.personal.gridbot.amaros.runtime

import kotlinx.coroutines.flow.Flow

/**
 * Read-only command state access for the Android layer.
 * Command mutation remains owned by AmarCommandLifecycleService.
 */
class AmarCommandRepository(private val dao: AmarOperationalDao) {
    fun observeLatest(botNumber: Int): Flow<AmarRuntimeCommandRecord?> {
        require(botNumber in 1..10) { "Invalid bot number" }
        return dao.observeLatestCommand(botNumber)
    }

    suspend fun get(commandId: Long): AmarRuntimeCommandRecord? {
        require(commandId > 0) { "Invalid command id" }
        return dao.commandById(commandId)
    }
}
