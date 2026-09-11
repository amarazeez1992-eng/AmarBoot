package com.personal.gridbot.amaros.broker

import java.math.BigDecimal
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

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
        require(idempotencyKey.isNotBlank());require(nonce.isNotBlank());require(expiresAtMs>issuedAtMs)
        require(accountLogin>0);require(botMagic>=0);require(symbol.isNotBlank());require(signature.isNotBlank())
    }
}

object AmarCommandSigner {
    private fun decimal(value: Double): String=BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    fun canonical(envelope: AmarCommandEnvelope): String=listOf(
        envelope.requestId,envelope.idempotencyKey,envelope.nonce,envelope.issuedAtMs.toString(),envelope.expiresAtMs.toString(),
        envelope.accountLogin.toString(),envelope.botMagic.toString(),envelope.symbol,envelope.command.requestId,
        envelope.command.side.name,decimal(envelope.command.quantity),envelope.command.price?.let(::decimal).orEmpty()
    ).joinToString("|")
    fun hmacSha256(secret:String,value:String):String{
        require(secret.isNotBlank());val mac=Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8),"HmacSHA256"))
        return mac.doFinal(value.toByteArray(Charsets.UTF_8)).joinToString(""){ "%02x".format(it) }
    }
    fun verify(envelope:AmarCommandEnvelope,secret:String):Boolean{
        val expected=hmacSha256(secret,canonical(envelope))
        return MessageDigest.isEqual(expected.toByteArray(Charsets.US_ASCII),envelope.signature.toByteArray(Charsets.US_ASCII))
    }
}

enum class AmarCommandRejectReason { UNAUTHENTICATED,ACCOUNT_DISABLED,LIVE_LOCKED,CONNECTOR_NOT_READY,EXPIRED,REPLAYED,DUPLICATE,INVALID_SCOPE,INVALID_REQUEST,INVALID_SIGNATURE }
data class AmarCommandDecision(val accepted:Boolean,val reason:AmarCommandRejectReason?=null,val message:String)

class AmarSecureCommandValidator(private val clockMs:()->Long,private val maxFutureSkewMs:Long=30_000L){
    init{require(maxFutureSkewMs>=0)}
    fun validate(
        envelope:AmarCommandEnvelope,authenticated:Boolean,accountEnabled:Boolean,explicitLiveAuthorization:Boolean,connectorReady:Boolean,
        expectedAccountLogin:Long,expectedBotMagic:Long,expectedSymbol:String,replayDetected:Boolean,signatureSecret:String?=null
    ):AmarCommandDecision{
        if(!authenticated)return reject(AmarCommandRejectReason.UNAUTHENTICATED,"المصادقة مطلوبة")
        if(!accountEnabled)return reject(AmarCommandRejectReason.ACCOUNT_DISABLED,"الحساب معطل")
        if(!explicitLiveAuthorization)return reject(AmarCommandRejectReason.LIVE_LOCKED,"التنفيذ المباشر مقفول")
        if(!connectorReady)return reject(AmarCommandRejectReason.CONNECTOR_NOT_READY,"موصل التداول غير جاهز")
        val now=clockMs()
        if(envelope.issuedAtMs>now+maxFutureSkewMs||envelope.expiresAtMs<=now)return reject(AmarCommandRejectReason.EXPIRED,"انتهت صلاحية الأمر")
        if(replayDetected)return reject(AmarCommandRejectReason.REPLAYED,"تم رفض إعادة استخدام الأمر")
        if(envelope.accountLogin!=expectedAccountLogin||envelope.botMagic!=expectedBotMagic||envelope.symbol!=expectedSymbol)return reject(AmarCommandRejectReason.INVALID_SCOPE,"نطاق الأمر لا يطابق الحساب أو البوت أو الرمز")
        if(envelope.requestId!=envelope.command.requestId)return reject(AmarCommandRejectReason.INVALID_REQUEST,"معرف الأمر غير متطابق")
        if(signatureSecret!=null&&!AmarCommandSigner.verify(envelope,signatureSecret))return reject(AmarCommandRejectReason.INVALID_SIGNATURE,"توقيع الأمر غير صالح")
        return AmarCommandDecision(true,message="تم اجتياز بوابة الأمر")
    }
    private fun reject(reason:AmarCommandRejectReason,message:String)=AmarCommandDecision(false,reason,message)
}
