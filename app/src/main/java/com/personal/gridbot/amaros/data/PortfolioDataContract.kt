package com.personal.gridbot.amaros.data

/** Read-only normalized account/position/order state. */
data class AccountSnapshot(
    val balance: Double = 0.0,
    val equity: Double = 0.0,
    val margin: Double = 0.0,
    val freeMargin: Double = 0.0,
    val peakEquity: Double = 0.0
) {
    val drawdownPercent: Double
        get() = if (peakEquity > 0.0) (((peakEquity - equity) / peakEquity) * 100.0).coerceAtLeast(0.0) else 0.0
}

data class PositionSnapshot(
    val id: String,
    val symbol: String,
    val side: String,
    val volume: Double,
    val openPrice: Double,
    val currentPrice: Double,
    val pnl: Double
)

data class OrderSnapshot(
    val id: String,
    val symbol: String,
    val side: String,
    val type: String,
    val volume: Double,
    val price: Double,
    val status: String = "PENDING"
)

data class BotRuntimeSnapshot(
    val botId: String = "AMAR_GRID",
    val enabled: Boolean = false,
    val state: String = "DEMO",
    val activeOrders: Int = 0,
    val activePositions: Int = 0
)

data class RiskSnapshot(
    val riskPercent: Double = 0.0,
    val dailyLoss: Double = 0.0,
    val maxDrawdownPercent: Double = 0.0,
    val exposure: Double = 0.0,
    val emergencyStop: Boolean = false
)
