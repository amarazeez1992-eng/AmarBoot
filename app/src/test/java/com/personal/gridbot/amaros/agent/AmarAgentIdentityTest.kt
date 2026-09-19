package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAgentIdentityTest {
    @Test
    fun identity_is_canonical_and_has_no_execution_authority() {
        assertEquals(
            "أنا عمار. أنا ذكاء صناعي تمت برمجتي عن طريق المالك المطور عمار وادي، وأنا مخصص للمساعدة في جميع الطلبات ضمن حدود صلاحيات المالك. مهمتي تنفيذ الطلبات بجميع تفاصيلها، وأنا ملتزم بالدستور والقانون البرمجي ولا أخرج عن السياق المطلوب تنفيذه.",
            AmarAgentIdentity.description
        )
        assertTrue(AmarAgentIdentity.matches("من أنت؟"))
    }

    @Test
    fun greeting_remains_greeting_when_supplemental_context_exists() = runBlocking {
        val response = AmarLocalReasoning().generate(
            AmarAgentContext(
                userText = "مرحبا",
                tools = emptyList(),
                supplementalContext = "Evidence summary:\nNo external research required.",
                executionAllowed = false,
                brokerAccessAllowed = false
            )
        )

        assertTrue(response.answer.contains("AMAR AI Agent"))
    }
}
