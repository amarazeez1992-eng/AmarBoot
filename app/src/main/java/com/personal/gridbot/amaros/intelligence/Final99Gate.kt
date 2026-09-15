package com.personal.gridbot.amaros.intelligence

/**
 * Stage 11 Item 9: independent, fail-closed certification gate.
 *
 * A numeric score never overrides a hard blocker. This class is certification logic only
 * and has no broker, order, or live-execution authority.
 */
class Final99Gate(
    private val requiredScorePercent: Double = 99.0
) {
    init {
        require(requiredScorePercent in 0.0..100.0)
    }

    data class EvidenceDimension(
        val name: String,
        val weightPercent: Double,
        val scorePercent: Double,
        val evidenceCurrent: Boolean = true,
        val valid: Boolean = true,
        val blocking: Boolean = false
    ) {
        init {
            require(name.isNotBlank())
            require(weightPercent >= 0.0)
            require(scorePercent in 0.0..100.0)
        }
    }

    data class CertificationInput(
        val dimensions: List<EvidenceDimension>,
        val mandatoryEvidenceComplete: Boolean,
        val regressionClean: Boolean,
        val buildVerified: Boolean,
        val architectureVerified: Boolean,
        val safetyBoundaryVerified: Boolean,
        val finalAuditPassed: Boolean,
        val closureChainComplete: Boolean
    )

    data class CertificationResult(
        val scorePercent: Double,
        val certified: Boolean,
        val blockers: List<String>
    )

    fun certify(input: CertificationInput): CertificationResult {
        val blockers = buildList {
            if (input.dimensions.isEmpty()) add("no_dimensions")
            if (!input.mandatoryEvidenceComplete) add("mandatory_evidence_incomplete")
            if (!input.regressionClean) add("regression_not_clean")
            if (!input.buildVerified) add("build_not_verified")
            if (!input.architectureVerified) add("architecture_not_verified")
            if (!input.safetyBoundaryVerified) add("safety_boundary_not_verified")
            if (!input.finalAuditPassed) add("final_audit_failed")
            if (!input.closureChainComplete) add("closure_chain_incomplete")

            input.dimensions.forEach { dimension ->
                if (!dimension.evidenceCurrent) add("stale_evidence:${dimension.name}")
                if (!dimension.valid) add("invalid_evidence:${dimension.name}")
                if (dimension.blocking) add("explicit_blocker:${dimension.name}")
            }
        }

        val totalWeight = input.dimensions.sumOf { it.weightPercent }
        val weightedScore = if (totalWeight == 0.0) {
            0.0
        } else {
            input.dimensions.sumOf {
                it.scorePercent * (it.weightPercent / totalWeight)
            }
        }

        val score = weightedScore.coerceIn(0.0, 100.0)
        val certified = blockers.isEmpty() && score >= requiredScorePercent
        if (!certified && blockers.isEmpty()) {
            return CertificationResult(score, false, listOf("score_below_threshold"))
        }
        return CertificationResult(score, certified, blockers.distinct())
    }
}
