package com.personal.gridbot.amaros.runtime

import android.content.Context
import com.personal.gridbot.amaros.bots.AmarBotVaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Real operational aggregation layer.
 * It never invents broker statistics. If no broker snapshot has been received,
 * counts remain zero and status remains IDLE.
 */
class AmarBotOperationalEngine(context: Context) {
    private val appContext = context.applicationContext
    private val dao = AmarOperationalDatabase.get(appContext).dao()
    private val vault = AmarBotVaultRepository(appContext)

    fun observeBots(): Flow<List<AmarBotRuntimeRecord>> = dao.observeBots()

    suspend fun ensureBotCatalog() = withContext(Dispatchers.IO) {
        val rows = vault.load().map { bot ->
            val previous = dao.ordersForBot(bot.botNumber)
            val positions = dao.positionsForBot(bot.botNumber)
            AmarBotRuntimeRecord(
                botNumber = bot.botNumber,
                name = bot.name,
                status = when {
                    positions.isNotEmpty() -> "ACTIVE"
                    previous.any { it.status.equals("PENDING", true) } -> "ARMED"
                    else -> "IDLE"
                },
                openPositions = positions.count { !it.status.equals("CLOSED", true) },
                pendingOrders = previous.count { it.status.equals("PENDING", true) },
                totalLots = positions.filter { !it.status.equals("CLOSED", true) }.sumOf { it.volume },
                floatingPnl = positions.filter { !it.status.equals("CLOSED", true) }.sumOf { it.floatingPnl },
                updatedAt = System.currentTimeMillis()
            )
        }
        dao.upsertBots(rows)
    }

    suspend fun refreshAggregates(botNumber: Int) = withContext(Dispatchers.IO) {
        val bot = vault.load().firstOrNull { it.botNumber == botNumber } ?: return@withContext
        val orders = dao.ordersForBot(botNumber)
        val positions = dao.positionsForBot(botNumber)
        dao.upsertBots(
            listOf(
                AmarBotRuntimeRecord(
                    botNumber = botNumber,
                    name = bot.name,
                    status = when {
                        positions.any { !it.status.equals("CLOSED", true) } -> "ACTIVE"
                        orders.any { it.status.equals("PENDING", true) } -> "ARMED"
                        else -> "IDLE"
                    },
                    openPositions = positions.count { !it.status.equals("CLOSED", true) },
                    pendingOrders = orders.count { it.status.equals("PENDING", true) },
                    totalLots = positions.filter { !it.status.equals("CLOSED", true) }.sumOf { it.volume },
                    floatingPnl = positions.filter { !it.status.equals("CLOSED", true) }.sumOf { it.floatingPnl },
                    updatedAt = System.currentTimeMillis()
                )
            )
        )
    }
}
