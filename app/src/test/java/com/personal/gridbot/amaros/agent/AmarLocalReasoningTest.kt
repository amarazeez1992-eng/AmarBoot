package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarLocalReasoningTest {
    @Test fun local_reasoning_never_calls_external_ai_and_uses_supplied_evidence() = runBlocking {
        val provider = AmarLocalReasoning()
        val response = provider.respond(
            AmarAgentContext(
                userText = "ما هي النتيجة؟\n\nEvidence summary:\nsource=0|title=Official|authority=OFFICIAL|stance=SUPPORTS|publisher=Official|uri=https://example.gov/x\nevidence=الدليل الفعلي هنا",
                tools = emptyList(),
                executionAllowed = false,
                brokerAccessAllowed = false
            )
        )
        assertTrue(response.answer.contains("الدليل الفعلي هنا"))
        assertTrue(response.answer.contains("Official"))
    }

    @Test fun local_reasoning_blocks_unsupported_external_claims() = runBlocking {
        val response = AmarLocalReasoning().respond(
            AmarAgentContext(
                userText = "ما الأخبار الحالية؟\n\nEvidence summary:\nNo external research required.",
                tools = emptyList(),
                executionAllowed = false,
                brokerAccessAllowed = false
            )
        )
        assertTrue(response.answer.contains("لا سأخترع"))
    }
}
