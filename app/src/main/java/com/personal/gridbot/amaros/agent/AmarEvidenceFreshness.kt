package com.personal.gridbot.amaros.agent

/**
 * Point 4 boundary: evaluates temporal freshness of already-collected evidence.
 * It does not collect, rank, route, verify claims, or make trading decisions.
 */
class AmarEvidenceFreshnessAnalyzer(
    private val freshnessWindowMs: Long = 30L * 24L * 60L * 60L * 1000L
) {
    init { require(freshnessWindowMs > 0) }

    fun assess(retrievedAtEpochMs: Long, nowEpochMs: Long): AmarEvidenceFreshness {
        require(nowEpochMs >= 0)
        val ageMs = nowEpochMs - retrievedAtEpochMs
        if (ageMs < 0) return AmarEvidenceFreshness(FreshnessStatus.FUTURE, ageMs, 0.0)
        if (ageMs <= freshnessWindowMs) return AmarEvidenceFreshness(FreshnessStatus.FRESH, ageMs, 1.0)
        return AmarEvidenceFreshness(
            FreshnessStatus.STALE,
            ageMs,
            (freshnessWindowMs.toDouble() / ageMs.toDouble()).coerceIn(0.0, 1.0)
        )
    }
}

enum class FreshnessStatus { FRESH, STALE, FUTURE }

data class AmarEvidenceFreshness(
    val status: FreshnessStatus,
    val ageMs: Long,
    val score: Double
)
