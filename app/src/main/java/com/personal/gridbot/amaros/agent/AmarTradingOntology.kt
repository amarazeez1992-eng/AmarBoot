package com.personal.gridbot.amaros.agent

/** Canonical vocabulary prevents ambiguous prompts from becoming ambiguous trading data. */
enum class AmarTradingTerm {
    MARKET, SYMBOL, TIMEFRAME, OHLCV, SPREAD, COMMISSION, SLIPPAGE, LATENCY,
    LIQUIDITY, VOLATILITY, TREND, REGIME, ENTRY, EXIT, STOP_LOSS, TAKE_PROFIT,
    RISK, EQUITY, DRAWDOWN, EXPECTANCY, PROFIT_FACTOR, WIN_RATE, EXPOSURE,
    POSITION_SIZE, CORRELATION, WALK_FORWARD, OUT_OF_SAMPLE, MONTE_CARLO
}

data class AmarMarketContext(
    val symbol: String,
    val timeframe: String,
    val bid: Double? = null,
    val ask: Double? = null,
    val spreadPoints: Double? = null,
    val timestampEpochMs: Long
)
