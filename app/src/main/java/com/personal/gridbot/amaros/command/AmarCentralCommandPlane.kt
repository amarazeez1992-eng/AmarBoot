package com.personal.gridbot.amaros.command

import com.personal.gridbot.amaros.governance.AmarExecutionGovernance

/**
 * Stage 8 application command authority.
 *
 * The command plane routes only decisions already produced by Amar AI. Control-room
 * modules are consumers and cannot grant themselves capabilities or device access.
 * This class deliberately does not execute broker/device operations.
 */
object AmarCentralCommandPlane {
    enum class Route {
        CONTROL_ROOM,
        EXECUTION_GATEWAY,
        DEVICE_GATEWAY,
        AUDIT
    }

    enum class DeviceCapability {
        READ_STATUS,
        USER_APPROVED_INPUT,
        EXPORT_ARTIFACT,
        NOTIFY_USER
    }

    enum class Decision {
        ROUTED,
        REJECTED,
        REVOKED,
        EMERGENCY_LOCKED,
        STALE
    }

    data class Permission(
        val principal: String,
        val capabilities: Set<DeviceCapability>,
        val expiresAtEpochMs: Long,
        val version: Long = 1L,
        val active: Boolean = true,
    ) {
        init {
            require(principal.isNotBlank())
            require(capabilities.isNotEmpty())
            require(expiresAtEpochMs > 0L)
            require(version > 0L)
        }

        fun permits(capability: DeviceCapability, nowEpochMs: Long): Boolean =
            active && expiresAtEpochMs > nowEpochMs && capability in capabilities
    }

    data class AgentDecision(
        val decisionId: String,
        val principal: String,
        val capability: AmarExecutionGovernance.Capability,
        val route: Route,
        val command: String,
        val issuedAtEpochMs: Long,
        val expiresAtEpochMs: Long,
        val policyVersion: Long,
        val policyApproved: Boolean,
        val auditRequired: Boolean = true,
    ) {
        init {
            require(decisionId.isNotBlank())
            require(principal.isNotBlank())
            require(command.isNotBlank())
            require(issuedAtEpochMs >= 0L)
            require(expiresAtEpochMs > issuedAtEpochMs)
            require(policyVersion > 0L)
        }
    }

    data class Receipt(
        val decisionId: String,
        val decision: Decision,
        val route: Route?,
        val message: String,
        val sequence: Long,
    )

    private val lock = Any()
    private val permissions = mutableMapOf<String, Permission>()
    private val receipts = mutableMapOf<String, Receipt>()
    private var emergencyLock = false
    private var sequence = 0L

    fun grant(permission: Permission, nowEpochMs: Long): Boolean = synchronized(lock) {
        if (permission.expiresAtEpochMs <= nowEpochMs) return@synchronized false
        permissions[permission.principal] = permission
        true
    }

    fun revoke(principal: String): Boolean = synchronized(lock) {
        permissions.remove(principal) != null
    }

    fun setEmergencyLock(enabled: Boolean): Unit = synchronized(lock) {
        emergencyLock = enabled
    }

    fun isEmergencyLocked(): Boolean = synchronized(lock) { emergencyLock }

    /**
     * Routes an Agent decision. Repeated decision IDs are idempotent.
     * A stale, unapproved, revoked or emergency-locked decision is never routed.
     */
    fun route(decision: AgentDecision, nowEpochMs: Long): Receipt = synchronized(lock) {
        receipts[decision.decisionId]?.let { return@synchronized it }

        val result = when {
            emergencyLock -> Decision.EMERGENCY_LOCKED to "central emergency lock active"
            !decision.policyApproved -> Decision.REJECTED to "Agent policy decision not approved"
            nowEpochMs < decision.issuedAtEpochMs || nowEpochMs >= decision.expiresAtEpochMs ->
                Decision.STALE to "Agent decision outside validity window"
            !permissions[decision.principal].orFalse(decision.capability, nowEpochMs) ->
                Decision.REVOKED to "principal lacks active delegated capability"
            decision.route == Route.DEVICE_GATEWAY && decision.capability == AmarExecutionGovernance.Capability.SUBMIT_EXECUTION ->
                Decision.REJECTED to "execution cannot bypass the execution governance boundary"
            else -> Decision.ROUTED to "Agent decision routed to subordinate gateway"
        }
        val receipt = Receipt(decision.decisionId, result.first, decision.route.takeIf { result.first == Decision.ROUTED }, result.second, ++sequence)
        receipts[decision.decisionId] = receipt
        receipt
    }

    /** Device access is separately bounded and cannot be inferred from an Agent route. */
    fun authorizeDevice(
        principal: String,
        capability: DeviceCapability,
        nowEpochMs: Long,
    ): Boolean = synchronized(lock) {
        !emergencyLock && permissions[principal]?.permits(capability, nowEpochMs) == true
    }

    fun receipt(decisionId: String): Receipt? = synchronized(lock) { receipts[decisionId] }

    fun clearForTests() = synchronized(lock) {
        permissions.clear()
        receipts.clear()
        emergencyLock = false
        sequence = 0L
    }

    private fun Permission?.orFalse(
        capability: AmarExecutionGovernance.Capability,
        nowEpochMs: Long,
    ): Boolean = this?.let {
        it.active && it.expiresAtEpochMs > nowEpochMs && capabilityAllowed(it.principal, capability)
    } ?: false

    /* Capability membership is delegated to the Stage 7 governance authority. */
    private fun capabilityAllowed(
        principal: String,
        capability: AmarExecutionGovernance.Capability,
    ): Boolean = delegatedCapabilities[principal]?.contains(capability) == true

    private val delegatedCapabilities = mutableMapOf<String, Set<AmarExecutionGovernance.Capability>>()

    fun bindDelegatedCapabilities(
        principal: String,
        capabilities: Set<AmarExecutionGovernance.Capability>,
    ): Boolean = synchronized(lock) {
        if (principal.isBlank() || capabilities.isEmpty()) return@synchronized false
        delegatedCapabilities[principal] = capabilities.toSet()
        true
    }

    fun clearDelegatedCapabilities(principal: String): Boolean = synchronized(lock) {
        delegatedCapabilities.remove(principal) != null
    }
}
