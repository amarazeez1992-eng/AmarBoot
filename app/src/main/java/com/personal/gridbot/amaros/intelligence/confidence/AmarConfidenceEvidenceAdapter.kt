package com.personal.gridbot.amaros.intelligence.confidence

import com.personal.gridbot.amaros.agent.AmarCanonicalEvidenceQualityCertificationReport
import com.personal.gridbot.amaros.agent.AmarAgentEvidenceConsensus
import com.personal.gridbot.amaros.agent.ResearchFinding

/**
 * Constitutional adapter between the already-owned evidence/consensus outputs and
 * AmarConfidenceEngine.
 *
 * This class performs no new evidence analysis and never invents upstream
 * verification state. Point 10 certification is consumed exactly as supplied.
 */
object AmarConfidenceEvidenceAdapter {

    fun evaluate(
        findings: List<ResearchFinding>,
        quality: AmarCanonicalEvidenceQualityCertificationReport,
        consensus: com.personal.gridbot.amaros.agent.AmarConsensusReport =
            AmarAgentEvidenceConsensus().summarize(findings)
    ): AmarConfidenceEngine.Result {
        val qualityItems = quality.quality.items
        val completeness = if (findings.isEmpty()) {
            0.0
        } else {
            findings.count { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
                .toDouble() / findings.size
        }

        val freshness = if (qualityItems.isEmpty()) {
            0.0
        } else {
            qualityItems.map { it.freshnessScore }.average().coerceIn(0.0, 1.0)
        }

        val sourceReliability = if (qualityItems.isEmpty()) {
            0.0
        } else {
            qualityItems.map { it.authorityScore }.average().coerceIn(0.0, 1.0)
        }

        return AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(
                quality = quality.certificationScore.coerceIn(0.0, 1.0),
                completeness = completeness.coerceIn(0.0, 1.0),
                freshness = freshness,
                agreement = consensus.consensusScore.coerceIn(0.0, 1.0),
                sourceReliability = sourceReliability
            )
        )
    }
}
