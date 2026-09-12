package com.personal.gridbot.amaros.agent.control

/** Future control boundary for phone/computer/MT5. All capabilities are denied until explicitly enabled. */
interface AmarFutureControlGateway {
    suspend fun request(command: AmarControlCommand): AmarControlResult
}

data class AmarControlCommand(
    val target: AmarControlTarget,
    val action: String,
    val parameters: Map<String, String> = emptyMap(),
    val authorizationToken: String? = null
)

enum class AmarControlTarget { PHONE, COMPUTER, MT5, MARKET_DATA, FILE_SYSTEM }

data class AmarControlResult(val accepted: Boolean, val message: String, val auditId: String)

class AmarControlPolicy(
    val phoneEnabled: Boolean = false,
    val computerEnabled: Boolean = false,
    val mt5Enabled: Boolean = false,
    val marketDataEnabled: Boolean = false
) {
    fun allows(target: AmarControlTarget): Boolean = when (target) {
        AmarControlTarget.PHONE -> phoneEnabled
        AmarControlTarget.COMPUTER -> computerEnabled
        AmarControlTarget.MT5 -> mt5Enabled
        AmarControlTarget.MARKET_DATA -> marketDataEnabled
        AmarControlTarget.FILE_SYSTEM -> false
    }
}
