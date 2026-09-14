package com.personal.gridbot.amaros.command

import com.personal.gridbot.amaros.governance.AmarExecutionGovernance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AmarCentralCommandPlaneTest {
    private val plane = AmarCentralCommandPlane
    private var now = 1_000L

    @Before
    fun reset() = plane.clearForTests()

    private fun permission(
        expires: Long = 10_000L,
        execution: Set<AmarExecutionGovernance.Capability> = setOf(AmarExecutionGovernance.Capability.SUBMIT_EXECUTION),
        device: Set<AmarCentralCommandPlane.DeviceCapability> = setOf(AmarCentralCommandPlane.DeviceCapability.READ_STATUS),
    ) = AmarCentralCommandPlane.Permission("user", execution, device, expires)

    private fun decision(
        id: String = "d1",
        issued: Long = 1_000L,
        expires: Long = 5_000L,
        approved: Boolean = true,
        route: AmarCentralCommandPlane.Route = AmarCentralCommandPlane.Route.EXECUTION_GATEWAY,
    ) = AmarCentralCommandPlane.AgentDecision(
        decisionId = id,
        principal = "user",
        capability = AmarExecutionGovernance.Capability.SUBMIT_EXECUTION,
        route = route,
        command = "OPEN_MARKET",
        issuedAtEpochMs = issued,
        expiresAtEpochMs = expires,
        policyVersion = 1L,
        policyApproved = approved,
    )

    @Test
    fun unapprovedAgentDecisionIsRejectedEvenWithPermission() {
        assertTrue(plane.grant(permission(), now))
        assertEquals(AmarCentralCommandPlane.Decision.REJECTED, plane.route(decision(approved = false), now).decision)
    }

    @Test
    fun explicitPermissionIsRequiredAndRevocationIsImmediate() {
        assertEquals(AmarCentralCommandPlane.Decision.REVOKED, plane.route(decision(), now).decision)
        plane.grant(permission(), now)
        assertEquals(AmarCentralCommandPlane.Decision.ROUTED, plane.route(decision(), now).decision)
        assertTrue(plane.revoke("user"))
        assertEquals(AmarCentralCommandPlane.Decision.REVOKED, plane.route(decision("d2"), now).decision)
    }

    @Test
    fun emergencyLockOverridesValidPermission() {
        plane.grant(permission(), now)
        plane.setEmergencyLock(true)
        assertTrue(plane.isEmergencyLocked())
        assertEquals(AmarCentralCommandPlane.Decision.EMERGENCY_LOCKED, plane.route(decision(), now).decision)
        assertFalse(plane.authorizeDevice("user", AmarCentralCommandPlane.DeviceCapability.READ_STATUS, now))
    }

    @Test
    fun decisionExpiryIsFailClosed() {
        plane.grant(permission(), now)
        now = 5_000L
        assertEquals(AmarCentralCommandPlane.Decision.STALE, plane.route(decision(), now).decision)
    }

    @Test
    fun devicePermissionsAreBoundedAndIndependent() {
        plane.grant(
            permission(
                execution = emptySet(),
                device = setOf(AmarCentralCommandPlane.DeviceCapability.READ_STATUS),
            ),
            now,
        )
        assertTrue(plane.authorizeDevice("user", AmarCentralCommandPlane.DeviceCapability.READ_STATUS, now))
        assertFalse(plane.authorizeDevice("user", AmarCentralCommandPlane.DeviceCapability.EXPORT_ARTIFACT, now))
        assertEquals(AmarCentralCommandPlane.Decision.REVOKED, plane.route(decision(), now).decision)
    }

    @Test
    fun executionCannotBeRoutedThroughDeviceGateway() {
        plane.grant(permission(), now)
        assertEquals(
            AmarCentralCommandPlane.Decision.REJECTED,
            plane.route(decision(route = AmarCentralCommandPlane.Route.DEVICE_GATEWAY), now).decision,
        )
    }

    @Test
    fun duplicateDecisionIdReturnsOriginalReceipt() {
        plane.grant(permission(), now)
        val first = plane.route(decision(), now)
        val second = plane.route(decision(), now + 100L)
        assertEquals(first, second)
    }
}
