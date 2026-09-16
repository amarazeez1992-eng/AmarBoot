package com.personal.gridbot.amaros.agent

/** Provider-neutral reasoning contract owned by the AMAR Agent. */
interface AmarReasoning {
    suspend fun generate(context: AmarAgentContext): AmarAgentResponse
}

/** Safe local reasoning: works without network or external API keys. */
class AmarLocalReasoning : AmarReasoning, AmarReasoningProvider {
    override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
        val text = context.userText.trim()
        if (text.isEmpty()) return AmarAgentResponse("اكتب طلبك وسأبدأ التحليل.")
        return AmarAgentResponse(
            answer = "AMAR AI Agent جاهز. تم استقبال طلبك: $text\n\nالوضع الحالي: تحليل وبحث ومحاكاة فقط، بدون تنفيذ صفقات.",
            actions = context.tools.map { it.id }
        )
    }

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse = generate(context)
}
