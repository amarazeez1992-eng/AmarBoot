package com.personal.gridbot.amaros.agent

/**
 * Single policy contract for the canonical AmarEvidenceQualityEngine.
 * This is configuration only: no retrieval, research orchestration, or execution authority.
 */
data class AmarEvidencePolicy(
    val version: Int = 2,
    val freshnessHalfLifeMs: Long = 86_400_000L,
    val authorityWeight: Double = 0.40,
    val freshnessWeight: Double = 0.25,
    val independenceWeight: Double = 0.20,
    val uniquenessWeight: Double = 0.15,
    val verifiedThreshold: Double = 0.80,
    val weakThreshold: Double = 0.35
) {
    init {
        require(version > 0)
        require(freshnessHalfLifeMs > 0)
        val weights = listOf(authorityWeight, freshnessWeight, independenceWeight, uniquenessWeight)
        require(weights.all { it.isFinite() && it >= 0.0 })
        require(kotlin.math.abs(weights.sum() - 1.0) < 1e-9)
        require(verifiedThreshold in 0.0..1.0)
        require(weakThreshold in 0.0..1.0)
        require(weakThreshold <= verifiedThreshold)
    }

    companion object {
        val DEFAULT = AmarEvidencePolicy()
    }
}
