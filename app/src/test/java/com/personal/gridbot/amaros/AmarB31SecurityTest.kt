package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarBrokerCommand
import com.personal.gridbot.amaros.broker.AmarCommandEnvelope
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
    fun authorizedCommandPassesAllGates() {
        val e = envelope()
        val decision = AmarSecureCommandValidator { 2_000L }.validate(e, true, true, true, true, 123L, 20260908L, "XAUUSD", false)
        assertTrue(decision.accepted)
    }

    @Test
    fun missingLiveAuthorizationBlocks() {
        val e = envelope()
        val decision = AmarSecureCommandValidator { 2_000L }.validate(e, true, true, false, true, 123L, 20260908L, "XAUUSD", false)
        assertFalse(decision.accepted)
    }

    @Test
    fun futureAndWrongScopeAreRejected() {
        val e = envelope(100_000L)
        val future = AmarSecureCommandValidator { 1_000L }.validate(e, true, true, true, true, 123L, 20260908L, "XAUUSD", false)
        assertEquals(false, future.accepted)
        val scope = AmarSecureCommandValidator { 101_000L }.validate(e, true, true, true, true, 999L, 20260908L, "XAUUSD", false)
        assertEquals(false, scope.accepted)
    }
}
