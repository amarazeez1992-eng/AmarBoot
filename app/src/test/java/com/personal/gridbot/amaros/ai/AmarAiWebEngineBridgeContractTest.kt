package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiWebEngineBridgeContractTest {
    @Test
    fun bridgeContractDeclaresProviderNeutralEngineBoundary() {
        val source = AmarAiEngineMesh().snapshot()
        assertTrue(source.providerNeutral)
        assertTrue(!source.executionAuthority)
        assertTrue(AmarAiEngineMesh().connectedEngineIds().contains("reasoning"))
        assertTrue(AmarAiEngineMesh().connectedEngineIds().contains("market"))
        assertTrue(AmarAiEngineMesh().connectedEngineIds().contains("research"))
        assertTrue(AmarAiEngineMesh().connectedEngineIds().contains("self_audit"))
    }
}
