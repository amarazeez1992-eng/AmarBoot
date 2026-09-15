package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.agent.AmarAgentCritic
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.data.AmarDataSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Stage 11 Item 7: deterministic contract benchmark; test-only, no execution authority. */
class AmarIntelligenceQualityBenchmarkTest {
    private val decisionEngine = DecisionEngine()
    private val critic = AmarAgentCritic()
    private val data = AmarDataSnapshot(generatedAtEpochMs = 1_000L)
    private val context = MarketAnalyzer().analyze(data)

    private data class Observation(val id: String, val passed: Boolean, val detail: String)

    @Test
    fun benchmark_meets_correctness_safety_monotonicity_and_determinism_gates() {
        val first = executeBenchmark()
        val second = executeBenchmark()
        assertEquals("Benchmark is nondeterministic", first, second)
        assertEquals(12, first.size)
        assertEquals("Benchmark case IDs must be unique", first.size, first.map { it.id }.toSet().size)
        assertTrue("Benchmark accuracy below 100%", first.all { it.passed })
        assertTrue(first.all { it.detail.isNotBlank() })
    }

    @Test
    fun benchmark_does_not_introduce_execution_authority() {
        val low = decisionEngine.evaluate(data, context)
        val high = decisionEngine.evaluate(data, context, highRiskInput())
        assertFalse(low.executable)
        assertFalse(high.executable)
    }

    private fun highRiskInput() = DecisionEngine.QuantitativeInput(
        closes = listOf(100.0, 130.0, 70.0, 140.0),
        equityCurve = listOf(1000.0, 1200.0, 300.0, 350.0),
        losses = listOf(50.0, 70.0, 90.0, 100.0, 80.0),
        varScale = 100.0
    )

    private fun executeBenchmark(): List<Observation> {
        val observations = mutableListOf<Observation>()
        fun check(id: String, condition: Boolean, detail: String) {
            observations += Observation(id, condition, detail)
            assertTrue("Benchmark case failed: $id — $detail", condition)
        }

        check("risk.no_evidence", decisionEngine.assessQuantitativeRisk(DecisionEngine.QuantitativeInput()) == null, "absent evidence produces no fabricated risk")

        val minimumSampleRisk = decisionEngine.assessQuantitativeRisk(DecisionEngine.QuantitativeInput(
            closes = listOf(100.0, 101.0, 99.0),
            equityCurve = listOf(1000.0, 900.0),
            losses = listOf(10.0, 20.0, 30.0, 40.0),
            varScale = 100.0
        ))
        check("risk.minimum_samples", minimumSampleRisk != null && minimumSampleRisk.realizedVolatility == null && minimumSampleRisk.maxDrawdown != null && minimumSampleRisk.normalizedHistoricalVar == null, "each metric enforces its own minimum sample requirement")

        val partialInvalid = decisionEngine.assessQuantitativeRisk(DecisionEngine.QuantitativeInput(
            closes = listOf(100.0, Double.NaN, 101.0, 102.0),
            equityCurve = listOf(1000.0, 1200.0, 900.0, 950.0),
            losses = listOf(10.0, 20.0, 30.0, 40.0, 50.0),
            varConfidence = 0.95,
            varScale = 100.0
        ))
        check("risk.partial_invalid", partialInvalid != null && partialInvalid.realizedVolatility == null && partialInvalid.maxDrawdown != null && partialInvalid.normalizedHistoricalVar != null, "invalid volatility does not erase valid independent metrics")

        val lowRiskInput = DecisionEngine.QuantitativeInput(
            closes = listOf(100.0, 100.1, 100.0, 100.1),
            equityCurve = listOf(1000.0, 1005.0, 1003.0, 1007.0),
            losses = listOf(1.0, 1.0, 2.0, 1.0, 2.0),
            varScale = 100.0
        )
        val lowRisk = decisionEngine.assessQuantitativeRisk(lowRiskInput)!!
        val highRisk = decisionEngine.assessQuantitativeRisk(highRiskInput())!!
        val lowDecision = decisionEngine.evaluate(data, context, lowRiskInput)
        val highDecision = decisionEngine.evaluate(data, context, highRiskInput())
        check("risk.monotonic_penalty", highRisk.score > lowRisk.score && highDecision.confidence <= lowDecision.confidence && kotlin.math.abs(highDecision.score) <= kotlin.math.abs(lowDecision.score), "higher measured risk cannot improve decision strength")
        check("risk.bounded", highRisk.score in 0.0..1.0 && highDecision.score in -1.0..1.0 && highDecision.confidence in 0.0..1.0, "outputs remain bounded")

        val clean = critic.review("النتيجة مدعومة بمصدرين مستقلين.", listOf(
            ResearchFinding("A", "https://example.com/a", "support-a", stance = EvidenceStance.SUPPORTS),
            ResearchFinding("B", "https://example.org/b", "support-b", stance = EvidenceStance.SUPPORTS)
        ), requireEvidence = true)
        check("critic.clean_accept", clean.accepted && clean.issues.isEmpty(), "supported answer passes")

        val conflict = critic.review("النتيجة مؤكدة.", listOf(
            ResearchFinding("A", "https://example.com/a", "support", stance = EvidenceStance.SUPPORTS),
            ResearchFinding("B", "https://example.org/b", "oppose", stance = EvidenceStance.OPPOSES)
        ), requireEvidence = true)
        check("critic.conflict_reject", !conflict.accepted && "evidence_conflict" in conflict.issues, "conflicting stances are rejected")

        val duplicate = critic.review("تحليل قائم على المصدر.", listOf(
            ResearchFinding("A1", "https://example.com/a", "one"),
            ResearchFinding("A2", "https://example.com/a", "two")
        ), requireEvidence = true)
        check("critic.source_independence", !duplicate.accepted && duplicate.independentSourceCount == 1, "duplicate URI is not independent")

        val invalid = critic.review("النتيجة 95%.", listOf(
            ResearchFinding("invalid", "", "missing source"),
            ResearchFinding("invalid-2", "https://example.com/b", "")
        ), requireEvidence = true)
        check("critic.invalid_evidence", !invalid.accepted && "invalid_evidence" in invalid.issues, "malformed evidence is not support")

        val guarantee = critic.review("هذا مضمون.", listOf(
            ResearchFinding("A", "https://example.com/a", "support-a", stance = EvidenceStance.SUPPORTS),
            ResearchFinding("B", "https://example.org/b", "support-b", stance = EvidenceStance.SUPPORTS)
        ), requireEvidence = true)
        check("critic.guarantee_guard", !guarantee.accepted && "guarantee_language" in guarantee.issues, "guarantee language remains blocked")

        val empty = critic.review("   ", emptyList(), requireEvidence = false)
        check("critic.empty_reject", !empty.accepted && empty.score == 0.0, "empty answer is rejected")

        val proposal = decisionEngine.evaluate(data, context, lowRiskInput)
        check("architecture.proposal_only", !proposal.executable, "decision layer remains proposal-only")
        return observations
    }
}
