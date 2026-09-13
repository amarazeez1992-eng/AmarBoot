package com.personal.gridbot.amaros.ai.research

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiTradingResearchPhaseThreeTest {
    @Test
    fun ohlcvNormalizer_sortsAndRemovesDuplicateTimestamps() {
        val rows = listOf(
            AmarOhlcv(2, 2.0, 3.0, 1.0, 2.5, 10.0),
            AmarOhlcv(1, 1.0, 2.0, 0.5, 1.5, 8.0),
            AmarOhlcv(2, 2.0, 4.0, 1.5, 3.5, 12.0)
        )
        val normalized = AmarOhlcvNormalizer.normalize(rows)
        assertEquals(listOf(1L, 2L), normalized.map { it.timestamp })
    }

    @Test
    fun robustnessScore_composesFourResearchGates() {
        val score = AmarRobustnessScore(0.8, 0.6, 0.7, 0.9)
        assertEquals(0.75, score.composite, 0.0001)
    }

    @Test
    fun selector_prefersHighestCompositeCandidate() {
        val weak = AmarStrategyCandidate("weak", AmarRobustnessScore(0.4, 0.4, 0.4, 0.4))
        val strong = AmarStrategyCandidate("strong", AmarRobustnessScore(0.9, 0.8, 0.7, 0.9))
        assertEquals("strong", AmarChampionChallengerSelector().select(listOf(weak, strong))?.id)
        assertTrue(AmarChampionChallengerSelector().separation(strong, weak) > 0.0)
    }
}
