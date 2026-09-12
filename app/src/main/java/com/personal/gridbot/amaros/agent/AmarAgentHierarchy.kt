package com.personal.gridbot.amaros.agent

/** Layered decision hierarchy. Each layer advises, challenges or verifies; no layer bypasses policy gates. */
enum class AmarAgentRole { DIRECTOR, ADVISOR, ANALYST, RESEARCHER, DECISION_CONFIRMATION, RISK_GUARD, EXECUTOR_FUTURE, AUDITOR }

data class AmarRoleMandate(
    val role: AmarAgentRole,
    val purpose: String,
    val canResearch: Boolean = false,
    val canRecommend: Boolean = false,
    val canApprove: Boolean = false,
    val canExecute: Boolean = false
)

class AmarAgentHierarchy {
    fun defaultMandates(): List<AmarRoleMandate> = listOf(
        AmarRoleMandate(AmarAgentRole.DIRECTOR, "تحديد المهمة وتوزيع العمل ودمج النتائج وإدارة التعارضات", canRecommend = true),
        AmarRoleMandate(AmarAgentRole.ADVISOR, "تقديم رأي مستقل واقتراح البدائل", canRecommend = true),
        AmarRoleMandate(AmarAgentRole.ANALYST, "تحليل البيانات الفنية والسوقية والاقتصادية", canResearch = true, canRecommend = true),
        AmarRoleMandate(AmarAgentRole.RESEARCHER, "جمع مصادر مستقلة والتحقق والمقارنة وكشف النقص", canResearch = true),
        AmarRoleMandate(AmarAgentRole.DECISION_CONFIRMATION, "مراجعة القرار النهائي مقابل الأدلة والتناقضات", canApprove = true),
        AmarRoleMandate(AmarAgentRole.RISK_GUARD, "رفض القرار إذا خالف حدود المخاطر والسلامة", canApprove = true),
        AmarRoleMandate(AmarAgentRole.EXECUTOR_FUTURE, "تنفيذ قرار معتمد فقط عبر بوابة تنفيذ مستقلة", canExecute = false),
        AmarRoleMandate(AmarAgentRole.AUDITOR, "تسجيل الأدلة والقرار والأسباب واكتشاف الانحرافات", canResearch = true)
    )
}

data class AmarAgentOpinion(
    val role: AmarAgentRole,
    val conclusion: String,
    val confidence: Double,
    val supportingEvidenceIds: List<String> = emptyList(),
    val opposingEvidenceIds: List<String> = emptyList(),
    val risks: List<String> = emptyList()
)

data class AmarDecisionReview(
    val opinions: List<AmarAgentOpinion>,
    val consensusScore: Double,
    val conflicts: List<String>,
    val approved: Boolean,
    val reason: String
)

class AmarDecisionCouncil(private val minimumConfidence: Double = 0.80) {
    fun review(opinions: List<AmarAgentOpinion>): AmarDecisionReview {
        if (opinions.isEmpty()) return AmarDecisionReview(emptyList(), 0.0, listOf("لا توجد آراء كافية"), false, "insufficient evidence")
        val averageConfidence = opinions.map { it.confidence.coerceIn(0.0, 1.0) }.average()
        val conclusions = opinions.map { it.conclusion.trim().lowercase() }.filter { it.isNotEmpty() }
        val conflicts = if (conclusions.distinct().size > 1) listOf("تعارض بين طبقات الوكيل") else emptyList()
        val approved = averageConfidence >= minimumConfidence && conflicts.isEmpty()
        return AmarDecisionReview(opinions, averageConfidence, conflicts, approved, if (approved) "decision supported" else "decision requires more verification")
    }
}
