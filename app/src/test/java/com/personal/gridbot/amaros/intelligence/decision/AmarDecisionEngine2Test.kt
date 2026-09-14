package com.personal.gridbot.amaros.intelligence.decision

import com.personal.gridbot.amaros.agent.AmarEvidence
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.intelligence.Evidence
import com.personal.gridbot.amaros.intelligence.EvidenceType
import com.personal.gridbot.amaros.intelligence.MarketContext
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationLayer
import org.junit.Assert.*
import org.junit.Test

class AmarDecisionEngine2Test {
    private val engine = AmarDecisionEngine2()
    private val strongContext = MarketContext(
        directionalScore = 0.9,
        confidence = 0.9,
        regime = "TREND",
        evidence = listOf(Evidence(EvidenceType.TREND, 0.9, 0.9, "strong trend")),
        explanation = "strong aligned evidence"
    )

    @Test fun deterministicStrongDecisionIsProposedOnly() {
        val verification = verifiedReport()
        val input = AmarDecisionInput("case-1", strongContext, volatility = 0.1, spreadRatio = 0.01, verification = verification, nowEpochMs = 1000L)
        val first = engine.decide(input)
        val second = engine.decide(input)
        assertEquals(first, second)
        assertEquals(AmarDecisionDirection.LONG_BIAS, first.direction)
        assertEquals(AmarDecisionStatus.PROPOSED, first.status)
        assertTrue(first.verifiedEvidence)
        assertFalse(first.executable)
    }

    @Test fun missingVerificationBlocksDecisionByDefault() {
        val result = engine.decide(AmarDecisionInput("case-2", strongContext, volatility = 0.1, spreadRatio = 0.01))
        assertEquals(AmarDecisionStatus.BLOCKED, result.status)
        assertEquals(AmarDecisionDirection.NEUTRAL, result.direction)
        assertFalse(result.executable)
    }

    @Test fun emergencyLockAlwaysBlocks() {
        val result = engine.decide(AmarDecisionInput("case-3", strongContext, verification = verifiedReport(), emergencyLock = true))
        assertEquals(AmarDecisionStatus.BLOCKED, result.status)
        assertEquals(AmarDecisionDirection.NEUTRAL, result.direction)
        assertTrue(result.rationale.contains("emergencyLock=true"))
    }

    @Test fun excessiveRiskBlocksEvenWithVerifiedEvidence() {
        val result = engine.decide(AmarDecisionInput("case-4", strongContext, volatility = 1.0, spreadRatio = 1.0, verification = verifiedReport()))
        assertEquals(AmarDecisionStatus.BLOCKED, result.status)
        assertEquals(AmarDecisionDirection.NEUTRAL, result.direction)
        assertTrue(result.riskScore >= 0.75)
    }

    @Test fun quantitativeAssessmentRaisesRiskAndRemainsProposalOnly() {
        val quantitative = AmarQuantitativeAssessment(
            realizedVolatility = 0.8,
            maxDrawdown = 0.7,
            historicalVar = 0.6,
            riskOfRuin = 0.5
        )
        val baseline = engine.decide(AmarDecisionInput("case-5", strongContext, verification = verifiedReport()))
        val quantified = engine.decide(AmarDecisionInput("case-5", strongContext, verification = verifiedReport(), quantitative = quantitative))
        assertTrue(quantified.riskScore > baseline.riskScore)
        assertTrue(quantified.rationale.contains("quantRisk="))
        assertFalse(quantified.executable)
    }

    @Test fun neutralAndLowConfidenceStatesAreExplicit() {
        val neutral = engine.decide(AmarDecisionInput("case-6", strongContext.copy(directionalScore = 0.0), verification = verifiedReport()))
        assertEquals(AmarDecisionStatus.HOLD, neutral.status)
        assertEquals(AmarDecisionDirection.NEUTRAL, neutral.direction)

        val low = engine.decide(AmarDecisionInput("case-7", strongContext.copy(confidence = 0.05), verification = verifiedReport()))
        assertEquals(AmarDecisionStatus.LOW_CONFIDENCE, low.status)
    }

    @Test fun conflictsReduceConfidenceAndCannotBecomeVerifiedDecision() {
        val findings = listOf(
            finding("support", "price trend remains strong", EvidenceStance.SUPPORTS),
            finding("oppose", "price trend remains weak", EvidenceStance.OPPOSES)
        )
        val verification = AmarVerificationLayer().verify("price trend", findings, 1000L)
        assertTrue(verification.conflicts.isNotEmpty())
        val result = engine.decide(AmarDecisionInput("case-8", strongContext, verification = verification))
        assertTrue(result.confidence < 0.70)
        assertEquals(AmarDecisionStatus.BLOCKED, result.status)
    }

    @Test fun policyWeightsAreNormalizedInsteadOfSilentlyOverweightingConfidence() {
        val normalized = AmarDecisionEngine2(AmarDecisionPolicy(contextWeight = 1.0, evidenceWeight = 1.0))
        val default = AmarDecisionEngine2(AmarDecisionPolicy(contextWeight = 0.5, evidenceWeight = 0.5))
        val input = AmarDecisionInput("case-9", strongContext, verification = verifiedReport())
        assertEquals(default.decide(input), normalized.decide(input))
    }

    @Test fun scoreAndConfidenceAlwaysBounded() {
        repeat(20) { index ->
            val result = engine.decide(
                AmarDecisionInput(
                    decisionKey = "bounded-$index",
                    context = strongContext.copy(directionalScore = if (index % 2 == 0) 1.0 else -1.0, confidence = 1.0),
                    volatility = 0.2,
                    spreadRatio = 0.1,
                    verification = verifiedReport()
                )
            )
            assertTrue(result.score in -1.0..1.0)
            assertTrue(result.confidence in 0.0..1.0)
            assertTrue(result.riskScore in 0.0..1.0)
        }
    }

    private fun verifiedReport() = AmarVerificationLayer().verifyEvidenceOnly(
        listOf(finding("verified", "market trend evidence supports direction", EvidenceStance.SUPPORTS)), 1000L
    )

    private fun finding(id: String, evidence: String, stance: EvidenceStance) = ResearchFinding(
        sourceTitle = id,
        sourceUri = "https://example.com/$id",
        evidence = evidence,
        authority = Authority.REPUTABLE,
        stance = stance,
        retrievedAtEpochMs = 1000L,
        fingerprint = AmarEvidence.fingerprintOf(id)
    )
}
