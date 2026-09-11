package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarBot1RemoteCommandType
import com.personal.gridbot.amaros.broker.AmarBot1RemoteEnvelope
import com.personal.gridbot.amaros.broker.AmarBot1RemoteSettings
import com.personal.gridbot.amaros.broker.AmarBot1RemoteSigner
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarBot1RemoteProtocolTest {
    private fun envelope(command: AmarBot1RemoteCommandType, settings: AmarBot1RemoteSettings? = null) = AmarBot1RemoteEnvelope(
        idempotencyKey = "idem-${command.name}",
        issuedAtMs = 1_000L,
        expiresAtMs = 11_000L,
        accountLogin = 123L,
        botMagic = 20260908L,
        symbol = "XAUUSD",
        command = command,
        targetSymbol = "XAUUSD",
        settings = settings,
        deviceId = "device-test",
        sequence = 1L,
        devicePublicKey = "public-key-test",
        deviceSignature = "device-signature-test",
        signature = "pending",
    )

    @Test
    fun signedEnvelopeVerifies() {
        val signed = AmarBot1RemoteSigner.sign(envelope(AmarBot1RemoteCommandType.REBUILD), "secret")
        assertTrue(AmarBot1RemoteSigner.verify(signed, "secret"))
    }

    @Test
    fun sideCloseCommandsRemainSignable() {
        listOf(AmarBot1RemoteCommandType.CLOSE_BUY, AmarBot1RemoteCommandType.CLOSE_SELL).forEach { command ->
            val signed = AmarBot1RemoteSigner.sign(envelope(command), "secret")
            assertTrue(signed.signature.isNotBlank())
            assertTrue(AmarBot1RemoteSigner.verify(signed, "secret"))
        }
    }

    @Test
    fun settingsAreRepresentableForRemoteRebuild() {
        val settings = AmarBot1RemoteSettings(0.01, 30.0, 10, 2.0, 50.0, -30.0, 0.0, true, true)
        val signed = AmarBot1RemoteSigner.sign(envelope(AmarBot1RemoteCommandType.UPDATE_SETTINGS, settings), "secret")
        assertTrue(signed.signature.isNotBlank())
        assertTrue(signed.sequence > 0)
        assertTrue(signed.deviceId.isNotBlank())
    }
}
