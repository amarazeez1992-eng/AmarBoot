package com.personal.gridbot.amaros.ai.integration

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiMt5IntegrationPhaseSixTest {
    @Test fun gateway_allowsOnlyReadOnlySnapshots() {
        assertTrue(AmarMt5IntegrationGateway.allowSnapshot(AmarMt5AccessMode.READ_ONLY))
        assertFalse(AmarMt5IntegrationGateway.allowSnapshot(AmarMt5AccessMode.PAPER))
        assertFalse(AmarMt5IntegrationGateway.allowSnapshot(AmarMt5AccessMode.LIVE))
    }

    @Test fun gateway_allowsOnlyApprovedPaperCommands() {
        val approved = AmarMt5Command("k1", "CLOSE_POSITION", true)
        val rejected = approved.copy(idempotencyKey = "k2", approved = false)
        assertTrue(AmarMt5IntegrationGateway.allowCommand(AmarMt5AccessMode.PAPER, approved))
        assertFalse(AmarMt5IntegrationGateway.allowCommand(AmarMt5AccessMode.PAPER, rejected))
        assertFalse(AmarMt5IntegrationGateway.allowCommand(AmarMt5AccessMode.READ_ONLY, approved))
        assertFalse(AmarMt5IntegrationGateway.allowCommand(AmarMt5AccessMode.LIVE, approved))
    }

    @Test fun idempotencyLedger_acceptsEachKeyOnce() {
        val ledger = AmarMt5IdempotencyLedger()
        assertTrue(ledger.acceptOnce("k1"))
        assertFalse(ledger.acceptOnce("k1"))
        assertFalse(ledger.acceptOnce(""))
    }

    @Test fun killSwitch_defaultsEnabledAndCanBeDeactivated() {
        val switch = AmarMt5KillSwitch()
        assertTrue(switch.enabled)
        switch.deactivate()
        assertFalse(switch.enabled)
        switch.activate()
        assertTrue(switch.enabled)
    }
}
