package com.personal.gridbot.amaros.agent.explanation

import com.personal.gridbot.amaros.agent.ranking.RankedEvidence

class AmarEvidenceExplainer : AmarEvidenceExplanationContract {
    override fun explain(evidence: List<RankedEvidence>): EvidenceExplanationResult {
        if (evidence.isEmpty()) return EvidenceExplanationResult.empty()

        val explained = mutableListOf<ExplainedEvidence>()
        val rejected = mutableListOf<ExplainedEvidence>()

        evidence.forEach { ranked ->
            val candidate = ranked.evidence.candidate.candidate
            val status = ranked.evidence.status

            val rejection = when {
                candidate.provider.isBlank() || candidate.title.isBlank() ||
                    candidate.canonicalUrl.isBlank() -> RejectionReason.INVALID_EVIDENCE
                candidate.normalizedExcerpt.isBlank() -> RejectionReason.MISSING_EVIDENCE
                candidate.provider.isBlank() || candidate.canonicalUrl.isBlank() ->
                    RejectionReason.MISSING_SOURCE
                status.name.isBlank() -> RejectionReason.MISSING_STATUS
                else -> null
            }

            if (rejection != null) {
                rejected += ExplainedEvidence(ranked, null, rejection)
            } else {
                val explanation = EvidenceExplanation(
                    summary = "The evidence reports: ${candidate.normalizedExcerpt}",
                    details = listOf(
                        "Title: ${candidate.title}",
                        "Fingerprint: ${candidate.fingerprint}"
                    ),
                    sourceInfo = "Provider: ${candidate.provider}; URL: ${candidate.canonicalUrl}",
                    statusInfo = "Status: ${status.name}",
                    language = "EN"
                )
                explained += ExplainedEvidence(ranked, explanation, null)
            }
        }

        return EvidenceExplanationResult(
            explained = explained,
            rejected = rejected,
            isDownstreamReady = explained.isNotEmpty() && rejected.isEmpty()
        )
    }
}
