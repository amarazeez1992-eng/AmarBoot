package com.personal.gridbot.amaros.agent

/**
 * AMAR-native reasoning kernel.
 *
 * No hosted model or external AI is invoked. The kernel performs deterministic
 * local language normalization, intent-aware evidence ranking, contradiction
 * handling and extractive synthesis. It never upgrades an unsupported claim
 * into a fact.
 */
interface AmarReasoning {
    suspend fun generate(context: AmarAgentContext): AmarAgentResponse
}

class AmarLocalReasoning(
    private val understanding: AmarIntentUnderstanding = AmarIntentUnderstanding()
) : AmarReasoning, AmarReasoningProvider {

    override suspend fun generate(context: AmarAgentContext): AmarAgentResponse {
        val request = context.userText.substringBefore("\n\nEvidence summary:").trim()
        if (request.isBlank()) {
            return AmarAgentResponse("اكتب طلبك وسأحلله عبر محرك AMAR الداخلي.")
        }

        val intent = understanding.understand(request).intent
        if (intent == AgentIntent.SYSTEM_IDENTITY) return AmarAgentResponse(answer = identityResponse(), actions = context.tools.map { it.id })
        if (intent == AgentIntent.SYSTEM_TIME) return AmarAgentResponse(answer = timeResponse(), actions = context.tools.map { it.id })
        if (intent == AgentIntent.SYSTEM_DATE) return AmarAgentResponse(answer = dateResponse(), actions = context.tools.map { it.id })
        if (intent == AgentIntent.SMALL_TALK) return AmarAgentResponse(answer = smallTalkResponse(), actions = context.tools.map { it.id })

        val evidenceBlock = context.userText
            .substringAfter("Evidence summary:", "")
            .substringBefore("Stage 2 deliberation:")
            .trim()

        val answer = when {
            isGreeting(request) -> greeting()
            evidenceBlock.isBlank() || evidenceBlock == "No external research required." ->
                synthesizeWithoutEvidence(request)
            else -> synthesizeFromEvidence(request, evidenceBlock)
        }

        return AmarAgentResponse(
            answer = answer,
            actions = context.tools.map { it.id }
        )
    }

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse =
        generate(context)

    private fun synthesizeFromEvidence(request: String, block: String): String {
        val records = parseEvidence(block)
        if (records.isEmpty()) return synthesizeWithoutEvidence(request)

        val ranked = records
            .map { it to relevance(request, it.evidence + " " + it.title) }
            .sortedByDescending { it.second }

        val best = ranked.filter { it.second > 0.0 }.take(4).map { it.first }
        val selected = if (best.isEmpty()) records.take(3) else best
        val direct = directEvidenceSentence(request, selected)

        val supporting = selected.count { it.stance.equals("SUPPORTS", true) }
        val opposing = selected.count { it.stance.equals("OPPOSES", true) }
        val confidenceLine = block.lineSequence()
            .firstOrNull { it.startsWith("confidence=", true) }

        return buildString {
            if (direct != null) {
                append(if (looksArabic(request)) "الإجابة حسب الدليل الموثق: " else "Answer from the verified evidence: ")
                append(direct.first)
                append("\n")
                append(if (looksArabic(request)) "المصدر: " else "Source: ")
                append(direct.second.title.ifBlank { direct.second.publisher })
                if (direct.second.publisher.isNotBlank() && direct.second.publisher != direct.second.title) {
                    append(" (").append(direct.second.publisher).append(")")
                }
                if (direct.second.uri.isNotBlank()) append(" — ").append(direct.second.uri)
                return@buildString
            }

            append(if (looksArabic(request)) "الإجابة حسب الأدلة المتاحة: " else "Answer from available evidence: ")
            append(if (supporting > opposing) {
                if (looksArabic(request)) "الأدلة المختارة تميل إلى دعم النتيجة." else "The selected evidence leans toward the supported conclusion."
            } else if (opposing > supporting) {
                if (looksArabic(request)) "الأدلة المختارة تميل إلى معارضة النتيجة." else "The selected evidence leans against the conclusion."
            } else {
                if (looksArabic(request)) "الأدلة لا تعطي اتجاهًا حاسمًا." else "The evidence does not establish a decisive direction."
            })

            if (confidenceLine != null) append("\n").append(confidenceLine)

            append(if (looksArabic(request)) "\n\nالأدلة الأقرب للسؤال:" else "\n\nMost relevant evidence:")
            selected.forEachIndexed { index, item ->
                append("\n").append(index + 1).append(". ")
                    .append(item.title.ifBlank { item.publisher })
                    .append(" — ").append(item.evidence.take(700))
            }

            if (supporting > 0 && opposing > 0) {
                append(if (looksArabic(request))
                    "\n\nتنبيه: توجد أدلة متعارضة، لذلك لا أعتبر النتيجة يقينية."
                    else "\n\nWarning: conflicting evidence is present, so the result is not treated as certain.")
            }
        }
    }

    private fun synthesizeWithoutEvidence(request: String): String =
        if (looksArabic(request)) {
            "الطلب: $request\n\n" +
                "AMAR لم يجد دليلًا موثوقًا كافيًا داخل هذه الدورة. لا سأخترع إجابة أو أقدّم حقيقة غير متحققة. " +
                "يمكنني تقديم تحليل منطقي محلي عندما تكون المعطيات موجودة، لكن الادعاء الواقعي يحتاج دليلًا داخل منظومة AMAR."
        } else {
            "Request: $request\n\n" +
                "AMAR does not have enough verified evidence in this cycle. I will not invent a factual answer. " +
                "Local reasoning can analyze supplied facts, while factual claims require evidence admitted by AMAR."
        }

    private fun parseEvidence(block: String): List<EvidenceRecord> {
        val records = mutableListOf<EvidenceRecord>()
        var current: EvidenceRecordBuilder? = null
        block.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("source=")) {
                current?.let { builder -> if (builder.evidence.isNotBlank()) records += builder.build() }
                current = EvidenceRecordBuilder(
                    title = field(line, "title"),
                    authority = field(line, "authority"),
                    stance = field(line, "stance"),
                    publisher = field(line, "publisher"),
                    uri = field(line, "uri")
                )
            } else if (line.startsWith("evidence=")) {
                current?.let { builder ->
                    builder.evidence = line.removePrefix("evidence=").trim()
                }
            }
        }
        current?.let { builder -> if (builder.evidence.isNotBlank()) records += builder.build() }
        return records
    }

    private fun field(line: String, name: String): String =
        Regex("(^|\\|)$name=([^|]*)").find(line)?.groupValues?.getOrNull(2).orEmpty()

    private fun relevance(query: String, evidence: String): Double {
        val q = tokens(query)
        if (q.isEmpty()) return 0.0
        val e = tokens(evidence)
        val overlap = q.intersect(e).size.toDouble() / q.size.toDouble()
        val exactPhrase = if (normalize(evidence).contains(normalize(query))) 0.35 else 0.0
        return (overlap + exactPhrase).coerceIn(0.0, 1.0)
    }

    private fun directEvidenceSentence(
        query: String,
        records: List<EvidenceRecord>
    ): Pair<String, EvidenceRecord>? {
        val queryTokens = tokens(query)
        if (queryTokens.isEmpty()) return null
        val requiredOverlap = if (queryTokens.size >= 2) 2 else 1
        return records.asSequence()
            .flatMap { record ->
                record.evidence
                    .split(Regex("(?<=[.!؟])\\s+"))
                    .asSequence()
                    .map { sentence -> sentence.trim() to record }
            }
            .map { (sentence, record) ->
                val overlap = queryTokens.intersect(tokens(sentence)).size
                Triple(sentence, record, overlap)
            }
            .filter { it.first.isNotBlank() && it.third >= requiredOverlap }
            .maxByOrNull { it.third }
            ?.let { it.first to it.second }
    }

    private fun tokens(text: String): Set<String> =
        normalize(text).split(" ").filter { it.length >= 3 }.toSet()

    private fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
            .replace(Regex("[^\\p{L}\\p{Nd}]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun looksArabic(text: String): Boolean =
        text.any { it in '\u0600'..'\u06FF' }

    private fun isGreeting(text: String): Boolean {
        val analysis = understanding.understand(text.trim())
        return analysis.intent == AgentIntent.GENERAL && analysis.questionForm == "GREETING"
    }

    private fun smallTalkResponse(): String =
        "أنا بخير، شكرًا لسؤالك. كيف يمكنني مساعدتك؟"

    private fun identityResponse(): String =
        "أنا AMAR AI Agent. أنا وكيل ذكاء اصطناعي محلي ضمن منظومة AMAR: أفهم الطلب، أحدد مساره، أستخدم الأدلة عندما تكون مطلوبة، وأفصل بين المعلومات العادية والتحليل المالي الصارم. لا أملك صلاحية تنفيذ صفقات أو الوصول المباشر إلى حساب الوساطة."

    private fun timeResponse(): String {
        val now = java.time.LocalDateTime.now()
        return "الوقت الآن حسب ساعة الجهاز: " + now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    private fun dateResponse(): String {
        val now = java.time.LocalDate.now()
        val weekday = now.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("ar"))
        return "اليوم هو $weekday، والتاريخ هو " + now.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun greeting(): String =
        "أهلاً بك. أنا AMAR AI Agent. أستقبل الطلب، أفهمه، وأبني الإجابة من المعطيات والأدلة التي يسمح بها نظام AMAR."

    private data class EvidenceRecord(
        val title: String,
        val authority: String,
        val stance: String,
        val publisher: String,
        val uri: String,
        val evidence: String
    )

    private class EvidenceRecordBuilder(
        val title: String,
        val authority: String,
        val stance: String,
        val publisher: String,
        val uri: String
    ) {
        var evidence: String = ""
        fun build() = EvidenceRecord(title, authority, stance, publisher, uri, evidence)
    }
}
