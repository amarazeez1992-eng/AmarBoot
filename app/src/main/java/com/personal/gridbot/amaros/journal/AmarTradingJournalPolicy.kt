package com.personal.gridbot.amaros.journal

/** Independent trading-journal boundary. Read-only trade statistics; no execution. */
object AmarTradingJournalPolicy {
    data class TradeRecord(
        val id: String,
        val strategy: String,
        val profitLoss: Double,
        val riskReward: Double,
        val durationMinutes: Long,
    )

    data class Summary(
        val trades: Int,
        val wins: Int,
        val losses: Int,
        val winRate: Double,
        val netProfitLoss: Double,
        val averageProfitLoss: Double,
        val profitFactor: Double,
        val averageRiskReward: Double,
    )

    fun validate(trades: List<TradeRecord>): List<String> {
        val errors = mutableListOf<String>()
        val seenIds = mutableSetOf<String>()
        trades.forEachIndexed { index, trade ->
            val normalizedId = trade.id.trim()
            if (normalizedId.isEmpty()) {
                errors += "TRADE_${index}_ID_INVALID"
            } else if (!seenIds.add(normalizedId)) {
                errors += "TRADE_${index}_ID_DUPLICATE"
            }
            if (trade.strategy.isBlank()) errors += "TRADE_${index}_STRATEGY_INVALID"
            if (!trade.profitLoss.isFinite()) errors += "TRADE_${index}_PNL_INVALID"
            if (!trade.riskReward.isFinite() || trade.riskReward < 0.0) errors += "TRADE_${index}_RR_INVALID"
            if (trade.durationMinutes < 0L) errors += "TRADE_${index}_DURATION_INVALID"
        }
        return errors
    }

    fun summarize(trades: List<TradeRecord>): Summary {
        require(validate(trades).isEmpty())
        val wins = trades.count { it.profitLoss > 0.0 }
        val losses = trades.count { it.profitLoss < 0.0 }
        val grossProfit = trades.filter { it.profitLoss > 0.0 }.sumOf { it.profitLoss }
        val grossLoss = trades.filter { it.profitLoss < 0.0 }.sumOf { -it.profitLoss }
        return Summary(
            trades = trades.size,
            wins = wins,
            losses = losses,
            winRate = if (trades.isEmpty()) 0.0 else wins.toDouble() / trades.size,
            netProfitLoss = trades.sumOf { it.profitLoss },
            averageProfitLoss = if (trades.isEmpty()) 0.0 else trades.sumOf { it.profitLoss } / trades.size,
            profitFactor = if (grossLoss == 0.0) Double.POSITIVE_INFINITY else grossProfit / grossLoss,
            averageRiskReward = if (trades.isEmpty()) 0.0 else trades.sumOf { it.riskReward } / trades.size,
        )
    }
}
