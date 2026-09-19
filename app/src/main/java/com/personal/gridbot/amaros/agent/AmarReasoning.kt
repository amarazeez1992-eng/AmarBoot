package com.personal.gridbot.amaros.agent

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

interface AmarReasoning {
    suspend fun generate(context: AmarAgentContext): AmarAgentResponse
}

class AmarLocalReasoning : AmarReasoning, AmarReasoningProvider {
    override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
        val request = context.userText.substringBefore("\n\nEvidence summary:").trim()
        if (request.isEmpty()) {
            return AmarAgentResponse(answer = "اكتب طلبك وسأتعامل معه مباشرة.")
        }

        val lower = request.lowercase(Locale.ROOT)
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val day = now.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))

        val conversational = when {
            lower.matches(Regex(".*\\b(كيف حالك|شلونك|شخبارك|how are you)\\b.*")) ->
                "أنا بخير وجاهز للعمل معك. اسألني ما تريد، وسأجيب ضمن البيانات والقدرات المتاحة للمحرك."
            lower.matches(Regex(".*\\b(من أنت|من انت|عرف نفسك|من تكون|who are you)\\b.*")) ->
                "أنا AMAR AI Agent، وكيل المشروع. أفهم الطلب، أحدد نوع المهمة، وأمررها عبر التخطيط والتحليل والتحقق والنقد قبل صياغة الإجابة."
            lower.matches(Regex(".*\\b(الوقت|الساعة|كم الوقت|what time)\\b.*")) ->
                "الوقت الحالي على جهاز التشغيل هو $time."
            lower.matches(Regex(".*\\b(التاريخ|كم التاريخ|ما التاريخ|what date)\\b.*")) ->
                "تاريخ جهاز التشغيل هو $date."
            lower.matches(Regex(".*\\b(اليوم|ما هو اليوم|اي يوم|أي يوم|what day)\\b.*")) ->
                "اليوم هو $day."
            else -> null
        }

        val evidenceBlock = context.userText.substringAfter("Evidence summary:", "").substringBefore("Stage 2 deliberation:").trim()
        val answer = conversational ?: if (evidenceBlock.isBlank() || evidenceBlock == "No external research required.") {
            buildString {
                append("فهمت طلبك: ")
                append(request)
                append("\n\n")
                append("لا توجد نتائج بحث خارجية في هذه الدورة، لذلك لن أختلق معلومة غير متاحة.")
            }
        } else {
            buildString {
                append("نتيجة AMAR AI للطلب: ")
                append(request)
                append("\n\n")
                append("بيانات البحث والتحقق المتاحة:\n")
                append(evidenceBlock)
                append("\n\n")
                append("هذه النتيجة مبنية فقط على البيانات التي وصلت فعليًا إلى المحرك.")
            }
        }

        return AmarAgentResponse(answer = answer, actions = context.tools.map { it.id })
    }

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse = generate(context)
}
