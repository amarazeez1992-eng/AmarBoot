package com.personal.gridbot.amaros.simulation

import com.personal.gridbot.amaros.core.AmarOperatingMode
import kotlin.math.abs

/** B22: deterministic paper simulation. No network, credentials or broker execution. */
class AmarSimulationEngine {
    data class Candle(val epochMs: Long, val open: Double, val high: Double, val low: Double, val close: Double, val volume: Double = 0.0)
    data class Order(val id: String, val symbol: String, val side: Side, val quantity: Double, val entry: Double, val stopLoss: Double? = null, val takeProfit: Double? = null)
    enum class Side { BUY, SELL }
    data class Fill(val orderId: String, val price: Double, val quantity: Double, val profit: Double)
    data class Result(val mode: AmarOperatingMode, val fills: List<Fill>, val endingBalance: Double, val processedCandles: Int)

    fun run(initialBalance: Double, candles: List<Candle>, orders: List<Order>, commissionPerUnit: Double = 0.0): Result {
        require(initialBalance >= 0.0)
        require(commissionPerUnit >= 0.0)
        val active = orders.associateBy { it.id }
        val fills = mutableListOf<Fill>()
        var balance = initialBalance
        for (candle in candles) {
            for (order in active.values) {
                if (fills.any { it.orderId == order.id }) continue
                val touched = when (order.side) {
                    Side.BUY -> candle.low <= order.entry
                    Side.SELL -> candle.high >= order.entry
                }
                if (!touched) continue
                val profit = when (order.side) {
                    Side.BUY -> (candle.close - order.entry) * order.quantity
                    Side.SELL -> (order.entry - candle.close) * order.quantity
                } - abs(order.quantity) * commissionPerUnit
                balance += profit
                fills += Fill(order.id, order.entry, order.quantity, profit)
            }
        }
        return Result(AmarOperatingMode.SIMULATION, fills.toList(), balance, candles.size)
    }
}
