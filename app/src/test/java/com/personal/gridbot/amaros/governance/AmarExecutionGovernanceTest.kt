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

    @Test fun emergencyLockOverridesValidDelegationAndInterruptsPendingExecution() {
        val a = authority()
        a.delegate(delegation())
        assertEquals(AmarExecutionGovernance.Decision.APPROVED, a.authorize(proposal()).decision)
        a.acknowledge("k1")
        a.setEmergencyLock(true)
        assertTrue(a.isEmergencyLocked())
        assertEquals(AmarExecutionGovernance.CommandStatus.REJECTED, a.reconcile("k1", AmarExecutionGovernance.CommandStatus.REJECTED).expected)
        assertEquals(AmarExecutionGovernance.Decision.LOCKED, a.authorize(proposal("k2")).decision)
    }

    @Test fun riskEvidenceAndSecurityAreIndependentGates() {
        val a = authority()
        a.delegate(delegation())
        assertEquals(AmarExecutionGovernance.Decision.REJECTED, a.authorize(proposal(risk = 0.71)).decision)
        assertEquals(AmarExecutionGovernance.Decision.DATA_INSUFFICIENT, a.authorize(proposal("k2", evidence = false)).decision)
        assertEquals(AmarExecutionGovernance.Decision.REJECTED, a.authorize(proposal("k3", security = false)).decision)
    }

    @Test fun riskThresholdBoundaryIsInclusiveAtPointSeven() {
        val a = authority()
        a.delegate(delegation())
        assertEquals(AmarExecutionGovernance.Decision.APPROVED, a.authorize(proposal(risk = 0.70)).decision)
        assertEquals(AmarExecutionGovernance.Decision.REJECTED, a.authorize(proposal("k2", risk = 0.7000001)).decision)
    }

    @Test fun idempotencyReturnsTheOriginalReceipt() {
        val a = authority()
        a.delegate(delegation())
        val first = a.authorize(proposal())
        val second = a.authorize(proposal())
        assertEquals(first, second)
    }

    @Test fun executionLifecycleRequiresAckThenExecutionThenVerification() {
        val a = authority()
        a.delegate(delegation())
        assertEquals(AmarExecutionGovernance.CommandStatus.APPROVED, a.authorize(proposal()).status)
        assertEquals(AmarExecutionGovernance.CommandStatus.APPROVED, a.markExecuted("k1")!!.status)
        assertEquals(AmarExecutionGovernance.CommandStatus.ACKNOWLEDGED, a.acknowledge("k1")!!.status)
        assertEquals(AmarExecutionGovernance.CommandStatus.ACKNOWLEDGED, a.verify("k1")!!.status)
        assertEquals(AmarExecutionGovernance.CommandStatus.EXECUTED, a.markExecuted("k1")!!.status)
        assertEquals(AmarExecutionGovernance.CommandStatus.VERIFIED, a.verify("k1")!!.status)
        assertEquals(AmarExecutionGovernance.CommandStatus.VERIFIED, a.acknowledge("k1")!!.status)
    }

    @Test fun reconciliationDetectsReadBackMismatch() {
        val a = authority()
        a.delegate(delegation())
        a.authorize(proposal())
        a.acknowledge("k1")
        assertTrue(a.reconcile("k1", AmarExecutionGovernance.CommandStatus.ACKNOWLEDGED).consistent)
        assertFalse(a.reconcile("k1", AmarExecutionGovernance.CommandStatus.EXECUTED).consistent)
    }

    @Test fun emergencyLockBlocksVerificationOfAlreadyExecutedCommand() {
        val a = authority()
        a.delegate(delegation())
        a.authorize(proposal())
        a.acknowledge("k1")
        a.markExecuted("k1")
        a.setEmergencyLock(true)
        assertEquals(AmarExecutionGovernance.CommandStatus.EXECUTED, a.reconcile("k1", AmarExecutionGovernance.CommandStatus.EXECUTED).expected)
        assertEquals(AmarExecutionGovernance.CommandStatus.EXECUTED, a.verify("k1")!!.status)
    }
}
