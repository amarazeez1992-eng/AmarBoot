package com.personal.gridbot.amaros.workforce

import org.junit.Assert.assertTrue
import org.junit.Test

class Stage2SmokeTest3 {
    @Test
    fun stageTwo_core_class_exists() {
        val core = AmarAgentCore()
        assertTrue(core.toString().isNotBlank())
    }
}
