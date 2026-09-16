package com.personal.gridbot.amaros.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiFileToolGatewayTest {
    @Test fun file_analysis_is_exposed_through_agent_tool_boundary() = runBlocking {
        val result = AmarAiDeterministicToolGateway.execute(
            "file_analyze",
            "path=src/Test.kt\n---AMAR-CONTENT---\nclass Test { fun run() = 1 }"
        )
        assertTrue(result?.startsWith("FILE_ANALYSIS|") == true)
        assertTrue(result?.contains("language=KOTLIN") == true)
    }

    @Test fun write_capabilities_remain_blocked() = runBlocking {
        val result = AmarAiDeterministicToolGateway.execute("strategy_save", "draft")
        assertTrue(result?.startsWith("TOOL_REJECTED|") == true)
    }
}
