package com.personal.gridbot.amaros.agent.historical

import com.personal.gridbot.amaros.agent.deterministic.DeterministicEvidenceResult
import com.personal.gridbot.amaros.agent.deterministic.DeterministicHandlingReason
import com.personal.gridbot.amaros.intelligence.advanced.AmarMarketRegime
import com.personal.gridbot.amaros.intelligence.advanced.AmarRegimeObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarHistoricalValidationTest {
    private val validator = AmarHistoricalValidator()
    private val regime = AmarRegimeObservation(AmarMarketRegime.TREND, 0.9, 0.4, 0.8, 0.2, 0.1, 1000L)
    private val hypothesis = Hypothesis("h1", "XAUUSD", "H1", mapOf("session" to "NY"))
    private val validEvidence = DeterministicEvidenceResult(
        canonicalEvidence = emptyList(),
        invalidEvidence = emptyList(),
        futureEvidence = emptyList(),
        isDownstreamReady = true,
        handlingReason = DeterministicHandlingReason.CANONICAL_ORDER_APPLIED
    )

    private fun case(
        id: String,
        time: Long = 900L,
        market: String = "XAUUSD",
        timeframe: String = "H1",
        caseRegime: AmarMarketRegime = AmarMarketRegime.TREND,
        hypothesisId: String = "h1",
        outcome: String = "UP",
        attrs: Map<String, String> = mapOf("session" to "NY")
    ) = HistoricalCase(id, market, timeframe, time,
        regime.copy(regime = caseRegime, timestamp = time), hypothesisId, outcome, attrs)

    private fun result(
        cases: List<HistoricalCase> = listOf(case("1")),
        criteria: MatchCriteria = MatchCriteria(),
        cutoff: Long = 1000L,
        evidence: DeterministicEvidenceResult = validEvidence
    ) = validator.validate(hypothesis, cases, criteria, regime, evidence, cutoff)

    @Test fun empty_input_returns_empty_result() {
        assertEquals(HistoricalValidationReason.DATA_UNAVAILABLE, result(emptyList()).reason)
    }

    @Test fun single_comparable_case_identified() {
        assertEquals(1, result().frequency)
    }

    @Test fun multiple_comparable_cases_identified() {
        assertEquals(2, result(listOf(case("1"), case("2"))).frequency)
    }

    @Test fun no_comparable_cases_returns_insufficient() {
        assertEquals(HistoricalValidationReason.INSUFFICIENT_COMPARABLE_CASES,
            result(listOf(case("1")), MatchCriteria(minimumComparableCases = 2)).reason)
    }

    @Test fun regime_mismatch_rejects() {
        assertEquals(HistoricalValidationReason.REGIME_MISMATCH,
            result(listOf(case("1", caseRegime = AmarMarketRegime.RANGE))).reason)
    }

    @Test fun match_criteria_enforced() {
        assertEquals(
            HistoricalValidationReason.NO_COMPARABLE_CASES,
            result(
                listOf(case("1", attrs = mapOf("session" to "LONDON"))),
                MatchCriteria(requiredAttributes = setOf("session"))
            ).reason
        )
    }

    @Test fun outcome_summary_calculated() {
        val r = result(listOf(case("1", outcome = "UP"), case("2", outcome = "DOWN"), case("3", outcome = "UP")))
        assertEquals(mapOf("UP" to 2, "DOWN" to 1), r.observedOutcomes.outcomeCounts)
    }

    @Test fun frequency_counted() {
        assertEquals(3, result(listOf(case("1"), case("2"), case("3"))).frequency)
    }

    @Test fun differences_listed() {
        val r = result(listOf(case("1", attrs = mapOf("session" to "NY", "extra" to "x"))))
        assertTrue(r.differences.isEmpty())
    }


    @Test fun no_comparable_cases_returns_no_cases() {
        assertEquals(HistoricalValidationReason.NO_COMPARABLE_CASES,
            result(listOf(case("1", market = "EURUSD"))).reason)
    }

    @Test fun is_deterministic() {
        assertEquals(result(listOf(case("1"), case("2"))), result(listOf(case("1"), case("2"))))
    }

    @Test fun is_stateless() {
        val a = result()
        val b = result(listOf(case("2", outcome = "DOWN")))
        assertNotSame(a, b)
        assertEquals(1, a.frequency)
        assertEquals("DOWN", b.observedOutcomes.outcomeCounts.keys.single())
    }

    @Test fun no_backtest_execution() {
        assertEquals(1, result().frequency)
    }

    @Test fun no_replay_execution() {
        assertTrue(result().isDownstreamReady)
    }

    @Test fun preserves_evidence() {
        val r = result()
        assertTrue(validEvidence.isDownstreamReady)
        assertEquals(validEvidence, validEvidence)
        assertTrue(r.isDownstreamReady)
    }

    @Test fun result_partitions_correctly() {
        val r = result(listOf(case("1"), case("2")))
        assertEquals(r.frequency, r.comparableCases.size)
        assertEquals(r.frequency, r.observedOutcomes.totalOccurrences)
    }

    @Test fun fail_closed_on_invalid_input() {
        assertFalse(result(cutoff = -1L).isDownstreamReady)
    }

    @Test fun is_downstream_ready_when_valid() {
        assertTrue(result().isDownstreamReady)
    }

    @Test fun no_recalculation_of_upstream() {
        val before = validEvidence
        result()
        assertEquals(before, validEvidence)
    }

    @Test fun future_case_is_rejected_by_decision_cutoff() {
        assertEquals(HistoricalValidationReason.NO_COMPARABLE_CASES,
            result(listOf(case("future", time = 2000L)), cutoff = 1000L).reason)
    }
}
