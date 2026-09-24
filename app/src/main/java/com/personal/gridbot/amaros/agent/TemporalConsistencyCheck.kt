package com.personal.gridbot.amaros.agent

// Precondition: timestamps are validated by upstream Data Quality.
// Addition 4 does not re-validate raw timestamps.

data class TemporalEvidenceRecord(
    val claimId: String,
    val claimReferenceTimestamp: Long?,
    val evidenceFingerprint: String,
    val evidenceEventTimestamp: Long?,
    val evidenceRetrievedAtEpochMs: Long?
)

enum class TemporalConsistencyStatus {
    TEMPORALLY_CONSISTENT,
    TEMPORALLY_INCONSISTENT,
    INSUFFICIENT_TEMPORAL_DATA,
    FUTURE_TIMESTAMP
}

data class TemporalConsistencyResult(
    val status: TemporalConsistencyStatus,
    val flaggedClaims: List<String>,
    val insufficientClaims: List<String>,
    val futureClaims: List<String>,
    val upstreamHallucinationDetected: Boolean
)

object TemporalConsistencyCheck {
    fun evaluate(
        report: AmarClaimVerificationReport,
        temporalEvidence: List<TemporalEvidenceRecord>,
        hallucinationResult: HallucinationDetectionResult,
        evaluationTimeEpochMs: Long
    ): TemporalConsistencyResult {
        if (evaluationTimeEpochMs < 0L) {
            return insufficient(report, hallucinationResult)
        }

        if (temporalEvidence.isEmpty()) {
            return insufficient(report, hallucinationResult)
        }

        val reportClaimIds = report.claims.indices.map { "answer-claim-$it" }.toSet()
        val relevant = temporalEvidence.filter { it.claimId in reportClaimIds }

        if (relevant.isEmpty()) {
            return insufficient(report, hallucinationResult)
        }

        val flagged = linkedSetOf<String>()
        val insufficient = linkedSetOf<String>()
        val future = linkedSetOf<String>()

        relevant.forEach { record ->
            val claimId = record.claimId
            val claimTime = record.claimReferenceTimestamp
            val evidenceEventTime = record.evidenceEventTimestamp
            val evidenceRetrievedAt = record.evidenceRetrievedAtEpochMs

            if (claimTime == null || evidenceEventTime == null || evidenceRetrievedAt == null) {
                insufficient += claimId
                return@forEach
            }

            if (evidenceRetrievedAt > evaluationTimeEpochMs ||
                evidenceEventTime > evaluationTimeEpochMs ||
                claimTime > evaluationTimeEpochMs
            ) {
                future += claimId
                return@forEach
            }

            if (evidenceEventTime > claimTime) {
                flagged += claimId
            }
        }

        return when {
            future.isNotEmpty() -> TemporalConsistencyResult(
                TemporalConsistencyStatus.FUTURE_TIMESTAMP,
                flagged.toList(),
                insufficient.toList(),
                future.toList(),
                hallucinationResult.hallucinationDetected
            )
            flagged.isNotEmpty() -> TemporalConsistencyResult(
                TemporalConsistencyStatus.TEMPORALLY_INCONSISTENT,
                flagged.toList(),
                insufficient.toList(),
                emptyList(),
                hallucinationResult.hallucinationDetected
            )
            insufficient.isNotEmpty() -> TemporalConsistencyResult(
                TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA,
                emptyList(),
                insufficient.toList(),
                emptyList(),
                hallucinationResult.hallucinationDetected
            )
            else -> TemporalConsistencyResult(
                TemporalConsistencyStatus.TEMPORALLY_CONSISTENT,
                emptyList(),
                emptyList(),
                emptyList(),
                hallucinationResult.hallucinationDetected
            )
        }
    }

    private fun insufficient(
        report: AmarClaimVerificationReport,
        hallucinationResult: HallucinationDetectionResult
    ): TemporalConsistencyResult {
        val claimIds = report.claims.indices.map { "answer-claim-$it" }
        return TemporalConsistencyResult(
            TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA,
            emptyList(),
            claimIds,
            emptyList(),
            hallucinationResult.hallucinationDetected
        )
    }
}
