package com.personal.gridbot.amaros.agent

/** Provider-neutral reasoning contract. Gemini/ChatGPT are not part of the core. */
interface AmarReasoning {
    suspend fun generate(context: AmarAgentContext): AmarAgentResponse
}

/** Safe local reasoning boundary: no network, API keys, execution, or provider dependency. */
class AmarLocalReasoning : AmarReasoning, AmarReasoningProvider {
    override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
        val text = context.userText.trim()
        if (text.isEmpty()) return AmarAgentResponse("اكتب طلبك وسأبدأ التحليل.")

        val generationRequested = text.contains("CODE_GENERATION=true", ignoreCase = true)
        val requestLine = text.lineSequence()
            .firstOrNull { it.startsWith("USER_REQUEST=") }
            ?.removePrefix("USER_REQUEST=")
            ?.trim()
            .orEmpty()

        val answer = if (generationRequested) {
            "AMAR AI: تم التعرف على طلب توليد الكود. المسار آمن ومسودة فقط.\n" +
                "المولد الفعلي يجب أن يكون مزود استدلال محليًا متوافقًا؛ لا أعتبر النص المولد ناجحًا أو قابلًا للتشغيل دون فحص/Build فعلي.\n" +
                if (requestLine.isNotBlank()) "المتطلب: $requestLine" else "أرسل المتطلب البرمجي بالتفصيل."
        } else {
            "AMAR AI: تم استقبال الطلب وتحليله ضمن حدود المحرك المحلي.\n" +
                "الوضع الحالي: تحليل وبحث ومحاكاة فقط، بدون تنفيذ صفقات أو تغييرات حساسة.\n" +
                if (requestLine.isNotBlank()) "الطلب: $requestLine" else "الطلب: $text"
        }

        return AmarAgentResponse(
            answer = answer,
            actions = context.tools.map { it.id }
        )
    }

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse = generate(context)
}
