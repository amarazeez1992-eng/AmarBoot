package com.personal.gridbot.amaros.runtime

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmarCommandRepositoryTest {
    @Test
    fun observeLatestDelegatesToDao() = runBlocking {
        val expected = AmarRuntimeCommandRecord(
            id = 7,
            botNumber = 1,
            command = "PING",
            status = "PENDING_MT5"
        )
        val repository = AmarCommandRepository(FakeDao(latest = flowOf(expected)))
        assertEquals(expected, repository.observeLatest(1).first())
    }

    @Test
    fun getReturnsDaoValue() = runBlocking {
        val expected = AmarRuntimeCommandRecord(
            id = 7,
            botNumber = 1,
            command = "PING",
            status = "PENDING_MT5"
        )
        assertEquals(expected, AmarCommandRepository(FakeDao(command = expected)).get(7))
    }

    @Test
    fun getMissingReturnsNull() = runBlocking {
        assertNull(AmarCommandRepository(FakeDao()).get(7))
    }
}

private class FakeDao(
    private val latest: kotlinx.coroutines.flow.Flow<AmarRuntimeCommandRecord?> = flowOf(null),
    private val command: AmarRuntimeCommandRecord? = null
) : AmarOperationalDao {
    override fun observeBots() = flowOf(emptyList<AmarBotRuntimeRecord>())
    override fun observeBot(botNumber: Int) = flowOf(null)
    override fun observeLatestCommand(botNumber: Int) = latest
    override suspend fun upsertBots(rows: List<AmarBotRuntimeRecord>) = Unit
    override suspend fun upsertOrders(rows: List<AmarRuntimeOrderRecord>) = Unit
    override suspend fun upsertPositions(rows: List<AmarRuntimePositionRecord>) = Unit
    override suspend fun insertCommand(command: AmarRuntimeCommandRecord) = 1L
    override suspend fun updateCommandStatus(commandId: Long, status: String, acknowledgedAt: Long?, error: String?) = Unit
    override suspend fun commandById(commandId: Long) = command
    override suspend fun ordersForBot(botNumber: Int) = emptyList<AmarRuntimeOrderRecord>()
    override suspend fun positionsForBot(botNumber: Int) = emptyList<AmarRuntimePositionRecord>()
}
