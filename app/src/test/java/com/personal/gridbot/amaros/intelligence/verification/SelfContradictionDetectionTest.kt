package com.personal.gridbot.amaros.intelligence.verification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelfContradictionDetectionTest {

    private val detector = SelfContradictionDetector()

    @Test
    fun opposite_polarity_same_subject_and_predicate_is_detected() {
        val result = detector.detect(
            listOf(
                "Gold is rising today.",
                "Gold is not rising today."
            )
        )

        assertEquals(SelfContradictionStatus.SELF_CONTRADICTION_DETECTED, result.status)
        assertEquals(listOf(SelfContradictionPair(0, 1)), result.pairs)
    }

    @Test
    fun same_polarity_is_not_a_contradiction() {
        val result = detector.detect(
            listOf(
                "Gold is rising today.",
                "Gold is rising today."
            )
        )

        assertEquals(SelfContradictionStatus.NO_SELF_CONTRADICTION, result.status)
        assertTrue(result.pairs.isEmpty())
    }

    @Test
    fun different_subject_or_predicate_is_not_a_contradiction() {
        val result = detector.detect(
            listOf(
                "Gold is rising today.",
                "Silver is not rising today.",
                "Gold is stable today."
            )
        )

        assertEquals(SelfContradictionStatus.NO_SELF_CONTRADICTION, result.status)
        assertTrue(result.pairs.isEmpty())
    }

    @Test
    fun extraction_failure_fails_closed() {
        val result = detector.detect(
            listOf(
                "Gold is rising today.",
                "Insufficiently structured claim."
            )
        )

        assertEquals(
            SelfContradictionStatus.INSUFFICIENT_SELF_CONTRADICTION_DATA,
            result.status
        )
        assertTrue(result.pairs.isEmpty())
    }

    @Test
    fun contradiction_pairs_are_deterministically_ordered() {
        val result = detector.detect(
            listOf(
                "Gold is rising today.",
                "Gold is not rising today.",
                "Silver is falling today.",
                "Silver is not falling today."
            )
        )

        assertEquals(
            listOf(
                SelfContradictionPair(0, 1),
                SelfContradictionPair(2, 3)
            ),
            result.pairs
        )
    }
}
