package com.personal.gridbot.amaros.broker

import com.personal.gridbot.amaros.core.AmarOperatingMode

/** B25: broker boundary. Implementations are isolated behind contracts and safety gates. */
interface AmarBrokerAdapter {
    fun capabilities(): AmarBrokerCapabilities
    fun account(): AmarBrokerAccount
    fun submit(command: AmarBrokerCommand): AmarBrokerResult
}

data class AmarBrokerCapabilities(val brokerName: String, val supportsOrders: Boolean, val supportsPositions: Boolean, val supportsModification: Boolean)
data class AmarBrokerAccount(val currency: String, val balance: Double, val equity: Double, val connected: Boolean)
data class AmarBrokerCommand(val requestId: String, val symbol: String, val side: Side, val quantity: Double, val price: Double? = null)
enum class Side { BUY, SELL }
data class AmarBrokerResult(val accepted: Boolean, val executed: Boolean, val requestId: String, val message: String)

/** Safe adapter for the current phase. It deliberately cannot execute. */
class DemoBrokerAdapter : AmarBrokerAdapter {
    override fun capabilities() = AmarBrokerCapabilities("محرك تجريبي", false, true, false)
    override fun account() = AmarBrokerAccount("USD", 10_000.0, 10_000.0, connected = false)
    override fun submit(command: AmarBrokerCommand): AmarBrokerResult = AmarBrokerResult(
        accepted = false,
        executed = false,
        requestId = command.requestId,
        message = "تم حظر التنفيذ: الموصل التجريبي لا ينفذ أوامر"
    )
}

/** Future live adapter shell. Live execution is explicitly rejected until a later approved stage. */
class BlockedLiveBrokerAdapter : AmarBrokerAdapter {
    override fun capabilities() = AmarBrokerCapabilities("موصل مستقبلي", false, false, false)
    override fun account() = AmarBrokerAccount("USD", 0.0, 0.0, connected = false)
    override fun submit(command: AmarBrokerCommand): AmarBrokerResult = AmarBrokerResult(
        accepted = false,
        executed = false,
        requestId = command.requestId,
        message = "التداول الحقيقي غير متاح في هذه المرحلة: الوضع ${AmarOperatingMode.LIVE} محظور"
    )
}
