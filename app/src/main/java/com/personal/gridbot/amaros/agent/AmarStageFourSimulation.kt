package com.personal.gridbot.amaros.agent

/** Deterministic close-on-reversal simulator. It never reaches a broker. */
class AmarStageFourSimulationEngine {
    fun run(
        candles: List<AmarMarketCandle>,
        signals: List<AmarStrategySignal>,
        config: AmarSimulationConfig
    ): AmarSimulationResult {
        if (candles.isEmpty()) return emptyResult(config, "no_candles")
        if (candles.size > config.maxBars) return emptyResult(config, "bar_limit_exceeded")
        val ordered = candles.sortedBy { it.timestampEpochMs }
        val signalByTime = signals.associateBy { it.timestampEpochMs }
        var equity = config.initialEquity
        var peak = equity
        var maxDrawdown = 0.0
        var open: OpenTrade? = null
        val trades = mutableListOf<AmarSimulationTrade>()
        val issues = mutableListOf<String>()

        for (bar in ordered) {
            val signal = signalByTime[bar.timestampEpochMs] ?: continue
            if (open == null && signal.direction != AmarSignalDirection.FLAT) {
                val entry = executionPrice(bar.close, signal.direction, config.slippagePerUnit)
                open = OpenTrade(bar.timestampEpochMs, signal.direction, entry)
                continue
            }
            if (open != null && (signal.direction == AmarSignalDirection.FLAT || signal.direction != open.direction)) {
                val exit = executionPrice(bar.close, open.direction.opposite(), config.slippagePerUnit)
                val gross = pnl(open.direction, open.entryPrice, exit, config.quantity)
                val fees = config.feePerTrade
                val net = gross - fees
                equity += net
                trades += AmarSimulationTrade(open.entryTime, bar.timestampEpochMs, open.direction, open.entryPrice, exit, config.quantity, gross, fees, net)
                peak = maxOf(peak, equity)
                maxDrawdown = maxOf(maxDrawdown, (peak - equity).coerceAtLeast(0.0))
                open = if (signal.direction == AmarSignalDirection.FLAT) null else {
                    val entry = executionPrice(bar.close, signal.direction, config.slippagePerUnit)
                    OpenTrade(bar.timestampEpochMs, signal.direction, entry)
                }
            }
        }
        if (open != null) issues += "open_position_not_closed_by_data"
        val wins = trades.count { it.netPnl > 0.0 }
        val grossProfit = trades.filter { it.netPnl > 0.0 }.sumOf { it.netPnl }
        val grossLoss = trades.filter { it.netPnl < 0.0 }.sumOf { -it.netPnl }
        return AmarSimulationResult(
            config.initialEquity,
            equity,
            trades,
            maxDrawdown,
            if (trades.isEmpty()) 0.0 else wins.toDouble() / trades.size,
            if (grossLoss == 0.0) if (grossProfit > 0.0) Double.POSITIVE_INFINITY else 0.0 else grossProfit / grossLoss,
            issues.isEmpty(),
            issues.distinct()
        )
    }

    private fun emptyResult(config: AmarSimulationConfig, issue: String) = AmarSimulationResult(config.initialEquity, config.initialEquity, emptyList(), 0.0, 0.0, 0.0, false, listOf(issue))

    private fun executionPrice(close: Double, direction: AmarSignalDirection, slippage: Double): Double =
        close + if (direction == AmarSignalDirection.LONG) slippage else -slippage

    private fun pnl(direction: AmarSignalDirection, entry: Double, exit: Double, quantity: Double): Double = when (direction) {
        AmarSignalDirection.LONG -> (exit - entry) * quantity
        AmarSignalDirection.SHORT -> (entry - exit) * quantity
        AmarSignalDirection.FLAT -> 0.0
    }

    private data class OpenTrade(val entryTime: Long, val direction: AmarSignalDirection, val entryPrice: Double)

    private fun AmarSignalDirection.opposite() = when (this) {
        AmarSignalDirection.LONG -> AmarSignalDirection.SHORT
        AmarSignalDirection.SHORT -> AmarSignalDirection.LONG
        AmarSignalDirection.FLAT -> AmarSignalDirection.FLAT
    }
}
