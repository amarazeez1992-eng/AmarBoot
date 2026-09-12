package com.personal.gridbot.amaros.intelligence.trading

import org.json.JSONObject

/**
 * TradingView alert/webhook boundary. Parsing/validation only; no broker execution.
 * Designed for future server/bridge integration with replay and idempotency protection.
 */
object AmarTradingViewContract {
    data class Alert(
        val requestId: String,
        val idempotencyKey: String,
        val nonce: String,
        val issuedAtMs: Long,
        val expiresAtMs: Long,
        val symbol: String,
        val timeframe: String,
        val event: String,
        val side: String?,
        val price: Double?,
        val strategy: String?
    )

    fun parseAndValidate(raw: String, nowMs: Long): Alert {
        val j = JSONObject(raw)
        val requestId = j.getString("request_id")
        val idempotencyKey = j.getString("idempotency_key")
        val nonce = j.getString("nonce")
        val issuedAt = j.getLong("issued_at_ms")
        val expiresAt = j.getLong("expires_at_ms")
        require(requestId.isNotBlank() && idempotencyKey.isNotBlank() && nonce.isNotBlank()) { "Missing identity fields" }
        require(expiresAt > issuedAt) { "Invalid alert lifetime" }
        require(nowMs in issuedAt..expiresAt) { "Expired or not-yet-valid alert" }
        return Alert(
            requestId = requestId,
            idempotencyKey = idempotencyKey,
            nonce = nonce,
            issuedAtMs = issuedAt,
            expiresAtMs = expiresAt,
            symbol = j.getString("symbol"),
            timeframe = j.optString("timeframe", ""),
            event = j.getString("event"),
            side = j.optString("side", null),
            price = if (j.has("price")) j.optDouble("price") else null,
            strategy = j.optString("strategy", null)
        )
    }
}
