package com.personal.gridbot.amaros.workspace

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Item10CameraWorkspaceTest {
    @Test
    fun cameraRequiresPermissionAndExplicitUserInitiation() {
        val audit = AmarWorkspaceAuditLog()
        val camera = AmarCameraWorkspace(audit)
        assertFalse(camera.start(AmarCameraSession("c1", 100L, permissionGranted = false, userInitiated = true)))
        assertFalse(camera.start(AmarCameraSession("c2", 100L, permissionGranted = true, userInitiated = false)))
        assertTrue(camera.start(AmarCameraSession("c3", 100L, permissionGranted = true, userInitiated = true)))
        assertTrue(camera.active())
    }

    @Test
    fun cameraStopsImmediatelyAndCannotAnalyzeAfterStop() {
        val audit = AmarWorkspaceAuditLog()
        val camera = AmarCameraWorkspace(audit)
        assertTrue(camera.start(AmarCameraSession("c4", 100L, true, true)))
        assertTrue(camera.stop(110L))
        assertFalse(camera.active())
        assertFalse(camera.analyze(listOf(AmarCameraFrame(111L, visibleText = "Settings", confidence = 0.99))).evidenceBacked)
        assertTrue(audit.snapshot().any { it.action == "camera_stop" })
    }

    @Test
    fun insufficientVisibilityFailsClosedWithoutFabrication() {
        val audit = AmarWorkspaceAuditLog()
        val camera = AmarCameraWorkspace(audit)
        assertTrue(camera.start(AmarCameraSession("c5", 100L, true, true)))
        val result = camera.analyze(listOf(AmarCameraFrame(101L, confidence = 0.2)))
        assertFalse(result.evidenceBacked)
        assertTrue(result.blockers.contains("unreadable_camera_view"))
    }
}
