package com.personal.gridbot.amaros.agent.reasoning

/** Produces one answer from multiple evidence streams while preserving dissent and uncertainty. */
class AmarUnifiedAnswerEngine {
    fun synthesize(request: AmarUnifiedAnswerRequest): AmarUnifiedAnswer {
        val valid = request.sources.filter { it.text.isNotBlank() }
        val support = valid.count { it.stance == AmarAnswerStance.SUPPORTS }
        val oppose = valid.count { it.stance == AmarAnswerStance.OPPOSES }
        val unknown = valid.size - support - oppose
        val confidence = if (valid.isEmpty()) 0.0 else ((support + oppose).toDouble() / valid.size).coerceIn(0.0, 1.0)
        val summary = when {
            valid.isEmpty() -> "لا توجد أدلة كافية لإجابة موحدة."
            support > oppose -> "الأدلة تميل إلى التأييد مع حفظ الاعتراضات."
            oppose > support -> "الأدلة تميل إلى الرفض مع حفظ الأدلة المؤيدة."
            else -> "الأدلة متوازنة أو متعارضة؛ يلزم تحقق إضافي."
        }
        return AmarUnifiedAnswer(request.question, summary, confidence, support, oppose, unknown, valid)
    }
}

data class AmarUnifiedAnswerRequest(val question: String, val sources: List<AmarAnswerEvidence>)
data class AmarAnswerEvidence(val sourceId: String, val text: String, val stance: AmarAnswerStance)
enum class AmarAnswerStance { SUPPORTS, OPPOSES, UNKNOWN }
data class AmarUnifiedAnswer(
    val question: String,
    val summary: String,
    val confidence: Double,
    val supporting: Int,
    val opposing: Int,
    val unknown: Int,
    val evidence: List<AmarAnswerEvidence>
)
