package com.personal.gridbot.amaros.agent

/** Provider-neutral market-data boundary. Adapters may target MT5, CCXT or other venues. */
interface AmarMarketDataProvider {
    val id: String
    suspend fun candles(request: AmarMarketDataRequest): List<AmarMarketCandle>
    suspend fun quote(symbol: String): AmarMarketQuote?
}

data class AmarMarketDataRequest(
    val symbol: String,
    val timeframe: String,
    val limit: Int = 500
) {
    init {
        require(symbol.isNotBlank())
        require(timeframe.isNotBlank())
        require(limit in 1..100_000)
    }
}

data class AmarMarketCandle(
    val timestampEpochMs: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 0.0
) {
    init {
        require(high >= maxOf(open, close))
        require(low <= minOf(open, close))
        require(volume >= 0.0)
    }
}

data class AmarMarketQuote(
    val symbol: String,
    val bid: Double,
    val ask: Double,
    val timestampEpochMs: Long
) {
    init { require(ask >= bid) }
}

/** Explicit adapter families requested for the architecture; implementation remains external. */
enum class AmarMarketAdapterKind { MT5_PYTHON, CCXT, TRADINGVIEW_WEBHOOK, OTHER }

/** Execution is intentionally a separate capability from market-data access. */
interface AmarExecutionGateway {
    val id: String
    suspend fun validate(request: AmarExecutionRequest): AmarExecutionDecision
    suspend fun execute(request: AmarExecutionRequest): AmarExecutionResult
}

data class AmarExecutionRequest(
    val symbol: String,
    val side: AmarOrderSide,
    val quantity: Double,
    val orderType: AmarOrderType,
    val price: Double? = null,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val reason: String
) {
    init {
        require(symbol.isNotBlank())
        require(quantity > 0.0)
        require(reason.isNotBlank())
    }
}

enum class AmarOrderSide { BUY, SELL }
enum class AmarOrderType { MARKET, LIMIT, STOP }

data class AmarExecutionDecision(
    val approved: Boolean,
    val reasons: List<String> = emptyList()
)

data class AmarExecutionResult(
    val accepted: Boolean,
    val externalOrderId: String? = null,
    val message: String = ""
)

/** Hard default: no live broker/exchange execution. */
class AmarDisabledExecutionGateway : AmarExecutionGateway {
    override val id: String = "disabled"
    override suspend fun validate(request: AmarExecutionRequest) =
        AmarExecutionDecision(false, listOf("live_execution_disabled"))

    override suspend fun execute(request: AmarExecutionRequest) =
        AmarExecutionResult(false, message = "Live execution is disabled by AMAR policy.")
}
