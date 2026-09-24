package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarClaimVerification
import com.personal.gridbot.amaros.agent.AmarClaimVerificationReport
import com.personal.gridbot.amaros.agent.AttributionReason
import com.personal.gridbot.amaros.agent.BlockedClaim
import com.personal.gridbot.amaros.agent.BlockingReason
import com.personal.gridbot.amaros.agent.BlockingResult
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.HallucinationDetectionGuard
import com.personal.gridbot.amaros.agent.HallucinationDetectionResult
import com.personal.gridbot.amaros.agent.HallucinationSeverity
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.SourceAttributionEnforcer
import com.personal.gridbot.amaros.agent.TemporalConsistencyCheck
import com.personal.gridbot.amaros.agent.TemporalConsistencyResult
import com.personal.gridbot.amaros.agent.TemporalConsistencyStatus
import com.personal.gridbot.amaros.agent.TemporalEvidenceRecord
import com.personal.gridbot.amaros.agent.UnsupportedClaimBlocking
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationState
import com.personal.gridbot.amaros.agent.claim.StructuredClaim
import com.personal.gridbot.amaros.agent.claim.VerifiedClaim
import com.personal.gridbot.amaros.agent.correlation.AmarCrossSourceAgreementConsumer
import com.personal.gridbot.amaros.agent.correlation.CorrelatedGroup
import com.personal.gridbot.amaros.agent.correlation.CorrelationReason
import com.personal.gridbot.amaros.agent.correlation.CorrelationType
import com.personal.gridbot.amaros.agent.correlation.CrossSourceAgreementStatus
import com.personal.gridbot.amaros.agent.correlation.CrossSourceCorrelationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Item4HallucinationFirewallIntegrationTest {

    private val gate = FinalHallucinationDecisionGate()

    @Test
    fun caseA_full_pass_propagates_through_all_eight_additions() {
        val base = baseInput()
        assertEquals(SelfContradictionStatus.NO_SELF_CONTRADICTION, base.input.addition6!!.status)
        assertEquals(FinalAnswerClaimCoverageStatus.COVERAGE_COMPLETE, base.input.addition7!!.status)

        val result = gate.evaluate(base.input)

        assertEquals(FinalDecision.PASS, result.decision)
        assertEquals(FinalDecisionReason.NO_BLOCKING_SIGNAL, result.reason)
    }

    @Test
    fun caseB_a1_block_propagates_to_final_block() {
        val result = gate.evaluate(
            baseInput().input.copy(
                addition1 = BlockingResult(
                    true,
                    BlockingReason.UNSUPPORTED_CLAIM,
                    listOf(BlockedClaim("claim-1", 0, 0, 0))
                )
            )
        )

        assertEquals(FinalDecision.BLOCK, result.decision)
        assertEquals(FinalDecisionReason.ADDITION_1_BLOCKED, result.reason)
    }

    @Test
    fun caseC_a4_insufficient_temporal_data_propagates_to_final_block() {
        val base = baseInput()
        val temporal = TemporalConsistencyCheck.evaluate(
            base.report,
            emptyList(),
            base.addition3,
            1_000L
        )

        assertEquals(TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA, temporal.status)
        val result = gate.evaluate(base.input.copy(addition4 = temporal))

        assertEquals(FinalDecision.BLOCK, result.decision)
        assertEquals(FinalDecisionReason.ADDITION_4_INSUFFICIENT_TEMPORAL_DATA, result.reason)
    }

    @Test
    fun caseD_a5_disagreement_propagates_to_final_block() {
        val correlation = CrossSourceCorrelationResult(
            listOf(
                CorrelatedGroup(setOf("fp-a", "fp-b"), CorrelationType.DISAGREEMENT, emptyList())
            ),
            true,
            CorrelationReason.CONFLICT_UPSTREAM
        )
        val agreement = AmarCrossSourceAgreementConsumer().consume(correlation)

        assertEquals(CrossSourceAgreementStatus.READY, agreement.status)
        assertEquals(1, agreement.disagreementGroups.size)

        val result = gate.evaluate(baseInput().input.copy(addition5 = agreement))
        assertEquals(FinalDecision.BLOCK, result.decision)
        assertEquals(FinalDecisionReason.ADDITION_5_DISAGREEMENT, result.reason)
    }

    @Test
    fun caseE_arabic_self_contradiction_is_proven_by_pair_indices() {
        val result = SelfContradictionDetector().detect(
            listOf("الذهب هو صاعد", "الذهب ليس صاعد")
        )

        assertEquals(SelfContradictionStatus.SELF_CONTRADICTION_DETECTED, result.status)
        assertEquals(listOf(SelfContradictionPair(0, 1)), result.pairs)

        val finalResult = gate.evaluate(baseInput().input.copy(addition6 = result))
        assertEquals(FinalDecision.BLOCK, finalResult.decision)
        assertEquals(FinalDecisionReason.ADDITION_6_SELF_CONTRADICTION, finalResult.reason)
    }

    @Test
    fun caseF_a7_coverage_incomplete_propagates_to_final_block() {
        val claims = listOf(
            StructuredClaim("claim-1", "The market is stable today", "market", "stable", "today"),
            StructuredClaim("claim-2", "The trend is rising today", "trend", "rising", "today")
        )
        val verification = ClaimVerificationResult(
            listOf(
                VerifiedClaim(claims[0], ClaimVerificationState.SUPPORTED, listOf("fp-1"), emptyList(), "supported")
            ),
            emptyList(),
            true
        )
        val coverage = FinalAnswerClaimCoverageGate().evaluate(claims, verification)

        assertEquals(FinalAnswerClaimCoverageStatus.COVERAGE_INCOMPLETE, coverage.status)
        assertEquals(listOf("claim-2"), coverage.uncoveredClaimIds)

        val result = gate.evaluate(baseInput().input.copy(addition7 = coverage))
        assertEquals(FinalDecision.BLOCK, result.decision)
        assertEquals(FinalDecisionReason.ADDITION_7_COVERAGE_INCOMPLETE, result.reason)
    }

    @Test
    fun caseG_insufficient_a6_state_is_fail_closed() {
        val result = gate.evaluate(
            baseInput().input.copy(
                addition6 = SelfContradictionResult(
                    SelfContradictionStatus.INSUFFICIENT_SELF_CONTRADICTION_DATA,
                    emptyList()
                )
            )
        )

        assertEquals(FinalDecision.BLOCK, result.decision)
        assertEquals(
            FinalDecisionReason.ADDITION_6_INSUFFICIENT_SELF_CONTRADICTION_DATA,
            result.reason
        )
    }

    @Test
    fun caseH_missing_a1_result_blocks_with_missing_addition_reason() {
        val result = gate.evaluate(baseInput().input.copy(addition1 = null))

        assertEquals(FinalDecision.BLOCK, result.decision)
        assertEquals(FinalDecisionReason.INSUFFICIENT_ADDITION_N_DATA, result.reason)
        assertEquals(listOf(1), result.missingAdditionNumbers)
    }

    @Test
    fun caseI_all_seven_blocking_signals_use_only_priority_one_reason() {
        val base = baseInput().input
        val result = gate.evaluate(
            base.copy(
                addition1 = BlockingResult(
                    true,
                    BlockingReason.UNSUPPORTED_CLAIM,
                    listOf(BlockedClaim("claim-1", 0, 0, 0))
                ),
                addition2 = base.addition2!!.copy(
                    attributed = false,
                    reason = AttributionReason.MISSING_SOURCE_URI
                ),
                addition3 = HallucinationDetectionResult(true, emptyList(), HallucinationSeverity.HIGH),
                addition4 = TemporalConsistencyResult(
                    TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA,
                    emptyList(), listOf("answer-claim-0"), emptyList(), true
                ),
                addition5 = base.addition5!!.copy(
                    disagreementGroups = listOf(
                        CorrelatedGroup(setOf("fp-a", "fp-b"), CorrelationType.DISAGREEMENT, emptyList())
                    )
                ),
                addition6 = SelfContradictionResult(
                    SelfContradictionStatus.SELF_CONTRADICTION_DETECTED,
                    listOf(SelfContradictionPair(0, 1))
                ),
                addition7 = FinalAnswerClaimCoverageResult(
                    FinalAnswerClaimCoverageStatus.COVERAGE_INCOMPLETE,
                    listOf("claim-1")
                )
            )
        )

        assertEquals(FinalDecision.BLOCK, result.decision)
        assertEquals(FinalDecisionReason.ADDITION_1_BLOCKED, result.reason)
        assertTrue(result.missingAdditionNumbers.isEmpty())
    }

    private data class BaseInput(
        val input: FinalHallucinationDecisionInput,
        val report: AmarClaimVerificationReport,
        val addition3: HallucinationDetectionResult
    )

    private fun baseInput(): BaseInput {
        val report = AmarClaimVerificationReport(
            claims = listOf(
                AmarClaimVerification(
                    "The market structure remains stable today.",
                    3,
                    0,
                    true,
                    3,
                    0
                )
            ),
            accepted = true
        )
        val findings = listOf(
            ResearchFinding(
                "Source A",
                "https://example.com/a",
                "The market structure remains stable today.",
                Authority.REPUTABLE,
                EvidenceStance.SUPPORTS,
                fingerprint = "fp-a"
            ),
            ResearchFinding(
                "Source B",
                "https://example.com/b",
                "The market structure remains stable today.",
                Authority.REPUTABLE,
                EvidenceStance.SUPPORTS,
                fingerprint = "fp-b"
            ),
            ResearchFinding(
                "Source C",
                "https://example.com/c",
                "The market structure remains stable today.",
                Authority.REPUTABLE,
                EvidenceStance.SUPPORTS,
                fingerprint = "fp-c"
            )
        )

        val addition1 = UnsupportedClaimBlocking.evaluate(report)
        val addition2 = SourceAttributionEnforcer.evaluate(report, findings)
        val addition3 = HallucinationDetectionGuard.evaluate(
            report,
            addition1,
            addition2,
            "The market structure remains stable today."
        )
        val addition4 = TemporalConsistencyCheck.evaluate(
            report,
            listOf(
                TemporalEvidenceRecord(
                    "answer-claim-0",
                    900L,
                    "fp-a",
                    800L,
                    850L
                )
            ),
            addition3,
            1_000L
        )
        val addition5 = AmarCrossSourceAgreementConsumer().consume(
            CrossSourceCorrelationResult(
                listOf(
                    CorrelatedGroup(setOf("fp-a", "fp-b"), CorrelationType.AGREEMENT, emptyList())
                ),
                true,
                CorrelationReason.VALID_CORRELATION
            )
        )
        val addition6 = SelfContradictionDetector().detect(
            listOf("Gold is rising", "Gold is stable")
        )
        val structuredClaims = listOf(
            StructuredClaim(
                "claim-1",
                "The market structure remains stable today.",
                "market",
                "stable",
                "today"
            )
        )
        val addition7 = FinalAnswerClaimCoverageGate().evaluate(
            structuredClaims,
            ClaimVerificationResult(
                listOf(
                    VerifiedClaim(
                        structuredClaims[0],
                        ClaimVerificationState.SUPPORTED,
                        listOf("fp-a"),
                        emptyList(),
                        "supported"
                    )
                ),
                emptyList(),
                true
            )
        )

        return BaseInput(
            FinalHallucinationDecisionInput(
                addition1,
                addition2,
                addition3,
                addition4,
                addition5,
                addition6,
                addition7
            ),
            report,
            addition3
        )
    }
}
