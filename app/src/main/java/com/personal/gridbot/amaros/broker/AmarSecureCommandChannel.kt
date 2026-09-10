package com.personal.gridbot.amaros.broker

import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** B31: immutable command envelope. The bridge must validate every field independently. */
data class AmarCommandEnvelope(
    val requestId: String = UUID.randomUUID().toString(),
    val idempotencyKey: String,
    val nonce: String,
    val issuedAtMs: Long,
    val expiresAtMs: Long,
    val accountLogin: Long,
    val botMagic: Long,
    val symbol: String,
    val command: AmarBrokerCommand,
    val signature: String,
) {
    init {
        require(idempotencyKey.isNotBlank())
        require(nonce.isNotBlank())
        require(expiresAtMs > issuedAtMs)
        require(accountLogin > 0)
        require(botMagic >= 0)
        require(symbol.isNotBlank())
        require(signature.isNotBlank())
    }
}

object AmarCommandSigner {
    fun canonical(envelope: AmarCommandEnvelope): String = listOf(
        envelope.requestId, envelope.idempotencyKey, envelope.nonce,
        envelope.issuedAtMs, envelope.expiresAtMs, envelope.accountLogin,
        envelope.botMagic, envelope.symbol,
        envelope.command.requestId, envelope.command.side.name,
        envelope.command.quantity, envelope.command.price ?: "",
    ).joinToString("|")

    fun hmacSha256(secret: String, value: String): String {
        require(secret.isNotBlank())
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}

enum class AmarCommandRejectReason {
    UNAUTHENTICATED, ACCOUNT_DISABLED, LIVE_LOCKED, CONNECTOR_NOT_READY,
    EXPIRED, REPLAYED, DUPLICATE, INVALID_SCOPE, INVALID_REQUEST,
}

data class AmarCommandDecision(
    val accepted: Boolean,
    val reason: AmarCommandRejectReason? = null,
    val message: String,
)

/** Defense-in-depth gate used before a command reaches any broker adapter. */
class AmarSecureCommandValidator(
    private val clockMs: () -> Long,
    private val maxFutureSkewMs: Long = 30_000L,
) {
    init { require(maxFutureSkewMs >= 0) }

    fun validate(
        envelope: AmarCommandEnvelope,
        authenticated: Boolean,
        accountEnabled: Boolean,
        explicitLiveAuthorization: Boolean,
        connectorReady: Boolean,
        expectedAccountLogin: Long,
        expectedBotMagic: Long,
        expectedSymbol: String,
        replayDetected: Boolean,
    ): AmarCommandDecision {
        if (!authenticated) return reject(AmarCommandRejectReason.UNAUTHENTICATED, "المصادقة مطلوبة")
        if (!accountEnabled) return reject(AmarCommandRejectReason.ACCOUNT_DISABLED, "الحساب معطل")
        if (!explicitLiveAuthorization) return reject(AmarCommandRejectReason.LIVE_LOCKED, "التنفيذ المباشر مقفول")
        if (!connectorReady) return reject(AmarCommandRejectReason.CONNECTOR_NOT_READY, "موصل التداول غير جاهز")
        val now = clockMs()
        if (envelope.issuedAtMs > now + maxFutureSkewMs || envelope.expiresAtMs <= now) return reject(AmarCommandRejectReason.EXPIRED, "انتهت صلاحية الأمر")
        if (replayDetected) return reject(AmarCommandRejectReason.REPLAYED, "تم رفض إعادة استخدام الأمر")
        if (envelope.accountLogin != expectedAccountLogin || envelope.botMagic != expectedBotMagic || envelope.symbol != expectedSymbol) {
            return reject(AmarCommandRejectReason.INVALID_SCOPE, "نطاق الأمر لا يطابق الحساب أو البوت أو الرمز")
        }
        if (envelope.requestId != envelope.command.requestId) return reject(AmarCommandRejectReason.INVALID_REQUEST, "معرف الأمر غير متطابق")
        return AmarCommandDecision(true, message = "تم اجتياز بوابة الأمر")
    }

    private fun reject(reason: AmarCommandRejectReason, message: String) = AmarCommandDecision(false, reason, message)
}
