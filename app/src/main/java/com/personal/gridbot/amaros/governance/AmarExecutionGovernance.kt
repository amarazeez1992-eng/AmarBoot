package com.personal.gridbot.amaros.governance

/** Stage 7: fail-closed execution governance. This module proposes and authorizes commands; it never talks to a broker. */
object AmarExecutionGovernance {
    enum class Capability { PROPOSE_EXECUTION, SUBMIT_EXECUTION, CANCEL_EXECUTION, EMERGENCY_LOCK, RECONCILE }
    enum class Decision { APPROVED, REJECTED, LOCKED, DATA_INSUFFICIENT }
    enum class CommandStatus { PROPOSED, VALIDATED, RISK_CLEARED, SECURITY_CLEARED, APPROVED, ACKNOWLEDGED, EXECUTED, VERIFIED, REJECTED, FAILED, STALE, CANCELLED }

    data class Delegation(
        val principal: String,
        val capabilities: Set<Capability>,
        val expiresAtEpochMs: Long,
        val active: Boolean = true,
        val version: Long = 1L,
    ) {
        init {
            require(principal.isNotBlank())
            require(capabilities.isNotEmpty())
            require(expiresAtEpochMs > 0)
            require(version > 0)
        }

        fun permits(capability: Capability, nowEpochMs: Long): Boolean =
            active && nowEpochMs < expiresAtEpochMs && capability in capabilities
    }

    data class Proposal(
        val idempotencyKey: String,
        val principal: String,
        val capability: Capability,
        val command: String,
        val riskScore: Double,
        val evidenceComplete: Boolean,
        val securityPassed: Boolean,
        val createdAtEpochMs: Long,
    ) {
        init {
            require(idempotencyKey.isNotBlank())
            require(principal.isNotBlank())
            require(command.isNotBlank())
            require(riskScore.isFinite() && riskScore in 0.0..1.0)
            require(createdAtEpochMs > 0)
        }
    }

    data class Receipt(
        val idempotencyKey: String,
        val status: CommandStatus,
        val decision: Decision,
        val message: String,
        val sequence: Long,
    )

    data class Reconciliation(
        val idempotencyKey: String,
        val expected: CommandStatus,
        val observed: CommandStatus,
        val consistent: Boolean,
    )

    class Authority(private val clock: () -> Long = { System.currentTimeMillis() }) {
        private val delegations = linkedMapOf<String, Delegation>()
        private val receipts = linkedMapOf<String, Receipt>()
        private var sequence = 0L
        private var emergencyLocked = false

        @Synchronized
        fun delegate(delegation: Delegation): Boolean {
            if (delegation.expiresAtEpochMs <= clock()) return false
            delegations[delegation.principal] = delegation
            return true
        }

        @Synchronized
        fun revoke(principal: String): Boolean = delegations.remove(principal) != null

        @Synchronized
        fun setEmergencyLock(locked: Boolean) {
            emergencyLocked = locked
        }

        @Synchronized
        fun isEmergencyLocked(): Boolean = emergencyLocked

        @Synchronized
        fun authorize(proposal: Proposal): Receipt {
            receipts[proposal.idempotencyKey]?.let { return it }
            val result = when {
                emergencyLocked -> receipt(proposal, Decision.LOCKED, CommandStatus.REJECTED, "Emergency lock active")
                proposal.capability != Capability.SUBMIT_EXECUTION -> receipt(proposal, Decision.REJECTED, CommandStatus.REJECTED, "Capability is not executable")
                !proposal.evidenceComplete -> receipt(proposal, Decision.DATA_INSUFFICIENT, CommandStatus.REJECTED, "Evidence is incomplete")
                !proposal.securityPassed -> receipt(proposal, Decision.REJECTED, CommandStatus.REJECTED, "Security validation failed")
                proposal.riskScore > 0.70 -> receipt(proposal, Decision.REJECTED, CommandStatus.REJECTED, "Risk threshold exceeded")
                !delegations[proposal.principal].orFalse { it.permits(Capability.SUBMIT_EXECUTION, clock()) } -> receipt(proposal, Decision.REJECTED, CommandStatus.REJECTED, "Delegation missing, revoked, or expired")
                else -> receipt(proposal, Decision.APPROVED, CommandStatus.APPROVED, "Execution approved; gateway hand-off required")
            }
            receipts[proposal.idempotencyKey] = result
            return result
        }

        @Synchronized
        fun acknowledge(idempotencyKey: String): Receipt? = transition(idempotencyKey, CommandStatus.ACKNOWLEDGED)

        @Synchronized
        fun reconcile(idempotencyKey: String, observed: CommandStatus): Reconciliation {
            val expected = receipts[idempotencyKey]?.status ?: CommandStatus.REJECTED
            return Reconciliation(idempotencyKey, expected, observed, expected == observed)
        }

        private fun transition(key: String, next: CommandStatus): Receipt? {
            val current = receipts[key] ?: return null
            if (current.status != CommandStatus.APPROVED) return current
            val updated = current.copy(status = next, sequence = ++sequence, message = "State updated to $next")
            receipts[key] = updated
            return updated
        }

        private fun receipt(proposal: Proposal, decision: Decision, status: CommandStatus, message: String): Receipt =
            Receipt(proposal.idempotencyKey, status, decision, message, ++sequence)
    }

    private inline fun <T> T?.orFalse(predicate: (T) -> Boolean): Boolean = this?.let(predicate) == true
}
