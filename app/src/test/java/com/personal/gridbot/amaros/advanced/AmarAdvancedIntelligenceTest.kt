package com.personal.gridbot.amaros.advanced

import com.personal.gridbot.amaros.intelligence.advanced.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class AmarAdvancedIntelligenceTest {
    @org.junit.Test
    fun advancedLayerBoundariesWork() {
        val candles = (0 until 60).map { i -> val base = 100.0 + i * .3; AmarOhlc(i.toLong(), base, base + .5, base - .2, base + .3) }
        assertTrue(AmarMarketRegimeEngine().classify(candles).regime != AmarMarketRegime.UNKNOWN)
        val profile = AmarStrategyProfile("AMAR1", 1, "test", AmarStrategyDirection.LONG, listOf("close above prior high"), listOf("close below invalidation"), "below swing low", "2R", .01, setOf(AmarMarketRegime.TREND))
        assertTrue(AmarStrategyCompiler().compile(profile).valid)
        val engine = AmarWalkForwardEngine(); val splits = engine.split(candles, 30, 15, 10, 10); assertFalse(splits.isEmpty())
        val report = engine.aggregate(splits.map { AmarValidationWindowResult(it, .5, .4, .3, false, 10) })
        assertTrue(report.verified); assertEquals(.3, report.aggregateOosScore, 1e-9)
    }

    @org.junit.Test
    fun memoryStressEvidenceAndExposureAreBounded() {
        val memory = AmarDecisionMemoryStore(); memory.remember(AmarDecisionMemory("d1", "approve AMAR1", "trend", "positive", true)); assertEquals(1, memory.search("AMAR1").size)
        val stress = AmarStressTestingEngine().evaluate(.8, listOf(AmarStressScenario(AmarStressType.SPREAD, 1.0, "wide spread"))).single(); assertTrue(stress.stressedScore < stress.baseScore)
        val evidence = AmarEvidenceEngine().score(AmarEvidence("e1", "primary", "gold data", AmarEvidenceTier.PRIMARY, .9)); assertTrue(evidence.score > .8)
        val exposure = AmarPortfolioExposureEngine().assess(listOf(AmarExposure("AMAR1", "XAUUSD", AmarStrategyDirection.LONG, .4, AmarMarketRegime.TREND))); assertEquals(.4, exposure.grossRisk, 1e-9)
    }
}
