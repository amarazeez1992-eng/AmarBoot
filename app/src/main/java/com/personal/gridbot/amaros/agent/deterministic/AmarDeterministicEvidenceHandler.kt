package com.personal.gridbot.amaros.agent.deterministic

import com.personal.gridbot.amaros.agent.protection.InvalidFutureEvidenceProtectionResult

class AmarDeterministicEvidenceHandler : AmarDeterministicEvidenceContract {
    override fun handle(
        input: InvalidFutureEvidenceProtectionResult
    ): DeterministicEvidenceResult {
        if (input.protected.isEmpty() &&
            input.invalid.isEmpty() &&
            input.future.isEmpty()
        ) {
            return DeterministicEvidenceResult.failed(
                DeterministicHandlingReason.MISSING_EVIDENCE
            )
        }

        val canonical = input.protected.map { protectedEvidence ->
            val key = protectedEvidence.evidence.candidate.candidate.fingerprint
            if (!key.matches(Regex("^[0-9a-fA-F]{64}$"))) {
                return DeterministicEvidenceResult.failed(
                    DeterministicHandlingReason.INVALID_FINGERPRINT
                )
            }
            CanonicalEvidence(
                evidence = protectedEvidence,
                canonicalKey = key
            )
        }.sortedBy { it.canonicalKey }

        val keys = canonical.map { it.canonicalKey }
        if (keys.size != keys.toSet().size) {
            return DeterministicEvidenceResult.failed(
                DeterministicHandlingReason.DUPLICATE_CANONICAL_KEY
            )
        }

        if (canonical.isEmpty()) {
            return DeterministicEvidenceResult(
                canonicalEvidence = emptyList(),
                invalidEvidence = input.invalid,
                futureEvidence = input.future,
                isDownstreamReady = false,
                handlingReason = DeterministicHandlingReason.INVALID_INPUT
            )
        }

        return DeterministicEvidenceResult(
            canonicalEvidence = canonical,
            invalidEvidence = input.invalid,
            futureEvidence = input.future,
            isDownstreamReady = true,
            handlingReason = DeterministicHandlingReason.CANONICAL_ORDER_APPLIED
        )
    }
}
