package com.personal.gridbot.amaros.agent

/** Provider-neutral reasoning contract. External hosted vendors are not part of the core. */
interface AmarReasoning {
    suspend fun generate(context: AmarAgentContext): AmarAgentResponse
}

/**
 * Deterministic local answer generator.
 *
 * It is intentionally not presented as an external LLM. It turns the current
 * request, plan context and available evidence into a useful response instead
 * of returning the old fixed placeholder sentence.
 */
class AmarLocalReasoning : AmarReasoning, AmarReasoningProvider {
    override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
        val text = context.userText.trim()
        if (text.isEmpty()) return AmarAgentResponse("اكتب سؤالك وسأبدأ التحليل.")

        val lines = mutableListOf<String>()
        lines += "AMAR AI"
        lines += "طلبك: $text"

        val evidence = extractEvidence(context.userText)
        if (evidence.isNotEmpty()) {
            lines += ""
            lines += "الأدلة المتاحة:"
            lines += evidence
        } else {
            lines += ""
            lines += "البيانات المتاحة حاليًا من السياق المحلي فقط؛ لا توجد أدلة خارجية موثقة مضافة لهذا الطلب."
        }

        lines += ""
        lines += when {
            context.requireCrossValidation ->
                "حالة التحقق: مطلوب تحقق متقاطع قبل اعتماد أي استنتاج."
            context.requireBacktestWhenApplicable ->
                "حالة التحقق: يلزم اختبار/محاكاة عندما يكون ذلك مناسبًا قبل اعتماد نتيجة تداولية."
            else ->
                "حالة التشغيل: تحليل محلي فقط."
        }

        lines += "التنفيذ: محجوب؛ هذا المسار لا يملك صلاحية تنفيذ صفقة."
        lines += ""
        lines += buildActionableResponse(text)

        return AmarAgentResponse(
            answer = lines.joinToString("\n"),
            actions = context.tools.map { it.id }
        )
    }

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse = generate(context)

    private fun extractEvidence(text: String): List<String> =
        text.lineSequence()
            .map { it.trim() }
            .filter { it.length >= 20 && !it.startsWith("Evidence summary:", ignoreCase = true) }
            .take(5)
            .toList()

    private fun buildActionableResponse(text: String): String {
        val q = text.lowercase()
        return when {
            listOf("سبب", "لماذا", "مشكلة", "خطأ", "error", "bug").any(q::contains) ->
                "الاستنتاج: سأتعامل مع الطلب كتشخيص للمشكلة، وأفصل بين السبب المثبت وما يحتاج إلى بيانات إضافية."
            listOf("تداول", "صفقة", "trade", "mt5", "ذهب", "xau").any(q::contains) ->
                "الاستنتاج: يمكن تحليل الاتجاه والمخاطر والسيناريوهات، لكن لا تُعتمد صفقة من دون بيانات سوق صالحة وحديثة."
            listOf("كود", "code", "برمجة", "app", "تطبيق").any(q::contains) ->
                "الاستنتاج: الطلب برمجي؛ سأبني الإجابة على الكود والسياق المتاح بدل إعطاء رد عام."
            else ->
                "الاستنتاج: تم تحليل صيغة الطلب محليًا، ويمكن متابعة الحوار على نفس السياق بدل إعادة رسالة ثابتة."
        }
    }
}
