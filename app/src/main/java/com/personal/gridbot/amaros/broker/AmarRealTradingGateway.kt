package com.personal.gridbot.amaros.broker

import com.personal.gridbot.amaros.accounts.AmarAccountProfile
import com.personal.gridbot.amaros.chart.AmarCandle
import com.personal.gridbot.amaros.chart.AmarMarketDataProvider
import com.personal.gridbot.amaros.chart.AmarTimeframe

/**
 * حد التنفيذ الحقيقي.
 * التطبيق لا يفترض بروتوكول وسيط بعينه؛ يمكن ربطه بواجهة برمجية أو بجسر منصة التداول.
 */
interface AmarRealTradingGateway {
    suspend fun authenticate(account: AmarAccountProfile, password: CharArray): AmarGatewaySession
    suspend fun account(session: AmarGatewaySession): AmarBrokerAccount
    suspend fun submit(session: AmarGatewaySession, command: AmarBrokerCommand): AmarBrokerResult
    suspend fun candles(session: AmarGatewaySession, symbol: String, timeframe: AmarTimeframe): List<AmarCandle>
    suspend fun disconnect(session: AmarGatewaySession)
}

data class AmarGatewaySession(
    val id: String,
    val authenticated: Boolean,
    val liveExecutionAuthorized: Boolean,
)

enum class AmarGatewayKind { BROKER_API, MT5_BRIDGE }

/** موصل حقيقي لا يعمل إلا بعد مصادقة ناجحة وتصريح تنفيذ مباشر من البوابة. */
class AmarAuthorizedLiveBrokerAdapter(
    private val gateway: AmarRealTradingGateway,
    private val accountProfile: AmarAccountProfile,
    private val passwordProvider: suspend () -> CharArray,
) {
    private var session: AmarGatewaySession? = null

    suspend fun connect(): AmarBrokerAccount {
        val password = passwordProvider()
        try {
            val authenticated = gateway.authenticate(accountProfile, password)
            check(authenticated.authenticated) { "فشل التحقق من بيانات الحساب" }
            session = authenticated
            return gateway.account(authenticated)
        } finally {
            password.fill('\u0000')
        }
    }

    suspend fun submit(command: AmarBrokerCommand): AmarBrokerResult {
        val activeSession = session ?: return AmarBrokerResult(false, false, command.requestId, "لا يوجد اتصال موثق")
        if (!activeSession.authenticated || !activeSession.liveExecutionAuthorized || !accountProfile.enabled) {
            return AmarBrokerResult(false, false, command.requestId, "تم رفض التنفيذ المباشر بسبب بوابة الأمان")
        }
        return gateway.submit(activeSession, command)
    }

    suspend fun disconnect() {
        session?.let { gateway.disconnect(it) }
        session = null
    }

    fun marketDataProvider(): AmarMarketDataProvider = object : AmarMarketDataProvider {
        override fun candles(symbol: String, timeframe: AmarTimeframe): List<AmarCandle> = emptyList()
    }
}
