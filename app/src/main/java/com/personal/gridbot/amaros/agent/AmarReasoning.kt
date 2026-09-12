package com.personal.gridbot.amaros.agent

/** Provider-neutral reasoning contract. Gemini/ChatGPT are not part of the core. */
interface AmarReasoning {
    suspend fun generate(context: AmarAgentContext): AmarAgentResponse
}

/** Safe local fallback: works without network or API keys. */
class AmarLocalReasoning : AmarReasoning {
    override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
        val text = context.userText.trim()
        if (text.isEmpty()) return AmarAgentResponse("اكتب سؤالك التداولي وسأبدأ التحليل.")
        return AmarAgentResponse(
            answer = "AMAR AI جاهز. تم استقبال طلبك: $text\n\nالوضع الحالي: تحليل وبحث ومحاكاة فقط، بدون تنفيذ صفقات.",
            actions = context.tools.map { it.id }
        )
    }
}
