package com.personal.gridbot.amaros.bots

import com.personal.gridbot.amaros.broker.AmarBrokerCommand
import com.personal.gridbot.amaros.broker.AmarBrokerResult
import com.personal.gridbot.amaros.broker.AmarMt5CommandClient
import com.personal.gridbot.amaros.broker.AmarSecureCommandValidator
import com.personal.gridbot.amaros.broker.Side
import java.util.UUID

/** BOT 1 execution boundary. UI never talks to MT5 directly. */
class AmarBot1ExecutionController(
    private val commandClient: AmarMt5CommandClient,
    private val accountLogin: Long,
    private val botMagic: Long = BOT1_MAGIC,
    private val symbol: String,
    private val authenticated: () -> Boolean,
    private val accountEnabled: () -> Boolean,
    private val explicitLiveAuthorization: () -> Boolean,
    private val connectorReady: () -> Boolean,
    private val clockMs: () -> Long = { System.currentTimeMillis() },
) {
    init {
        require(accountLogin > 0)
        require(botMagic >= 0)
        require(symbol.isNotBlank())
    }

    suspend fun buy(quantity: Double, idempotencyKey: String = UUID.randomUUID().toString()): AmarBrokerResult =
        submit(Side.BUY, quantity, idempotencyKey)

    suspend fun sell(quantity: Double, idempotencyKey: String = UUID.randomUUID().toString()): AmarBrokerResult =
        submit(Side.SELL, quantity, idempotencyKey)

    private suspend fun submit(side: Side, quantity: Double, idempotencyKey: String): AmarBrokerResult {
        if (!quantity.isFinite() || quantity <= 0.0) {
            return AmarBrokerResult(false, false, idempotencyKey, "حجم التداول غير صالح")
        }
        val requestId = UUID.randomUUID().toString()
        val command = AmarBrokerCommand(requestId, symbol, side, quantity)
        val now = clockMs()
        val envelope = com.personal.gridbot.amaros.broker.AmarCommandEnvelope(
            requestId = requestId,
            idempotencyKey = idempotencyKey,
            nonce = UUID.randomUUID().toString(),
            issuedAtMs = now,
            expiresAtMs = now + 15_000L,
            accountLogin = accountLogin,
            botMagic = botMagic,
            symbol = symbol,
            command = command,
            signature = "pending",
        )
        val decision = AmarSecureCommandValidator(clockMs).validate(
            envelope, authenticated(), accountEnabled(), explicitLiveAuthorization(), connectorReady(),
            accountLogin, botMagic, symbol, replayDetected = false,
        )
        if (!decision.accepted) return AmarBrokerResult(false, false, requestId, decision.message)
        return commandClient.submit(accountLogin, botMagic, symbol, command, idempotencyKey = idempotencyKey)
    }

    companion object { const val BOT1_MAGIC = 20260908L }
}
