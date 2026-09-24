package com.personal.gridbot.amaros.intelligence.verification

/**
 * Addition 6 — deterministic claim-to-claim contradiction detector.
 *
 * The detector uses only a normalized (subject, predicate, polarity) triple.
 * No semantic similarity, embeddings, LLM, confidence, authority, or blocking.
 */
class SelfContradictionDetector {

    fun detect(claims: List<String>): SelfContradictionResult {
        val normalized = claims.mapIndexedNotNull { index, claim ->
            extract(index, claim)
        }

        if (normalized.size != claims.size) {
            return SelfContradictionResult(
                status = SelfContradictionStatus.INSUFFICIENT_SELF_CONTRADICTION_DATA,
                pairs = emptyList()
            )
        }

        val pairs = normalized
            .indices
            .flatMap { leftIndex ->
                (leftIndex + 1 until normalized.size).mapNotNull { rightIndex ->
                    val left = normalized[leftIndex]
                    val right = normalized[rightIndex]
                    if (left.subject == right.subject &&
                        left.predicate == right.predicate &&
                        left.polarity != right.polarity
                    ) {
                        SelfContradictionPair(left.claimIndex, right.claimIndex)
                    } else {
                        null
                    }
                }
            }
            .sortedWith(compareBy<SelfContradictionPair> { it.leftClaimIndex }.thenBy { it.rightClaimIndex })

        return SelfContradictionResult(
            status = if (pairs.isEmpty()) {
                SelfContradictionStatus.NO_SELF_CONTRADICTION
            } else {
                SelfContradictionStatus.SELF_CONTRADICTION_DETECTED
            },
            pairs = pairs
        )
    }

    private fun extract(index: Int, claim: String): NormalizedClaim? {
        val words = claim
            .lowercase()
            .split(Regex("[^\\p{L}\\p{N}']+"))
            .filter { it.isNotBlank() }

        if (words.size < 3) return null

        val predicateIndex = words.indexOfFirst { it in predicateMarkers }
        if (predicateIndex <= 0 || predicateIndex >= words.lastIndex) return null

        val negated = words.getOrNull(predicateIndex + 1) in negationMarkers ||
            words.getOrNull(predicateIndex) in negativePredicateMarkers

        val subject = words.subList(0, predicateIndex).joinToString(" ")
        val predicate = words.subList(predicateIndex + 1, words.size)
            .filterNot { it in negationMarkers }
            .joinToString(" ")
            .trim()

        if (subject.isBlank() || predicate.isBlank()) return null

        return NormalizedClaim(
            claimIndex = index,
            subject = subject,
            predicate = predicate,
            polarity = if (negated) SelfContradictionPolarity.NEGATIVE else SelfContradictionPolarity.POSITIVE
        )
    }

    private data class NormalizedClaim(
        val claimIndex: Int,
        val subject: String,
        val predicate: String,
        val polarity: SelfContradictionPolarity
    )

    companion object {
        private val predicateMarkers = setOf(
            "is", "are", "was", "were", "be", "has", "have", "had",
            "will", "can", "could", "should", "does", "do", "did"
        )
        private val negationMarkers = setOf(
            "not", "never", "no"
        )
        private val negativePredicateMarkers = setOf(
            "isn't", "aren't", "wasn't", "weren't", "hasn't", "haven't",
            "hadn't", "won't", "can't", "couldn't", "shouldn't",
            "doesn't", "don't", "didn't"
        )
    }
}

enum class SelfContradictionPolarity {
    POSITIVE,
    NEGATIVE
}

enum class SelfContradictionStatus {
    NO_SELF_CONTRADICTION,
    SELF_CONTRADICTION_DETECTED,
    INSUFFICIENT_SELF_CONTRADICTION_DATA
}

data class SelfContradictionPair(
    val leftClaimIndex: Int,
    val rightClaimIndex: Int
)

data class SelfContradictionResult(
    val status: SelfContradictionStatus,
    val pairs: List<SelfContradictionPair>
)
