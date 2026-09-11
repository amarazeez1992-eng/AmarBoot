package com.personal.gridbot.amaros.broker

import com.google.gson.annotations.SerializedName

/** B41: broker-neutral terminal ACK returned by the authenticated bridge. */
data class AmarBot1CommandAck(
    @SerializedName("requestId") val requestId: String,
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("timestamp_ms") val timestampMs: Long = 0L,
    @SerializedName("message") val message: String? = null,
)

/** B41/B51: broker-neutral snapshot emitted by the MT5 BOT 1 wrapper. */
data class AmarBot1RemoteState(
    @SerializedName("available") val available: Boolean = false,
    @SerializedName("fresh") val fresh: Boolean = false,
    @SerializedName("age_ms") val ageMs: Long? = null,
    @SerializedName("bot_id") val botId: String? = null,
    @SerializedName("magic") val magic: Long? = null,
    @SerializedName("strategy_id") val strategyId: String? = null,
    @SerializedName("strategy_version") val strategyVersion: String? = null,
    @SerializedName("runtime_state") val runtimeState: String? = null,
    @SerializedName("target_symbol") val targetSymbol: String? = null,
    @SerializedName("chart_symbol") val chartSymbol: String? = null,
    @SerializedName("is_trading") val isTrading: Boolean = false,
    @SerializedName("buy_enabled") val buyEnabled: Boolean = false,
    @SerializedName("sell_enabled") val sellEnabled: Boolean = false,
    @SerializedName("lot_start") val lotStart: Double? = null,
    @SerializedName("grid_step") val gridStep: Double? = null,
    @SerializedName("max_orders") val maxOrders: Int? = null,
    @SerializedName("martingale") val martingale: Double? = null,
    @SerializedName("basket_tp") val basketTp: Double? = null,
    @SerializedName("basket_sl") val basketSl: Double? = null,
    @SerializedName("trailing") val trailing: Int? = null,
    @SerializedName("open_positions") val openPositions: Int = 0,
    @SerializedName("buy_positions") val buyPositions: Int = 0,
    @SerializedName("sell_positions") val sellPositions: Int = 0,
    @SerializedName("pending_orders") val pendingOrders: Int = 0,
    @SerializedName("market_ready") val marketReady: Boolean = false,
    @SerializedName("heartbeat_ms") val heartbeatMs: Long? = null,
    @SerializedName("last_request_id") val lastRequestId: String? = null,
    @SerializedName("last_command_status") val lastCommandStatus: String? = null,
    @SerializedName("last_error") val lastError: String? = null,
)

data class AmarBot1DiscoveredSymbol(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("visible") val visible: Boolean,
    @SerializedName("path") val path: String,
    @SerializedName("digits") val digits: Int,
    @SerializedName("point") val point: Double,
    @SerializedName("tradeMode") val tradeMode: Int?,
)

data class AmarBot1SymbolDiscoveryResponse(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("items") val items: List<AmarBot1DiscoveredSymbol> = emptyList(),
)
