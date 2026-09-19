package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarLocalReasoningResponseTest {
    @Test
    fun response_is_not_the_old_fixed_placeholder() = runBlocking {
        val response = AmarLocalReasoning().respond(
            AmarAgentContext(
                userText = "لماذا لا يوجد رد من الوكيل؟",
                tools = emptyList(),
                executionAllowed = false,
                brokerAccessAllowed = false,
                requestedSourceCount = 1,
                maximumSourceCount = 1,
                requireCrossValidation = false,
                requireBacktestWhenApplicable = false
            )
        )

        assertFalse(response.answer.isBlank())
        assertTrue(response.answer.contains("لماذا لا يوجد رد من الوكيل؟"))
        assertFalse(response.answer.contains("الوضع الحالي: تحليل وبحث ومحاكاة فقط"))
    }
}
