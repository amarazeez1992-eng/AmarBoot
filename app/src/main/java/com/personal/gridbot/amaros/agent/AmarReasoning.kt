package com.personal.gridbot.amaros.agent

/** Provider-neutral internal reasoning contract. */
interface AmarReasoning {
    suspend fun generate(context: AmarAgentContext): AmarAgentResponse
}

/**
 * AMAR internal synthesis engine.
 * It consumes outputs already produced by AMAR internal stages and does not
 * call or depend on any hosted LLM. Missing evidence is never fabricated.
 */
class AmarLocalReasoning : AmarReasoning, AmarReasoningProvider {
    override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
        val request = context.userText.substringBefore("\n\nEvidence summary:").trim()
        if (request.isBlank()) return AmarAgentResponse("اكتب سؤالك وسأبدأ التحليل.")

        val evidence = value(context.userText, "unifiedEvidence=").toIntOrNull() ?: 0
        val independent = value(context.userText, "independentSources=").toIntOrNull() ?: 0
        val verified = value(context.userText, "verificationAccepted=").equals("true", true)
        val consensus = value(context.userText, "consensus=").toDoubleOrNull() ?: 0.0
        val support = value(context.userText, "supporting=").toIntOrNull() ?: 0
        val oppose = value(context.userText, "opposing=").toIntOrNull() ?: 0
        val conflicts = context.userText.lineSequence().filter { it.trim().startsWith("conflict=") }.toList()
        val chosen = value(context.userText, "chosenDirection=")
        val confidenceValues = context.userText.lineSequence().filter { it.trim().startsWith("confidence=") }.mapNotNull { it.substringAfter("=").toDoubleOrNull() }.toList()
        val confidence = confidenceValues.firstOrNull() ?: 0.0
        val researchRequired = context.requireCrossValidation && context.userText.contains("Evidence summary:")
        val blocked = researchRequired && (!verified || evidence == 0)

        val lines = mutableListOf<String>()
        lines += "AMAR AI — Internal Agent Core"
        lines += "الطلب: $request"
        lines += "المسار الداخلي: فهم → تخطيط → بحث → أدلة → تحقق → توافق → نقد → قرار → استجابة"
        lines += if (evidence > 0) "الأدلة المستخدمة: $evidence | مصادر مستقلة: $independent" else "الأدلة الخارجية: غير متاحة في هذه الدورة."
        if (evidence > 0) lines += "التوافق: ${"%.2f".format(consensus)} | دعم=$support | معارضة=$oppose"
        if (conflicts.isNotEmpty()) lines += "التعارضات: ${conflicts.joinToString(" | ")}"

        if (blocked) {
            lines += "الحالة: BLOCKED — بوابة الأدلة/التحقق لم تُفتح."
            lines += "لا يحول AMAR نقص الأدلة إلى استنتاج مثبت."
        } else if (chosen.isNotBlank() && chosen != "UNKNOWN") {
            lines += "القرار الداخلي: $chosen"
            lines += "ثقة القرار: ${"%.2f".format(confidence)}"
        } else {
            lines += "الاستنتاج: لا يوجد اتجاه حاسم مثبت من السياق الحالي."
        }

        lines += "النقد: ${if (conflicts.isEmpty()) "لا يوجد تعارض مسجل." else "التعارضات مسجلة ولم تُخفَ."}"
        lines += "التنفيذ: محجوب؛ AMAR Agent لا يملك صلاحية وسيط التداول."

        return AmarAgentResponse(lines.joinToString("\n"), if (blocked) AmarAgentResponse.Status.ERROR else AmarAgentResponse.Status.READY, context.tools.map { it.id })
    }

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse = generate(context)

    private fun value(text: String, key: String): String = text.lineSequence().firstOrNull { it.trim().startsWith(key) }?.substringAfter("=")?.trim().orEmpty()
}