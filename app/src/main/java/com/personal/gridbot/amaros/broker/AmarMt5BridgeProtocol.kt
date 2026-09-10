package com.personal.gridbot.amaros.broker

/** B26/B30: protocol shared by the Android client and the self-hosted MT5 bridge. */
data class AmarBridgeConfig(
    val baseUrl: String,
    val token: String,
    val connectTimeoutMs: Long = 5_000L,
    val readTimeoutMs: Long = 5_000L,
) {
    init {
        require(baseUrl.startsWith("https://")) { "جسر MT5 يجب أن يستخدم HTTPS" }
        require(token.isNotBlank()) { "رمز الجسر مطلوب" }
        require(connectTimeoutMs > 0 && readTimeoutMs > 0)
    }
}

data class AmarMt5Health(val connected: Boolean, val terminal: String, val message: String)

data class AmarMt5AccountSnapshot(
    val login: Long,
    val server: String,
    val currency: String,
    val balance: Double,
    val equity: Double,
    val margin: Double,
    val connected: Boolean,
)

data class AmarMt5MarketSnapshot(
    val symbol: String,
    val bid: Double,
    val ask: Double,
    val spreadPoints: Double,
    val timestampMs: Long,
)

data class AmarMt5Candle(
    val timestampMs: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val tickVolume: Long,
)

data class AmarMt5CandleSeries(
    val ok: Boolean,
    val symbol: String,
    val timeframe: String,
    val items: List<AmarMt5Candle>,
)

data class AmarMt5BridgeStatus(val health: AmarMt5Health, val account: AmarMt5AccountSnapshot?)

enum class AmarMt5ReadOperation { HEALTH, ACCOUNT, MARKET, CANDLES, POSITIONS, PENDING_ORDERS, BOT_STATUS }
en
enum class AmarLiveGateState { LOCKED, AUTHENTICATED_READ_ONLY, AUTHORIZED_LIVE }

/** B28: execution stays blocked unless every explicit gate is satisfied. */
class AmarLiveExecutionGate {
    fun state(authenticated: Boolean, accountEnabled: Boolean, explicitLiveAuthorization: Boolean, connectorReady: Boolean): AmarLiveGateState = when {
        !authenticated || !accountEnabled -> AmarLiveGateState.LOCKED
        !explicitLiveAuthorization || !connectorReady -> AmarLiveGateState.AUTHENTICATED_READ_ONLY
        else -> AmarLiveGateState.AUTHORIZED_LIVE
    }

    fun allowExecution(state: AmarLiveGateState): Boolean = state == AmarLiveGateState.AUTHORIZED_LIVE
}
