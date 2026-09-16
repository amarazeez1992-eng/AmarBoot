package com.personal.gridbot.amaros.workforce

import org.junit.Assert.assertNotNull
import org.junit.Test

class Stage2SmokeTest5 {
    @Test
    fun stageTwo_core_exists() {
        assertNotNull(AmarAgentCore::class)
    }
}
