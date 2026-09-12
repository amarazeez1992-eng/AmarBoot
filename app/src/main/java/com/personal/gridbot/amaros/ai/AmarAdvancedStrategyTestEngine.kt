package com.personal.gridbot.amaros.ai

import kotlin.math.abs
import kotlin.math.sqrt

/** Deterministic historical/forward evaluator. It reports measured results only. */
object AmarAdvancedStrategyTestEngine {
    data class Candle(val timeMs: Long, val open: Double, val high: Double, val low: Double, val close: Double)
    enum class Side { BUY, SELL }
    data class Signal(val index: Int, val side: Side, val entry: Double, val stopLoss: Double, val takeProfit: Double)
    data class Trade(val side: Side, val entry: Double, val exit: Double, val pnlR: Double, val barsHeld: Int, val win: Boolean)
    data class Report(
        val trades: List<Trade>,
        val winRatePct: Double,
        val profitFactor: Double,
        val expectancyR: Double,
        val maxDrawdownR: Double,
        val netR: Double,
        val averageBarsHeld: Double,
        val sampleSize: Int,
        val verified: Boolean
    )

    fun evaluate(candles: List<Candle>, signals: List<Signal>, maxBarsHeld: Int = 100): Report {
        require(maxBarsHeld > 0)
        val trades = signals.sortedBy { it.index }.mapNotNull { signal ->
            if (signal.index !in candles.indices) return@mapNotNull null
            val risk = abs(signal.entry - signal.stopLoss)
            if (risk <= 0.0 || signal.takeProfit == signal.entry) return@mapNotNull null
            var exitIndex = -1
            var exitPrice = 0.0
            val last = minOf(candles.lastIndex, signal.index + maxBarsHeld)
            for (i in signal.index + 1..last) {
                val c = candles[i]
                val hitSl = if (signal.side == Side.BUY) c.low <= signal.stopLoss else c.high >= signal.stopLoss
                val hitTp = if (signal.side == Side.BUY) c.high >= signal.takeProfit else c.low <= signal.takeProfit
                if (hitSl && hitTp) {
                    exitIndex = i
                    exitPrice = signal.stopLoss
                    break
                }
                if (hitSl) { exitIndex = i; exitPrice = signal.stopLoss; break }
                if (hitTp) { exitIndex = i; exitPrice = signal.takeProfit; break }
            }
            if (exitIndex < 0) {
                exitIndex = last
                exitPrice = candles[last].close
            }
            val pnlR = if (signal.side == Side.BUY) (exitPrice - signal.entry) / risk else (signal.entry - exitPrice) / risk
            Trade(signal.side, signal.entry, exitPrice, pnlR, exitIndex - signal.index, pnlR > 0.0)
        }
        var equity = 0.0
        var peak = 0.0
        var maxDd = 0.0
        trades.forEach { t -> equity += t.pnlR; peak = maxOf(peak, equity); maxDd = maxOf(maxDd, peak - equity) }
        val grossWin = trades.filter { it.pnlR > 0 }.sumOf { it.pnlR }
        val grossLoss = -trades.filter { it.pnlR < 0 }.sumOf { it.pnlR }
        val pf = if (grossLoss > 0) grossWin / grossLoss else if (grossWin > 0) Double.POSITIVE_INFINITY else 0.0
        val net = trades.sumOf { it.pnlR }
        val avgBars = trades.map { it.barsHeld }.average().takeUnless { it.isNaN() } ?: 0.0
        return Report(
            trades = trades,
            winRatePct = if (trades.isEmpty()) 0.0 else trades.count { it.win } * 100.0 / trades.size,
            profitFactor = pf,
            expectancyR = if (trades.isEmpty()) 0.0 else net / trades.size,
            maxDrawdownR = maxDd,
            netR = net,
            averageBarsHeld = avgBars,
            sampleSize = trades.size,
            verified = trades.isNotEmpty()
        )
    }
}
