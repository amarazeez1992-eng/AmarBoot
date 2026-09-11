package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarBot1RemoteCommandType
import com.personal.gridbot.amaros.broker.AmarBot1RemoteEnvelope
import com.personal.gridbot.amaros.broker.AmarBot1RemoteSettings
import com.personal.gridbot.amaros.broker.AmarBot1RemoteSigner
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarBot1RemoteProtocolTest {
    @Test
    fun signedEnvelopeVerifies() {
        val unsigned = AmarBot1RemoteEnvelope(
            idempotencyKey = "idem-1",
            issuedAtMs = 1_000L,
            expiresAtMs = 11_000L,
            accountLogin = 123L,
            botMagic = 20260908L,
            symbol = "XAUUSD",
            command = AmarBot1RemoteCommandType.REBUILD,
            targetSymbol = "XAUUSD",
            signature = "pending",
        )
        val signed = AmarBot1RemoteSigner.sign(unsigned, "secret")
        assertTrue(AmarBot1RemoteSigner.verify(signed, "secret"))
    }

    @Test
    fun settingsAreRepresentableForRemoteRebuild() {
        val settings = AmarBot1RemoteSettings(0.01, 30.0, 10, 2.0, 50.0, -30.0, 0.0, true, true)
        val unsigned = AmarBot1RemoteEnvelope(
            idempotencyKey = "idem-2",
            issuedAtMs = 1_000L,
            expiresAtMs = 11_000L,
            accountLogin = 123L,
            botMagic = 20260908L,
            symbol = "XAUUSD",
            command = AmarBot1RemoteCommandType.UPDATE_SETTINGS,
            targetSymbol = "XAUUSD",
            settings = settings,
            signature = "pending",
        )
        assertTrue(AmarBot1RemoteSigner.sign(unsigned, "secret").signature.isNotBlank())
    }
}
