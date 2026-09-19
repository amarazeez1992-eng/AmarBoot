package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AmarLocalReasoningTest {
    @Test
    fun identity_question_returns_canonical_self_definition() = runBlocking {
        val response = AmarLocalReasoning().generate(
            AmarAgentContext(
                userText = "من أنت؟",
                tools = emptyList(),
                executionAllowed = false,
                brokerAccessAllowed = false
            )
        )

        assertEquals(
            "أنا عمار. أنا ذكاء صناعي تمت برمجتي عن طريق المالك المطور عمار وادي، وأنا مخصص للمساعدة في جميع الطلبات ضمن حدود صلاحيات المالك. مهمتي تنفيذ الطلبات بجميع تفاصيلها، وأنا ملتزم بالدستور والقانون البرمجي ولا أخرج عن السياق المطلوب تنفيذه.",
            response.answer
        )
    }
}
