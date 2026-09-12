package com.personal.gridbot.amaros.agent

/** Produces role-specific opinions without granting execution authority. */
class AmarRoleOpinionEngine {
    fun buildOpinions(answer: AmarAgentResponse, direction: AmarDecisionDirection, confidence: Double, evidence: List<ResearchFinding>): List<AmarAgentOpinion> {
        val c = confidence.coerceIn(0.0, 1.0)
        val opposing = evidence.any { it.stance == EvidenceStance.OPPOSES }
        val risks = mutableListOf<String>()
        if (evidence.isEmpty()) risks += "no_external_evidence"
        if (opposing) risks += "opposing_evidence_present"
        risks += "execution_disabled"
        return listOf(
            AmarAgentOpinion(AmarAgentRole.ANALYST, answer.answer, c, direction,
                evidence.filter { it.stance == EvidenceStance.SUPPORTS }.map { it.fingerprint },
                evidence.filter { it.stance == EvidenceStance.OPPOSES }.map { it.fingerprint }, risks),
            AmarAgentOpinion(AmarAgentRole.ADVISOR,
                "مراجعة بديلة للاتجاه ${direction.name} مع إبقاء الأدلة المعارضة قيد المراجعة.",
                (c * 0.95).coerceIn(0.0, 1.0),
                if (opposing) AmarDecisionDirection.HOLD else direction,
                risks = risks),
            AmarAgentOpinion(AmarAgentRole.RISK_GUARD,
                "فحص المخاطر: لا يُسمح بتحويل الرأي إلى تنفيذ مباشر.",
                (if (opposing) c * 0.80 else c).coerceIn(0.0, 1.0),
                if (opposing) AmarDecisionDirection.HOLD else direction,
                risks = risks)
        )
    }
}
