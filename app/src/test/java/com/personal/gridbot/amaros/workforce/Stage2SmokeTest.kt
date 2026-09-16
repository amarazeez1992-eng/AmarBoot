package com.personal.gridbot.amaros.workforce

import org.junit.Assert.assertTrue
import org.junit.Test

class Stage2SmokeTest {
    @Test
    fun stageTwo_agentCore_contract_is_available() {
        val core = AmarAgentCore()
        val result = core.ask("Hello")
        assertTrue(result.isNotBlank())
    }
}
