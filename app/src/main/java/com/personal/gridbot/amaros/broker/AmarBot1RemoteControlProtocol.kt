package com.personal.gridbot.amaros.broker

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** B37/B42: broker-neutral remote BOT 1 control contract. */
enum class AmarBot1RemoteCommandType {
    START, STOP, REBUILD, CLOSE_ALL, SET_BUY_ENABLED, SET_SELL_ENABLED, UPDATE_SETTINGS
}

data class AmarBot1RemoteSettings(
    @SerializedName("lot_start") val lotStart: Double,
    @SerializedName("grid_step") val gridStep: Double,
    @SerializedName("max_orders") val maxOrders: Int,
    @SerializedName("martingale") val martingale: Double,
    @SerializedName("basket_tp") val basketTp: Double,
    @SerializedName("basket_sl") val basketSl: Double,
    @SerializedName("trailing") val trailing: Double,
    @SerializedName("buy_enabled") val buyEnabled: Boolean,
    @SerializedName("sell_enabled") val sellEnabled: Boolean,
) {
    init {
        require(lotStart.isFinite() && lotStart > 0)
        require(gridStep.isFinite() && gridStep > 0)
        require(maxOrders > 0)
        require(martingale.isFinite() && martingale > 0)
        require(basketTp.isFinite() && basketSl.isFinite())
        require(trailing.isFinite() && trailing >= 0)
    }
}

data class AmarBot1RemoteEnvelope(
    @SerializedName("request_id") val requestId: String = UUID.randomUUID().toString(),
    @SerializedName("idempotency_key") val idempotencyKey: String,
    @SerializedName("nonce") val nonce: String = UUID.randomUUID().toString(),
    @SerializedName("issued_at_ms") val issuedAtMs: Long,
    @SerializedName("expires_at_ms") val expiresAtMs: Long,
    @SerializedName("account_login") val accountLogin: Long,
    @SerializedName("bot_magic") val botMagic: Long,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("command") val command: AmarBot1RemoteCommandType,
    @SerializedName("target_symbol") val targetSymbol: String? = null,
    @SerializedName("enabled") val enabled: Boolean? = null,
    @SerializedName("settings") val settings: AmarBot1RemoteSettings? = null,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("sequence") val sequence: Long,
    @SerializedName("device_public_key") val devicePublicKey: String,
    @SerializedName("device_signature") val deviceSignature: String,
    @SerializedName("signature") val signature: String,
) {
    init {
        require(requestId.isNotBlank() && idempotencyKey.isNotBlank() && nonce.isNotBlank())
        require(expiresAtMs > issuedAtMs)
        require(accountLogin > 0 && botMagic >= 0 && symbol.isNotBlank())
        require(targetSymbol == null || targetSymbol.isNotBlank())
        require(deviceId.isNotBlank() && sequence > 0 && devicePublicKey.isNotBlank() && deviceSignature.isNotBlank())
        if (command == AmarBot1RemoteCommandType.UPDATE_SETTINGS) require(settings != null)
        if (command == AmarBot1RemoteCommandType.SET_BUY_ENABLED || command == AmarBot1RemoteCommandType.SET_SELL_ENABLED) require(enabled != null)
        require(signature.isNotBlank())
    }
}

object AmarBot1RemoteSigner {
    private fun decimal(value: Double): String = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    private fun nullable(value: String?) = value.orEmpty()

    fun canonical(envelope: AmarBot1RemoteEnvelope): String = listOf(
        envelope.requestId, envelope.idempotencyKey, envelope.nonce,
        envelope.issuedAtMs.toString(), envelope.expiresAtMs.toString(),
        envelope.accountLogin.toString(), envelope.botMagic.toString(), envelope.symbol,
        envelope.command.name, nullable(envelope.targetSymbol), envelope.enabled?.toString().orEmpty(),
        envelope.settings?.let { s -> listOf(
            decimal(s.lotStart), decimal(s.gridStep), s.maxOrders.toString(), decimal(s.martingale),
            decimal(s.basketTp), decimal(s.basketSl), decimal(s.trailing), s.buyEnabled.toString(), s.sellEnabled.toString()
        ).joinToString("|") }.orEmpty(),
        envelope.deviceId, envelope.sequence.toString(), envelope.devicePublicKey
    ).joinToString("|")

    fun deviceCanonical(envelope: AmarBot1RemoteEnvelope): String = canonical(envelope.copy(deviceSignature = "pending", signature = "pending"))

    fun hmacSha256(secret: String, value: String): String {
        require(secret.isNotBlank())
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    fun sign(unsigned: AmarBot1RemoteEnvelope, secret: String): AmarBot1RemoteEnvelope =
        unsigned.copy(signature = hmacSha256(secret, canonical(unsigned)))

    fun verify(envelope: AmarBot1RemoteEnvelope, secret: String): Boolean {
        val expected = hmacSha256(secret, canonical(envelope))
        return MessageDigest.isEqual(expected.toByteArray(Charsets.US_ASCII), envelope.signature.toByteArray(Charsets.US_ASCII))
    }
}
