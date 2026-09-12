package com.personal.gridbot.amaros.agent

/** Combines independent findings while preserving disagreement instead of hiding it. */
class AmarAnswerSynthesisEngine {
    fun synthesize(findings: List<AmarSynthesisFinding>): AmarSynthesisResult {
        if (findings.isEmpty()) return AmarSynthesisResult("لا توجد أدلة كافية.", 0.0, emptyList())
        val support = findings.count { it.stance == EvidenceStance.SUPPORTS }
        val oppose = findings.count { it.stance == EvidenceStance.OPPOSES }
        val unknown = findings.size - support - oppose
        val confidence = if (findings.isEmpty()) 0.0 else
            ((support + oppose).toDouble() / findings.size.toDouble()) *
                findings.map { it.authorityWeight }.average().coerceIn(0.0, 1.0)
        val conflicts = if (support > 0 && oppose > 0) listOf("supporting_and_opposing_evidence") else emptyList()
        val summary = "supporting=$support, opposing=$oppose, unknown=$unknown, confidence=${"%.3f".format(confidence)}"
        return AmarSynthesisResult(summary, confidence, conflicts)
    }
}

data class AmarSynthesisFinding(
    val stance: EvidenceStance,
    val authorityWeight: Double = 0.5,
    val sourceId: String = ""
)

data class AmarSynthesisResult(
    val summary: String,
    val confidence: Double,
    val conflicts: List<String>
)
