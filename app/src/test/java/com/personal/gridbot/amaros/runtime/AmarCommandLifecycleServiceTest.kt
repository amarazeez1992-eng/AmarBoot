package com.personal.gridbot.amaros.runtime

import com.personal.gridbot.bridge.AmarBridgeContract
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarCommandLifecycleServiceTest {
    @Test
    fun acknowledgeMovesPendingCommandToAcknowledged() = runBlocking {
        val dao = MutableFakeDao(AmarRuntimeCommandRecord(id = 7, botNumber = 1, command = "PING", status = AmarBridgeContract.PENDING_MT5))
        val service = AmarCommandLifecycleService(dao)

        assertTrue(service.acknowledge(7))
        assertEquals(AmarBridgeContract.ACKNOWLEDGED, dao.current?.status)
        assertTrue(dao.current?.acknowledgedAt != null)
    }

    @Test
    fun verifyRequiresAcknowledgementOrExecution() = runBlocking {
        val dao = MutableFakeDao(AmarRuntimeCommandRecord(id = 7, botNumber = 1, command = "PING", status = AmarBridgeContract.PENDING_MT5))
        val service = AmarCommandLifecycleService(dao)

        assertFalse(service.verify(7))
        assertEquals(AmarBridgeContract.PENDING_MT5, dao.current?.status)

        assertTrue(service.acknowledge(7))
        assertTrue(service.verify(7))
        assertEquals(AmarBridgeContract.VERIFIED, dao.current?.status)
    }

    @Test
    fun terminalCommandCannotBeAcknowledgedAgain() = runBlocking {
        val dao = MutableFakeDao(AmarRuntimeCommandRecord(id = 7, botNumber = 1, command = "PING", status = AmarBridgeContract.VERIFIED))
        val service = AmarCommandLifecycleService(dao)

        assertFalse(service.acknowledge(7))
        assertEquals(AmarBridgeContract.VERIFIED, dao.current?.status)
    }

    @Test
    fun rejectStoresTrimmedReason() = runBlocking {
        val dao = MutableFakeDao(AmarRuntimeCommandRecord(id = 7, botNumber = 1, command = "PING", status = AmarBridgeContract.PENDING_MT5))
        val service = AmarCommandLifecycleService(dao)

        assertTrue(service.reject(7, "  rejected by bridge  "))
        assertEquals(AmarBridgeContract.REJECTED, dao.current?.status)
        assertEquals("rejected by bridge", dao.current?.error)
    }
}

private class MutableFakeDao(
    initial: AmarRuntimeCommandRecord
) : AmarOperationalDao {
    var current: AmarRuntimeCommandRecord? = initial

    override fun observeBots() = flowOf(emptyList<AmarBotRuntimeRecord>())
    override fun observeBot(botNumber: Int) = flowOf(null)
    override fun observeLatestCommand(botNumber: Int) = flowOf(current)
    override suspend fun upsertBots(rows: List<AmarBotRuntimeRecord>) = Unit
    override suspend fun upsertOrders(rows: List<AmarRuntimeOrderRecord>) = Unit
    override suspend fun upsertPositions(rows: List<AmarRuntimePositionRecord>) = Unit
    override suspend fun insertCommand(command: AmarRuntimeCommandRecord) = command.id
    override suspend fun updateCommandStatusIfCurrent(
        commandId: Long,
        expectedStatus: String,
        status: String,
        acknowledgedAt: Long?,
        error: String?
    ): Int {
        val row = current ?: return 0
        if (row.id != commandId || row.status != expectedStatus) return 0
        current = row.copy(status = status, acknowledgedAt = acknowledgedAt, error = error)
        return 1
    }
    override suspend fun commandById(commandId: Long) = current?.takeIf { it.id == commandId }
    override suspend fun ordersForBot(botNumber: Int) = emptyList<AmarRuntimeOrderRecord>()
    override suspend fun positionsForBot(botNumber: Int) = emptyList<AmarRuntimePositionRecord>()
}
