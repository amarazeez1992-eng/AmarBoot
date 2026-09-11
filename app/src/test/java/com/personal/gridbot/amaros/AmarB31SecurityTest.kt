package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarBrokerCommand
import com.personal.gridbot.amaros.broker.AmarCommandEnvelope
import com.personal.gridbot.amaros.broker.AmarCommandRejectReason
import com.personal.gridbot.amaros.broker.AmarCommandSigner
import com.personal.gridbot.amaros.broker.AmarSecureCommandValidator
import com.personal.gridbot.amaros.broker.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarB31SecurityTest {
    private fun envelope(now: Long = 1_000L): AmarCommandEnvelope {
        val command = AmarBrokerCommand("r1", "XAUUSD", Side.BUY, 0.01)
        val unsigned = AmarCommandEnvelope("r1", "id1", "nonce1", now, now + 10_000L, 123L, 20260908L, "XAUUSD", command, "pending")
        return unsigned.copy(signature = AmarCommandSigner.hmacSha256("secret", AmarCommandSigner.canonical(unsigned)))
    }

    @Test
    fun authorizedCommandPassesAllGatesWithReceiverHmac() {
        val e = envelope()
        val decision = AmarSecureCommandValidator(clockMs = { 2_000L }).validate(e, true, true, true, true, 123L, 20260908L, "XAUUSD", false, "secret")
        assertTrue(decision.accepted)
    }

    @Test
    fun missingLiveAuthorizationBlocks() {
        val e = envelope()
        val decision = AmarSecureCommandValidator(clockMs = { 2_000L }).validate(e, true, true, false, true, 123L, 20260908L, "XAUUSD", false, "secret")
        assertFalse(decision.accepted)
    }

    @Test
    fun missingHmacSecretBlocksLiveCommand() {
        val decision = AmarSecureCommandValidator(clockMs = { 2_000L }).validate(envelope(), true, true, true, true, 123L, 20260908L, "XAUUSD", false)
        assertFalse(decision.accepted)
        assertEquals(AmarCommandRejectReason.INVALID_SIGNATURE, decision.reason)
    }

    @Test
    fun invalidHmacBlocksCommand() {
        val e = envelope().copy(signature = "00")
        val decision = AmarSecureCommandValidator(clockMs = { 2_000L }).validate(e, true, true, true, true, 123L, 20260908L, "XAUUSD", false, "secret")
        assertFalse(decision.accepted)
        assertEquals(AmarCommandRejectReason.INVALID_SIGNATURE, decision.reason)
    }

    @Test
    fun commandSymbolMustMatchEnvelopeScope() {
        val command = AmarBrokerCommand("r1", "EURUSD", Side.BUY, 0.01)
        val unsigned = AmarCommandEnvelope("r1", "id1", "nonce1", 1_000L, 11_000L, 123L, 20260908L, "XAUUSD", command, "pending")
        val e = unsigned.copy(signature = AmarCommandSigner.hmacSha256("secret", AmarCommandSigner.canonical(unsigned)))
        val decision = AmarSecureCommandValidator(clockMs = { 2_000L }).validate(e, true, true, true, true, 123L, 20260908L, "XAUUSD", false, "secret")
        assertFalse(decision.accepted)
        assertEquals(AmarCommandRejectReason.INVALID_SCOPE, decision.reason)
    }

    @Test
    fun futureAndWrongScopeAreRejected() {
        val e = envelope(100_000L)
        val future = AmarSecureCommandValidator(clockMs = { 1_000L }).validate(e, true, true, true, true, 123L, 20260908L, "XAUUSD", false, "secret")
        assertEquals(false, future.accepted)
        val scope = AmarSecureCommandValidator(clockMs = { 101_000L }).validate(e, true, true, true, true, 999L, 20260908L, "XAUUSD", false, "secret")
        assertEquals(false, scope.accepted)
    }
}
