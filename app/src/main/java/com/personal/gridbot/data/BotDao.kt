package com.personal.gridbot.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BotDao {

    @Query("SELECT * FROM bots ORDER BY id DESC")
    fun getAllBots(): Flow<List<Bot>>

    @Insert
    suspend fun insertBot(bot: Bot): Long

    @Update
    suspend fun updateBot(bot: Bot)

    @Delete
    suspend fun deleteBot(bot: Bot)

    @Query("SELECT * FROM bot_logs WHERE botId = :botId ORDER BY timestamp DESC LIMIT 200")
    fun getLogsForBot(botId: Long): Flow<List<BotLog>>

    @Insert
    suspend fun insertLog(log: BotLog)
}
