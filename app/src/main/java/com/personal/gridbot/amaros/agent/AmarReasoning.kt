package com.personal.gridbot.amaros.agent

/**
 * AMAR-native reasoning contract.
 *
 * This implementation is intentionally self-contained: no hosted LLM, Gemini,
 * GPT, or external AI service is called. The orchestrator remains responsible
 * for research, verification, consensus and safety; this kernel performs local
 * linguistic synthesis over the supplied request and evidence.
 */
interface AmarReasoning {
    suspend fun generate(context: AmarAgentContext): AmarAgentResponse
}

/**
 * Local cognitive synthesis kernel.
 *
 * It does not pretend to be a neural model. It extracts the user's request,
 * structures the available evidence, distinguishes support/opposition/unknown,
 * and produces a deterministic answer from information actually supplied by
 * AMAR's own pipeline.
 */
class AmarLocalReasoning : AmarReasoning, AmarReasoningProvider {
    override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
        val request = context.userText
            .substringBefore("\n\nEvidence summary:")
            .trim()

        if (request.isBlank()) {
            return AmarAgentResponse(
                answer = "اكتب طلبك وسأحلله عبر محرك AMAR الداخلي."
            )
        }

        val evidenceBlock = context.userText
            .substringAfter("Evidence summary:", "")
            .substringBefore("Stage 2 deliberation:")
            .trim()

        val isGreeting = isGreeting(request)
        val answer = when {
            isGreeting -> greeting()
            evidenceBlock.isBlank() || evidenceBlock == "No external research required." ->
                synthesizeWithoutExternalEvidence(request)
            else ->
                synthesizeFromEvidence(request, evidenceBlock)
        }

        return AmarAgentResponse(
            answer = answer,
            actions = context.tools.map { it.id }
        )
    }

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse =
        generate(context)

    private fun isGreeting(text: String): Boolean {
        val normalized = text.trim().lowercase()
        return listOf(
            "هلو", "مرحبا", "مرحباً", "السلام عليكم",
            "hello", "hi", "hey"
        ).any { normalized == it || normalized.startsWith("$it ") }
    }

    private fun greeting(): String =
        "أهلاً بك. أنا AMAR AI Agent. أستقبل طلبك، أبني مساره داخلياً، وأعتمد فقط على البيانات والأدلة التي تدخل منظومة AMAR."

    private fun synthesizeWithoutExternalEvidence(request: String): String =
        buildString {
            append("الطلب: ").append(request)
            append("\n\n")
            append("تحليل AMAR الداخلي: الطلب استُقبل ويُعالج داخل الوكيل المركزي دون الاستعانة بعقل ذكاء اصطناعي خارجي.")
            append("\n")
            append("حالة الأدلة الخارجية: لا توجد أدلة خارجية في هذه الدورة.")
            append("\n")
            append("النتيجة: لا سأخترع معلومة غير موجودة. إذا كان الطلب يحتاج حقيقة حديثة، يجب أن تمر بياناتها عبر محرك البحث والأدلة الخاص بـAMAR ثم تُتحقق قبل اعتمادها.")
        }

    private fun synthesizeFromEvidence(request: String, block: String): String {
        val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
        val sourceLines = lines.filter {
            it.startsWith("source=", true) ||
            it.startsWith("evidence=", true) ||
            it.startsWith("title=", true)
        }

        val support = lines.filter { it.contains("supporting=", true) }.firstOrNull()
        val oppose = lines.filter { it.contains("opposing=", true) }.firstOrNull()
        val confidence = lines.firstOrNull { it.startsWith("confidence=", true) }

        return buildString {
            append("الطلب: ").append(request)
            append("\n\n")
            append("استنتاج AMAR: تم تمرير الطلب عبر البحث والأدلة والتحقق قبل هذه المرحلة.")
            if (confidence != null) append("\n").append(confidence)
            if (support != null) append("\n").append(support)
            if (oppose != null) append("\n").append(oppose)

            if (sourceLines.isNotEmpty()) {
                append("\n\nالأدلة المتاحة:")
                sourceLines.take(12).forEach { append("\n• ").append(it) }
            }

            append("\n\nالحكم: أي نتيجة لا تسندها الأدلة المتاحة تبقى غير مؤكدة، ولا يعتمد AMAR ادعاءً لمجرد وجوده في السؤال.")
        }
    }
}
