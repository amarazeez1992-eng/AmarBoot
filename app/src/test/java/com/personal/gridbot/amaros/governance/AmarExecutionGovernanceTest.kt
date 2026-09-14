package com.personal.gridbot.amaros.governance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarExecutionGovernanceTest {
    private var now = 1_000L
    private fun authority() = AmarExecutionGovernance.Authority { now }
    private fun delegation(expires: Long = 10_000L) = AmarExecutionGovernance.Delegation(
        principal = "user",
        capabilities = setOf(AmarExecutionGovernance.Capability.SUBMIT_EXECUTION),
        expiresAtEpochMs = expires,
    )
    private fun proposal(key: String = "k1", risk: Double = 0.20, evidence: Boolean = true, security: Boolean = true) =
        AmarExecutionGovernance.Proposal(key, "user", AmarExecutionGovernance.Capability.SUBMIT_EXECUTION, "OPEN_MARKET", risk, evidence, security, now)

    @Test fun approvedExecutionRequiresExplicitDelegation() {
        val a = authority()
        assertEquals(AmarExecutionGovernance.Decision.REJECTED, a.authorize(proposal()).decision)
        assertTrue(a.delegate(delegation()))
        assertEquals(AmarExecutionGovernance.Decision.APPROVED, a.authorize(proposal("k2")).decision)
    }

    @Test fun revocationAndExpiryFailClosed() {
        val a = authority()
        a.delegate(delegation())
        assertTrue(a.revoke("user"))
        assertEquals(AmarExecutionGovernance.Decision.REJECTED, a.authorize(proposal()).decision)
        a.delegate(delegation(2_000L))
        now = 2_000L
        assertEquals(AmarExecutionGovernance.Decision.REJECTED, a.authorize(proposal("k2")).decision)
    }

    @Test fun emergencyLockOverridesValidDelegation() {
        val a = authority()
        a.delegate(delegation())
        a.setEmergencyLock(true)
        assertEquals(AmarExecutionGovernance.Decision.LOCKED, a.authorize(proposal()).decision)
        a.setEmergencyLock(false)
        assertEquals(AmarExecutionGovernance.Decision.APPROVED, a.authorize(proposal("k2")).decision)
    }

    @Test fun riskEvidenceAndSecurityAreIndependentGates() {
        val a = authority()
        a.delegate(delegation())
        assertEquals(AmarExecutionGovernance.Decision.REJECTED, a.authorize(proposal(risk = 0.71)).decision)
        assertEquals(AmarExecutionGovernance.Decision.DATA_INSUFFICIENT, a.authorize(proposal("k2", evidence = false)).decision)
        assertEquals(AmarExecutionGovernance.Decision.REJECTED, a.authorize(proposal("k3", security = false)).decision)
    }

    @Test fun idempotencyReturnsTheOriginalReceipt() {
        val a = authority()
        a.delegate(delegation())
        val first = a.authorize(proposal())
        val second = a.authorize(proposal())
        assertEquals(first, second)
    }

    @Test fun acknowledgementAndReconciliationAreExplicit() {
        val a = authority()
        a.delegate(delegation())
        val approved = a.authorize(proposal())
        assertEquals(AmarExecutionGovernance.CommandStatus.APPROVED, approved.status)
        val ack = a.acknowledge("k1")!!
        assertEquals(AmarExecutionGovernance.CommandStatus.ACKNOWLEDGED, ack.status)
        assertTrue(a.reconcile("k1", AmarExecutionGovernance.CommandStatus.ACKNOWLEDGED).consistent)
        assertFalse(a.reconcile("k1", AmarExecutionGovernance.CommandStatus.EXECUTED).consistent)
    }
}
