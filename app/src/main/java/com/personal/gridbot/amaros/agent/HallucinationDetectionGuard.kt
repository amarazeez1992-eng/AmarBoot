package com.personal.gridbot.amaros.agent

const val MINIMUM_SOURCE_COUNT = 3

data class HallucinationDetectionResult(
    val hallucinationDetected: Boolean,
    val reasons: List<HallucinationIndicator>,
    val severity: HallucinationSeverity
)

enum class HallucinationIndicator {
    CERTAINTY_LANGUAGE_MISMATCH,
    NUMERIC_CLAIM_WITHOUT_EVIDENCE,
    ENTITY_MENTION_MISMATCH,
    CONFIDENCE_INFLATION,
    REPETITION_SUSPICION
}

enum class HallucinationSeverity { NONE, LOW, MEDIUM, HIGH }

object HallucinationDetectionGuard {
    private val certaintyTerms = listOf(
        "definitely", "guaranteed", "guarantee", "certainly", "will",
        "مؤكد", "بالتأكيد", "مضمون", "سوف", "حتماً", "حتما", "قطعاً", "قطعا", "لا شك"
    )

    fun evaluate(
        report: AmarClaimVerificationReport,
        blockingResult: BlockingResult,
        attributionResult: AttributionResult,
        finalAnswer: String
    ): HallucinationDetectionResult {
        val reasons = linkedSetOf<HallucinationIndicator>()
        val claims = report.claims
        val normalizedClaims = claims.map { normalize(it.claim) }

        if (hasCertaintyMismatch(claims, finalAnswer)) {
            reasons += HallucinationIndicator.CERTAINTY_LANGUAGE_MISMATCH
        }
        if (hasNumericClaimWithoutEvidence(claims)) {
            reasons += HallucinationIndicator.NUMERIC_CLAIM_WITHOUT_EVIDENCE
        }
        if (hasEntityMentionMismatch(claims)) {
            reasons += HallucinationIndicator.ENTITY_MENTION_MISMATCH
        }

        val availableMatchedEvidence = claims.sumOf { it.matchedEvidence }
        if (report.accepted && availableMatchedEvidence < MINIMUM_SOURCE_COUNT) {
            reasons += HallucinationIndicator.CONFIDENCE_INFLATION
        }

        if (normalizedClaims.filter { it.isNotBlank() }.groupingBy { it }.eachCount().values.any { it > 1 }) {
            reasons += HallucinationIndicator.REPETITION_SUSPICION
        }

        @Suppress("UNUSED_VARIABLE")
        val upstreamSignals = blockingResult.blocked to attributionResult.attributed

        val orderedReasons = HallucinationIndicator.values().filter { it in reasons }
        return HallucinationDetectionResult(
            hallucinationDetected = orderedReasons.isNotEmpty(),
            reasons = orderedReasons,
            severity = severityOf(orderedReasons)
        )
    }

    private fun hasCertaintyMismatch(claims: List<AmarClaimVerification>, finalAnswer: String): Boolean {
        if (finalAnswer.isBlank()) return false
        return claims.any { claim ->
            containsCertaintyLanguage(claim.claim) && claim.matchedEvidence < MINIMUM_SOURCE_COUNT
        }
    }

    private fun hasNumericClaimWithoutEvidence(claims: List<AmarClaimVerification>): Boolean =
        claims.any { it.claim.contains(Regex("\\d")) && it.matchedEvidence == 0 }

    private fun hasEntityMentionMismatch(claims: List<AmarClaimVerification>): Boolean =
        claims.any { claim -> claim.matchedEvidence == 0 && containsEntityCandidate(claim.claim) }

    private fun containsCertaintyLanguage(text: String): Boolean {
        val normalized = normalize(text)
        return certaintyTerms.any { term -> normalized.contains(normalize(term)) }
    }

    private fun containsEntityCandidate(text: String): Boolean {
        val tokens = text.split(Regex("[^\\p{L}\\p{N}@._-]+"))
            .map { it.trim('.', ',', ';', ':', '!', '?', '؟', '،') }
            .filter { it.length >= 3 }
        return tokens.withIndex().any { (index, token) ->
            index > 0 && (token.startsWith("@") || token.contains(".") || token.firstOrNull()?.isUpperCase() == true)
        }
    }

    private fun normalize(text: String): String =
        text.trim().replace(Regex("\\s+"), " ").trimEnd('.', '!', '?', '؟').lowercase()

    private fun severityOf(reasons: List<HallucinationIndicator>): HallucinationSeverity {
        if (reasons.isEmpty()) return HallucinationSeverity.NONE
        val strongCount = reasons.count {
            it == HallucinationIndicator.CERTAINTY_LANGUAGE_MISMATCH ||
                it == HallucinationIndicator.NUMERIC_CLAIM_WITHOUT_EVIDENCE ||
                it == HallucinationIndicator.ENTITY_MENTION_MISMATCH ||
                it == HallucinationIndicator.CONFIDENCE_INFLATION
        }
        return when {
            strongCount >= 2 -> HallucinationSeverity.HIGH
            strongCount == 1 -> HallucinationSeverity.MEDIUM
            else -> HallucinationSeverity.LOW
        }
    }
}
