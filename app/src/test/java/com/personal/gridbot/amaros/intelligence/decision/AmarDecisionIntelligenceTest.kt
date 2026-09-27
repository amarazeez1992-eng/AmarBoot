package com.personal.gridbot.amaros.intelligence.decision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarDecisionIntelligenceTest {

    private fun engine() = AmarDecisionIntelligence { 42L }

    @Test fun buildContext_normalizes() {
        val c = engine().buildContext("grow", listOf("a", "a", "b"), 0.5, 0.7)
        assertEquals(2, c.constraints.size)
    }

    @Test fun buildContext_rejectsInvalidTolerance() {
        assertTrue(runCatching { engine().buildContext("g", emptyList(), 1.5, 0.5) }.isFailure)
    }

    @Test fun generateOptions_distinct() {
        val opts = engine().generateOptions(listOf("buy", "buy", "sell"), 0.7)
        assertEquals(2, opts.size)
    }

    @Test fun decide_emptyOptionsRejects() {
        val c = engine().buildContext("g", emptyList(), 0.5, 0.9)
        val report = engine().decide(c, emptyList())
        assertEquals(AmarDecisionIntelligence.Decision.REJECT, report.decision)
        assertNull(report.selectedOptionId)
    }

    @Test fun decide_highConfidenceApproves() {
        val e = engine()
        val c = e.buildContext("gold", emptyList(), 0.8, 0.9)
        val opts = e.generateOptions(listOf("gold analysis"), 0.9)
        val report = e.decide(c, opts)
        assertEquals(AmarDecisionIntelligence.Decision.APPROVE, report.decision)
        assertNotNull(report.selectedOptionId)
    }

    @Test fun decide_lowConfidenceDefers() {
        val e = engine()
        val c = e.buildContext("gold", emptyList(), 0.8, 0.2)
        val opts = e.generateOptions(listOf("gold"), 0.3)
        val report = e.decide(c, opts)
        assertEquals(AmarDecisionIntelligence.Decision.DEFER, report.decision)
    }

    @Test fun decide_constraintViolationExcludes() {
        val e = engine()
        val c = e.buildContext("gold", listOf("halal"), 0.8, 0.9)
        val opts = e.generateOptions(listOf("non-compliant option"), 0.9)
        val report = e.decide(c, opts)
        assertTrue(report.decision != AmarDecisionIntelligence.Decision.APPROVE)
    }

    @Test fun decide_auditsTimestamp() {
        val e = engine()
        val c = e.buildContext("g", emptyList(), 0.5, 0.5)
        val report = e.decide(c, e.generateOptions(listOf("x"), 0.5))
        assertEquals(42L, report.decidedAtEpochMs)
    }

    @Test fun decide_explanationPopulated() {
        val e = engine()
        val c = e.buildContext("g", emptyList(), 0.5, 0.9)
        val report = e.decide(c, e.generateOptions(listOf("x"), 0.9))
        assertTrue(report.explanation.contains("decision="))
    }

    @Test fun decide_confidenceBounded() {
        val e = engine()
        val c = e.buildContext("g", emptyList(), 0.5, 0.9)
        val report = e.decide(c, e.generateOptions(listOf("x"), 0.9))
        assertTrue(report.confidence in 0.0..1.0)
    }

    @Test fun decide_evaluationsMatchOptions() {
        val e = engine()
        val c = e.buildContext("g", emptyList(), 0.5, 0.9)
        val opts = e.generateOptions(listOf("a", "b"), 0.9)
        val report = e.decide(c, opts)
        assertEquals(opts.size, report.evaluations.size)
    }
}
