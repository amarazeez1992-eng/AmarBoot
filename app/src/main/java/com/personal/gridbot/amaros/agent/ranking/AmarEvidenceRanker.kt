package com.personal.gridbot.amaros.agent.ranking

import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.EvidenceStatus
import java.util.Locale

class AmarEvidenceRanker : AmarEvidenceRankingContract {

    override fun rank(input: AmarEvidenceRankingInput): EvidenceRankingResult {
        if (input.classifiedEvidence.isEmpty()) return EvidenceRankingResult.empty()

        val rankedCandidates = mutableListOf<ScoredEvidence>()
        val unranked = mutableListOf<ClassifiedEvidence>()

        input.classifiedEvidence.forEach { evidence ->
            if (evidence.status !in RANKABLE_STATUSES) {
                unranked += evidence
                return@forEach
            }

            val fingerprint = evidence.candidate.candidate.fingerprint
            val relevance = input.relevanceScores[fingerprint]
            val authority = input.authorityScores[fingerprint]
            val freshness = input.freshnessScores[fingerprint]

            if (!isValidSignal(relevance) ||
                !isValidSignal(authority) ||
                !isValidSignal(freshness)
            ) {
                unranked += evidence
                return@forEach
            }

            val statusWeight = statusWeight(evidence.status)
            val finalScore =
                (relevance!! * RELEVANCE_WEIGHT) +
                    (statusWeight * STATUS_WEIGHT) +
                    (authority!! * AUTHORITY_WEIGHT) +
                    (freshness!! * FRESHNESS_WEIGHT)

            rankedCandidates += ScoredEvidence(
                evidence, relevance, statusWeight, authority, freshness, finalScore
            )
        }

        val sorted = rankedCandidates.sortedWith(
            compareByDescending<ScoredEvidence> { statusTier(it.evidence.status) }
                .thenByDescending { it.finalScore }
                .thenByDescending { it.authority }
                .thenByDescending { it.freshness }
                .thenBy { it.evidence.candidate.candidate.fingerprint }
        )

        val ranked = sorted.mapIndexed { index, scored ->
            RankedEvidence(
                evidence = scored.evidence,
                rank = index + 1,
                rankingSignals = mapOf(
                    RankingSignal.RELEVANCE to scored.relevance,
                    RankingSignal.STATUS_WEIGHT to scored.statusWeight,
                    RankingSignal.AUTHORITY to scored.authority,
                    RankingSignal.FRESHNESS to scored.freshness,
                    RankingSignal.FINAL_SCORE to scored.finalScore
                ),
                explanation = explanation(scored)
            )
        }

        return EvidenceRankingResult(
            ranked = ranked,
            unranked = unranked,
            isDownstreamReady = ranked.isNotEmpty() && unranked.isEmpty()
        )
    }

    private fun isValidSignal(value: Double?): Boolean =
        value != null && value.isFinite() && value in 0.0..1.0

    private fun statusWeight(status: EvidenceStatus): Double =
        when (status) {
            EvidenceStatus.COMPLETE -> 1.0
            EvidenceStatus.PARTIAL -> 0.7
            EvidenceStatus.STALE -> 0.3
            else -> error("Non-rankable status must be rejected before scoring")
        }

    private fun statusTier(status: EvidenceStatus): Int =
        when (status) {
            EvidenceStatus.COMPLETE -> 3
            EvidenceStatus.PARTIAL -> 2
            EvidenceStatus.STALE -> 1
            else -> 0
        }

    private fun explanation(scored: ScoredEvidence): String =
        "status=" + scored.evidence.status +
            ";relevance=" + format(scored.relevance) +
            ";statusWeight=" + format(scored.statusWeight) +
            ";authority=" + format(scored.authority) +
            ";freshness=" + format(scored.freshness) +
            ";finalScore=" + format(scored.finalScore) +
            ";tieBreak=authority DESC,freshness DESC,fingerprint ASC"

    private fun format(value: Double): String =
        String.format(Locale.ROOT, "%.6f", value)

    private data class ScoredEvidence(
        val evidence: ClassifiedEvidence,
        val relevance: Double,
        val statusWeight: Double,
        val authority: Double,
        val freshness: Double,
        val finalScore: Double
    )

    companion object {
        private const val RELEVANCE_WEIGHT = 0.50
        private const val STATUS_WEIGHT = 0.25
        private const val AUTHORITY_WEIGHT = 0.15
        private const val FRESHNESS_WEIGHT = 0.10

        private val RANKABLE_STATUSES = setOf(
            EvidenceStatus.COMPLETE,
            EvidenceStatus.PARTIAL,
            EvidenceStatus.STALE
        )
    }
}
