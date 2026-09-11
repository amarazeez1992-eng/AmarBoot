package com.personal.gridbot.amaros.broker

import com.google.gson.annotations.SerializedName

/** B38: terminal-verified BOT 1 command lifecycle state. */
data class AmarBot1CommandStatus(
    @SerializedName("request_id") val requestId: String,
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("accepted") val accepted: Boolean = false,
    @SerializedName("timestamp_ms") val timestampMs: Long = 0L,
    @SerializedName("message") val message: String = "",
) {
    val isVerified: Boolean get() = status == "VERIFIED" && accepted
    val isFailed: Boolean get() = status == "FAILED" || (!accepted && status != "PENDING")
}
