package com.personal.gridbot.amaros.agent

/** Stage 4 contracts: deterministic simulation, risk controls, crisis detection and audit. */
enum class AmarSignalDirection { LONG, SHORT, FLAT }

data class AmarStrategySignal(
    val timestampEpochMs: Long,
    val direction: AmarSignalDirection,
    val confidence: Double = 1.0
) {
    init { require(confidence in 0.0..1.0) }
}

data class AmarSimulationConfig(
    val initialEquity: Double,
    val quantity: Double,
    val feePerTrade: Double = 0.0,
    val slippagePerUnit: Double = 0.0,
    val maxBars: Int = 100_000
) {
    init {
        require(initialEquity > 0.0)
        require(quantity > 0.0)
        require(feePerTrade >= 0.0)
        require(slippagePerUnit >= 0.0)
        require(maxBars in 1..100_000)
    }
}

data class AmarSimulationTrade(
    val entryTimestampEpochMs: Long,
    val exitTimestampEpochMs: Long,
    val direction: AmarSignalDirection,
    val entryPrice: Double,
    val exitPrice: Double,
    val quantity: Double,
    val grossPnl: Double,
    val fees: Double,
    val netPnl: Double
)

data class AmarSimulationResult(
    val initialEquity: Double,
    val finalEquity: Double,
    val trades: List<AmarSimulationTrade>,
    val maxDrawdown: Double,
    val winRate: Double,
    val profitFactor: Double,
    val completed: Boolean,
    val issues: List<String> = emptyList()
)

data class AmarRiskLimits(
    val maxRiskPerTradeFraction: Double = 0.02,
    val maxDailyLossFraction: Double = 0.05,
    val maxDrawdownFraction: Double = 0.15,
    val maxOpenPositions: Int = 1,
    val minimumEquity: Double = 0.0
) {
    init {
        require(maxRiskPerTradeFraction in 0.0..1.0)
        require(maxDailyLossFraction in 0.0..1.0)
        require(maxDrawdownFraction in 0.0..1.0)
        require(maxOpenPositions >= 0)
        require(minimumEquity >= 0.0)
    }
}

data class AmarRiskSnapshot(
    val equity: Double,
    val initialEquity: Double,
    val openPositions: Int,
    val dailyPnl: Double,
    val peakEquity: Double
)

data class AmarRiskDecision(
    val approved: Boolean,
    val reasons: List<String> = emptyList()
)

data class AmarCrisisLimits(
    val maxSpreadFraction: Double = 0.005,
    val maxBarRangeFraction: Double = 0.03,
    val maxGapFraction: Double = 0.05,
    val maxDataAgeMs: Long = 120_000L
) {
    init {
        require(maxSpreadFraction >= 0.0)
        require(maxBarRangeFraction >= 0.0)
        require(maxGapFraction >= 0.0)
        require(maxDataAgeMs >= 0L)
    }
}

data class AmarCrisisState(
    val active: Boolean,
    val reasons: List<String> = emptyList()
)

data class AmarAuditEvent(
    val sequence: Long,
    val timestampEpochMs: Long,
    val actor: String,
    val action: String,
    val decision: String,
    val reason: String,
    val previousHash: String,
    val hash: String
)
