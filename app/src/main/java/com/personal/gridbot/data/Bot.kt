package com.personal.gridbot.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bots")
data class Bot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val pair: String,              // مثال: EURUSD
    val investment: Double,        // Investment Amount
    val gridCount: Int,            // Grid Count
    val gridSpacingPips: Double = 20.0,
    val takeProfitPct: Double,     // Take Profit %
    val stopLossPct: Double,       // Stop Loss %
    val leverage: Int = 100,
    val trailingStopPips: Double = 15.0,
    val isActive: Boolean = false,
    val currentProfit: Double = 0.0,
    val profitPercent: Double = 0.0
)

@androidx.room.Entity(tableName = "bot_logs")
data class BotLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String,      // ORDER / TRAIL / ERROR / INFO
    val message: String
)
