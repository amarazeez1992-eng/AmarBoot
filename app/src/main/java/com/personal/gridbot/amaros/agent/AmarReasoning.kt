package com.personal.gridbot.amaros.agent

/** Provider-neutral reasoning contract. External hosted vendors are not part of the core. */
interface AmarReasoning {
    suspend fun generate(context: AmarAgentContext): AmarAgentResponse
}

/**
 * Deterministic internal synthesis layer.
 *
 * It is deliberately local: it does not call a hosted LLM and it does not invent
 * market evidence. The orchestrator supplies the planning/evidence/verification
 * context; this layer turns that context into a readable response.
 */
class AmarLocalReasoning : AmarReasoning, AmarReasoningProvider {
    override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
        val request = context.userText.trim()
        if (request.isEmpty()) {
            return AmarAgentResponse(
                answer = "اكتب طلبك وسأفهمه ثم أرتّب خطوات التحليل وأوضح ما يمكن إثباته وما يحتاج إلى بيانات إضافية."
            )
        }

        val lower = request.lowercase()
        val evidenceBlock = context.userText
            .substringAfter("Evidence summary:", "")
            .substringBefore("Stage 2 deliberation:")
            .trim()

        val isGreeting = listOf("هلو", "مرحبا", "مرحباً", "السلام عليكم", "hello", "hi")
            .any { lower == it || lower.startsWith("$it ") }

        val answer = when {
            isGreeting -> "أهلاً بك. أنا AMAR AI Agent. أستطيع فهم الطلب، ترتيب خطواته، تحليل الأدلة المتاحة، التحقق منها، ثم إعطائك نتيجة واضحة مع بيان ما هو مؤكد وما يزال غير متحقق."
            evidenceBlock.isBlank() || evidenceBlock == "No external research required." ->
                buildString {
                    append("فهمت طلبك: ")
                    append(request)
                    append("\n\n")
                    append("سأتعامل معه عبر المسار الداخلي: فهم الطلب → التخطيط → التحليل → التحقق → النقد → القرار → صياغة الإجابة.")
                    append("\nلا توجد في هذه الدورة أدلة خارجية مقدمة للمحرك، لذلك لن أختلق مصادر أو نتائج غير متاحة.")
                }
            else ->
                buildString {
                    append("فهمت طلبك: ")
                    append(request)
                    append("\n\n")
                    append("تم تمرير الطلب عبر محركات AMAR الداخلية، وهذه هي حالة الأدلة المتاحة:")
                    append("\n")
                    append(evidenceBlock)
                    append("\n\n")
                    append("النتيجة أعلاه تصف ما وصل فعلياً إلى المحرك؛ أي معلومة غير مدعومة ببيانات متاحة تبقى غير مؤكدة.")
                }
        }

        return AmarAgentResponse(
            answer = answer,
            actions = context.tools.map { it.id }
        )
    }

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse = generate(context)
}
