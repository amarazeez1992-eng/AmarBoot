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
        val request = context.userText
            .substringBefore("\n\nEvidence summary:")
            .trim()
        if (request.isEmpty()) {
            return AmarAgentResponse(
                answer = "اكتب طلبك وسأجيبك مباشرة."
            )
        }

        val normalized = request
            .lowercase()
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ى", "ي")
            .replace("ة", "ه")
            .replace("ؤ", "و")
            .replace("ئ", "ي")
            .replace(Regex("[ًٌٍَُِّْـ]"), "")
            .replace(Regex("[؟?!.,،؛:]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")

        val evidenceBlock = context.userText
            .substringAfter("Evidence summary:", "")
            .substringBefore("Stage 2 deliberation:")
            .trim()

        val isGreeting = normalized in setOf(
            "هلو", "مرحبا", "اهلا", "السلام عليكم", "السلام عليكم ورحمة الله وبركاته",
            "hello", "hi", "hey"
        )

        val isIdentityQuestion = normalized in setOf(
            "من انت", "من تكون", "ما اسمك", "شنو اسمك", "من هو amar ai",
            "من هو عمار اي اي"
        )

        val isHowAreYouQuestion = normalized in setOf(
            "كيف حالك", "شلونك", "شخبارك", "كيفك", "كيف انت"
        )

        val isTimeQuestion = normalized.contains("كم الوقت") ||
            normalized.contains("الوقت الان") || normalized == "كم الساعة" ||
            normalized == "الساعة كم" || normalized.contains("ما هو الوقت")

        val isDateQuestion = normalized.contains("ما هو اليوم") ||
            normalized.contains("ما اليوم") || normalized.contains("اي يوم") ||
            normalized.contains("التاريخ اليوم") || normalized.contains("كم التاريخ") ||
            normalized.contains("ما هو التاريخ") || normalized == "التاريخ"

        val isCapabilityQuestion = normalized in setOf(
            "ماذا تستطيع", "ماذا تستطيع ان تفعل", "ماذا يمكنك ان تفعل",
            "ما الذي تستطيع فعله", "شنو تقدر تسوي", "شنو تستطيع تسوي",
            "ما هي قدراتك", "ما هي وظيفتك", "ماذا تفعل"
        )

        val now = java.time.ZonedDateTime.now()
        val timeText = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
        val dateText = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val dayText = now.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL,
            java.util.Locale("ar")
        )

        val answer = when {
            isGreeting ->
                "أهلاً بك 👋 أنا AMAR AI Agent. كيف أستطيع مساعدتك؟"

            isHowAreYouQuestion ->
                "أنا بخير وجاهز لمساعدتك. ماذا تريد أن تسألني؟"

            isTimeQuestion ->
                "الوقت الآن: $timeText."

            isDateQuestion ->
                "اليوم هو: $dayText، والتاريخ: $dateText."

            isIdentityQuestion ->
                "أنا AMAR AI Agent، الوكيل الذكي داخل مشروع AMAR AI. أستطيع فهم طلبك، تنفيذ ما تسمح به أدوات النظام، وتحليل المعلومات المتاحة ثم إعطائك جواباً واضحاً."

            isCapabilityQuestion ->
                "أستطيع فهم أسئلتك وطلباتك، ترتيب خطوات العمل، تحليل المعلومات والأدلة المتاحة، التحقق منها، ثم تقديم النتيجة بوضوح. وإذا كانت المعلومة غير متاحة أو غير مؤكدة فسأوضح ذلك بدلاً من اختلاقها."

            evidenceBlock.isBlank() || evidenceBlock == "No external research required." ->
                "فهمت طلبك: $request\n\nسأعالجه وفق قدرات AMAR AI المتاحة، ولن أختلق مصادر أو نتائج غير متاحة."

            else ->
                "فهمت طلبك: $request\n\nحالة الأدلة المتاحة:\n$evidenceBlock\n\nأي معلومة غير مدعومة ببيانات متاحة تبقى غير مؤكدة."
        }

        return AmarAgentResponse(
            answer = answer,
            actions = context.tools.map { it.id }
        )
    }

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse = generate(context)
}
