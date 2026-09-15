package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiEngineMeshTest {
    @Test
    fun meshIsProviderNeutralAndExecutionClosed() {
        val mesh = AmarAiEngineMesh()
        val snapshot = mesh.snapshot()
        assertTrue(snapshot.providerNeutral)
        assertFalse(snapshot.executionAuthority)
        assertTrue(mesh.connectedEngineIds().contains("reasoning"))
        assertTrue(mesh.connectedEngineIds().contains("research"))
        assertTrue(mesh.connectedEngineIds().contains("validation"))
        assertTrue(mesh.connectedEngineIds().contains("precision"))
        assertTrue(mesh.connectedEngineIds().contains("camera_multimodal"))
        assertTrue(mesh.connectedEngineIds().contains("screen_multimodal"))
    }
}
