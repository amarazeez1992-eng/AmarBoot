package com.personal.gridbot.amaros.runtime

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/** Persistent operational state. Broker adapters upsert observed MT5 orders/positions here. */
@Entity(tableName = "amar_bot_runtime")
data class AmarBotRuntimeRecord(
    @androidx.room.PrimaryKey val botNumber: Int,
    val name: String,
    val status: String = "IDLE",
    val openPositions: Int = 0,
    val pendingOrders: Int = 0,
    val totalLots: Double = 0.0,
    val realizedPnl: Double = 0.0,
    val floatingPnl: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "amar_runtime_orders", indices = [Index(value = ["brokerOrderId"], unique = true), Index(value = ["botNumber"])])
data class AmarRuntimeOrderRecord(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brokerOrderId: String,
    val botNumber: Int,
    val strategyNumber: Int,
    val symbol: String,
    val side: String,
    val volume: Double,
    val price: Double,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "amar_runtime_positions", indices = [Index(value = ["brokerPositionId"], unique = true), Index(value = ["botNumber"])])
data class AmarRuntimePositionRecord(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brokerPositionId: String,
    val botNumber: Int,
    val strategyNumber: Int,
    val symbol: String,
    val side: String,
    val volume: Double,
    val openPrice: Double,
    val currentPrice: Double,
    val floatingPnl: Double,
    val status: String,
    val openedAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "amar_runtime_commands", indices = [Index(value = ["botNumber"]), Index(value = ["createdAt"])])
data class AmarRuntimeCommandRecord(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botNumber: Int,
    val command: String,
    val status: String,
    val createdAt: Long = System.currentTimeMillis(),
    val acknowledgedAt: Long? = null,
    val error: String? = null
)

@Dao
interface AmarOperationalDao {
    @Query("SELECT * FROM amar_bot_runtime ORDER BY botNumber ASC")
    fun observeBots(): Flow<List<AmarBotRuntimeRecord>>

    @Query("SELECT * FROM amar_bot_runtime WHERE botNumber = :botNumber LIMIT 1")
    fun observeBot(botNumber: Int): Flow<AmarBotRuntimeRecord?>

    @Query("SELECT * FROM amar_runtime_commands WHERE botNumber = :botNumber ORDER BY createdAt DESC LIMIT 1")
    fun observeLatestCommand(botNumber: Int): Flow<AmarRuntimeCommandRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBots(rows: List<AmarBotRuntimeRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrders(rows: List<AmarRuntimeOrderRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPositions(rows: List<AmarRuntimePositionRecord>)

    @Insert
    suspend fun insertCommand(command: AmarRuntimeCommandRecord): Long

    @Query("SELECT * FROM amar_runtime_orders WHERE botNumber = :botNumber ORDER BY updatedAt DESC")
    suspend fun ordersForBot(botNumber: Int): List<AmarRuntimeOrderRecord>

    @Query("SELECT * FROM amar_runtime_positions WHERE botNumber = :botNumber ORDER BY updatedAt DESC")
    suspend fun positionsForBot(botNumber: Int): List<AmarRuntimePositionRecord>
}

@Database(
    entities = [AmarBotRuntimeRecord::class, AmarRuntimeOrderRecord::class, AmarRuntimePositionRecord::class, AmarRuntimeCommandRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AmarOperationalDatabase : RoomDatabase() {
    abstract fun dao(): AmarOperationalDao

    companion object {
        @Volatile private var instance: AmarOperationalDatabase? = null
        fun get(context: Context): AmarOperationalDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AmarOperationalDatabase::class.java, "amar_operational_runtime.db").build().also { instance = it }
        }
    }
}
