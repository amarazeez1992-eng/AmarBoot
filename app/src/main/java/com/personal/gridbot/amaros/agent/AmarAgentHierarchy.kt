package com.personal.gridbot.amaros.agent

/**
 * Layered decision hierarchy.
 * Each layer advises, challenges or verifies; no layer bypasses policy gates.
 * Execution remains disabled here and must use a separate execution gateway.
 */
enum class AmarAgentRole {
    DIRECTOR,
    ADVISOR,
    ANALYST,
    RESEARCHER,
    DECISION_CONFIRMATION,
    RISK_GUARD,
    EXECUTOR_FUTURE,
    AUDITOR
}

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

/** Structured market direction used for conflict detection instead of comparing free-form prose. */
enum class AmarDecisionDirection {
    BUY,
    SELL,
    HOLD,
    UNKNOWN
}

data class AmarAgentOpinion(
    val role: AmarAgentRole,
    val conclusion: String,
    val confidence: Double,
    val direction: AmarDecisionDirection = AmarDecisionDirection.UNKNOWN,
    val supportingEvidenceIds: List<String> = emptyList(),
    val opposingEvidenceIds: List<String> = emptyList(),
    val risks: List<String> = emptyList()
)

data class AmarDecisionReview(
    val opinions: List<AmarAgentOpinion>,
    val consensusScore: Double,
    val conflicts: List<String>,
    val approved: Boolean,
    val reason: String,
    val directionCounts: Map<AmarDecisionDirection, Int> = emptyMap()
)

class AmarDecisionCouncil(private val minimumConfidence: Double = 0.80) {
    fun review(opinions: List<AmarAgentOpinion>): AmarDecisionReview {
        if (opinions.isEmpty()) {
            return AmarDecisionReview(
                opinions = emptyList(),
                consensusScore = 0.0,
                conflicts = listOf("لا توجد آراء كافية"),
                approved = false,
                reason = "insufficient evidence"
            )
        }

        val normalizedOpinions = opinions.map { it.copy(confidence = it.confidence.coerceIn(0.0, 1.0)) }
        val directionCounts = normalizedOpinions
            .groupingBy { it.direction }
            .eachCount()

        val actionableDirections = directionCounts.filterKeys {
            it == AmarDecisionDirection.BUY ||
                it == AmarDecisionDirection.SELL ||
                it == AmarDecisionDirection.HOLD
        }

        val conflicts = buildList {
            if (actionableDirections.keys.size > 1) {
                val directions = actionableDirections.keys.joinToString(" / ")
                add("تعارض اتجاهي واضح: $directions")
            }

            val highConfidenceOpposition = normalizedOpinions
                .filter { it.confidence >= minimumConfidence && it.direction != AmarDecisionDirection.UNKNOWN }
                .map { it.direction }
                .distinct()

            if (highConfidenceOpposition.size > 1) {
                add("تعارض بين آراء عالية الثقة: ${highConfidenceOpposition.joinToString(" / ")}")
            }
        }.distinct()

        val averageConfidence = normalizedOpinions.map { it.confidence }.average()
        val directionalOpinions = normalizedOpinions.filter {
            it.direction != AmarDecisionDirection.UNKNOWN
        }

        val consensusScore = if (directionalOpinions.isEmpty()) {
            0.0
        } else {
            val strongestCount = directionalOpinions
                .groupingBy { it.direction }
                .eachCount()
                .values
                .maxOrNull() ?: 0
            strongestCount.toDouble() / directionalOpinions.size.toDouble()
        }

        val approved = averageConfidence >= minimumConfidence && conflicts.isEmpty()
        val reason = when {
            approved -> "decision supported"
            conflicts.isNotEmpty() -> "decision requires conflict resolution"
            averageConfidence < minimumConfidence -> "decision requires higher confidence"
            else -> "decision requires more verification"
        }

        return AmarDecisionReview(
            opinions = normalizedOpinions,
            consensusScore = consensusScore,
            conflicts = conflicts,
            approved = approved,
            reason = reason,
            directionCounts = directionCounts
        )
    }
}
