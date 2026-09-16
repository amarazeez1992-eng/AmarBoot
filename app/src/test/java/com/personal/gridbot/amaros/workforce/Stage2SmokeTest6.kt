package com.personal.gridbot.amaros.workforce

import org.junit.Assert.assertNotNull
import org.junit.Test

class Stage2SmokeTest6 {
    @Test
    fun stageTwo_core_type_is_loadable() {
        assertNotNull(AmarAgentCore::class.java)
    }
}
