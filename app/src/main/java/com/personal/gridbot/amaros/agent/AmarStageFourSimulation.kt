package com.personal.gridbot.amaros.agent

/** Deterministic close-on-reversal simulator. It never reaches a broker and fails closed on malformed or non-finite state. */
class AmarStageFourSimulationEngine {
    fun run(
        candles: List<AmarMarketCandle>,
        signals: List<AmarStrategySignal>,
        config: AmarSimulationConfig
    ): AmarSimulationResult {
        if (candles.isEmpty()) return emptyResult(config, "no_candles")
        if (candles.size > config.maxBars) return emptyResult(config, "bar_limit_exceeded")
        if (candles.any { !validCandle(it) }) return emptyResult(config, "invalid_candle")
        if (candles.map { it.timestampEpochMs }.distinct().size != candles.size) return emptyResult(config, "duplicate_candle_timestamp")
        if (signals.map { it.timestampEpochMs }.distinct().size != signals.size) return emptyResult(config, "duplicate_signal_timestamp")

        val ordered = candles.sortedBy { it.timestampEpochMs }
        val candleTimes = ordered.mapTo(mutableSetOf()) { it.timestampEpochMs }
        if (signals.any { it.timestampEpochMs !in candleTimes }) return emptyResult(config, "signal_without_candle")

        var equity = config.initialEquity
        var peak = equity
        var maxDrawdown = 0.0
        var open: OpenTrade? = null
        val trades = mutableListOf<AmarSimulationTrade>()
        val issues = mutableListOf<String>()
        val signalByTime = signals.associateBy { it.timestampEpochMs }

        for (bar in ordered) {
            val signal = signalByTime[bar.timestampEpochMs] ?: continue
            if (open == null && signal.direction != AmarSignalDirection.FLAT) {
                val entry = executionPrice(bar.close, signal.direction, config.slippagePerUnit)
                if (!validPrice(entry)) return emptyResult(config, "invalid_execution_price")
                open = OpenTrade(bar.timestampEpochMs, signal.direction, entry)
                continue
            }

            if (open != null && (signal.direction == AmarSignalDirection.FLAT || signal.direction != open.direction)) {
                val exit = executionPrice(bar.close, open.direction.opposite(), config.slippagePerUnit)
                if (!validPrice(exit)) return emptyResult(config, "invalid_execution_price")
                val gross = pnl(open.direction, open.entryPrice, exit, config.quantity)
                val fees = config.feePerTrade
                val net = gross - fees
                if (!gross.isFinite() || !net.isFinite()) return emptyResult(config, "non_finite_pnl")
                val nextEquity = equity + net
                if (!nextEquity.isFinite()) return emptyResult(config, "non_finite_equity")
                equity = nextEquity
                trades += AmarSimulationTrade(
                    open.entryTime, bar.timestampEpochMs, open.direction,
                    open.entryPrice, exit, config.quantity, gross, fees, net
                )
                peak = maxOf(peak, equity)
                val drawdown = (peak - equity).coerceAtLeast(0.0)
                if (!drawdown.isFinite()) return emptyResult(config, "non_finite_drawdown")
                maxDrawdown = maxOf(maxDrawdown, drawdown)

                open = if (signal.direction == AmarSignalDirection.FLAT) null else {
                    val entry = executionPrice(bar.close, signal.direction, config.slippagePerUnit)
                    if (!validPrice(entry)) return emptyResult(config, "invalid_execution_price")
                    OpenTrade(bar.timestampEpochMs, signal.direction, entry)
                }
            }
        }

        if (open != null) issues += "open_position_not_closed_by_data"
        val wins = trades.count { it.netPnl > 0.0 }
        val grossProfit = trades.filter { it.netPnl > 0.0 }.sumOf { it.netPnl }
        val grossLoss = trades.filter { it.netPnl < 0.0 }.sumOf { -it.netPnl }
        val profitFactor = when {
            grossLoss == 0.0 && grossProfit > 0.0 -> Double.POSITIVE_INFINITY
            grossLoss == 0.0 -> 0.0
            else -> grossProfit / grossLoss
        }
        if (!profitFactor.isFinite() && profitFactor != Double.POSITIVE_INFINITY) return emptyResult(config, "non_finite_profit_factor")

        return AmarSimulationResult(
            config.initialEquity,
            equity,
            trades.toList(),
            maxDrawdown,
            if (trades.isEmpty()) 0.0 else wins.toDouble() / trades.size,
            profitFactor,
            issues.isEmpty(),
            issues.distinct()
        )
    }

    private fun validCandle(candle: AmarMarketCandle): Boolean {
        val values = listOf(candle.open, candle.high, candle.low, candle.close)
        return candle.timestampEpochMs >= 0L &&
            values.all { it.isFinite() && it > 0.0 } &&
            candle.high >= candle.low &&
            candle.high >= maxOf(candle.open, candle.close) &&
            candle.low <= minOf(candle.open, candle.close)
    }

    private fun validPrice(price: Double): Boolean = price.isFinite() && price > 0.0

    private fun emptyResult(config: AmarSimulationConfig, issue: String) =
        AmarSimulationResult(config.initialEquity, config.initialEquity, emptyList(), 0.0, 0.0, 0.0, false, listOf(issue))

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
