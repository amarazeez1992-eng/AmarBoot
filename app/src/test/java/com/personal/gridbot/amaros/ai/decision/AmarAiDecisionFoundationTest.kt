package com.personal.gridbot.amaros.ai.decision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiDecisionFoundationTest {
    private val engine = AmarDecisionEngine()

    @Test
    fun weighted_buy_evidence_produces_buy() {
        val result = engine.synthesize(
            listOf(
                AmarDecisionEvidence("swing", "BUY", 2.0),
                AmarDecisionEvidence("liquidity", "SELL", 1.0)
            )
        )
        assertEquals(AmarDecisionDirection.BUY, result.direction)
        assertTrue(result.score > 0.0)
    }

    @Test
    fun unsupported_evidence_is_excluded() {
        val result = engine.synthesize(
            listOf(AmarDecisionEvidence("unknown", "BUY", 10.0, supported = false))
        )
        assertEquals(AmarDecisionDirection.NEUTRAL, result.direction)
        assertEquals(0.0, result.score, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalid_weight_is_rejected() {
        engine.synthesize(listOf(AmarDecisionEvidence("x", "BUY", -1.0)))
    }

    @Test
    fun cache_round_trip_is_deterministic() {
        val cache = AmarAiAnalysisCache<String, Int>()
        cache.put("gold", 42)
        assertEquals(42, cache.get("gold"))
        cache.clear()
        assertEquals(null, cache.get("gold"))
    }
}
