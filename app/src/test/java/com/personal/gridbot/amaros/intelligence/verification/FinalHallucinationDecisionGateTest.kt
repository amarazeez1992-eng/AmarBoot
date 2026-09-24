package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AttributionReason
import com.personal.gridbot.amaros.agent.AttributionResult
import com.personal.gridbot.amaros.agent.BlockedClaim
import com.personal.gridbot.amaros.agent.BlockingReason
import com.personal.gridbot.amaros.agent.BlockingResult
import com.personal.gridbot.amaros.agent.HallucinationDetectionResult
import com.personal.gridbot.amaros.agent.HallucinationSeverity
import com.personal.gridbot.amaros.agent.TemporalConsistencyResult
import com.personal.gridbot.amaros.agent.TemporalConsistencyStatus
import com.personal.gridbot.amaros.agent.correlation.CorrelationReason
import com.personal.gridbot.amaros.agent.correlation.CrossSourceAgreementResult
import com.personal.gridbot.amaros.agent.correlation.CrossSourceAgreementStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class FinalHallucinationDecisionGateTest {

    private val gate = FinalHallucinationDecisionGate()

    @Test
    fun passes_only_when_all_additions_pass() {
        val result = gate.evaluate(
            input(
                addition1 = BlockingResult.passed(),
                addition2 = AttributionResult(true, AttributionReason.ALL_ATTRIBUTED, emptyList()),
                addition3 = HallucinationDetectionResult(false, emptyList(), HallucinationSeverity.NONE),
                addition4 = TemporalConsistencyResult(
                    TemporalConsistencyStatus.TEMPORALLY_CONSISTENT, emptyList(), emptyList(), emptyList(), false
                ),
                addition5 = CrossSourceAgreementResult(
                    CrossSourceAgreementStatus.READY, emptyList(), emptyList(), emptyList(), CorrelationReason.VALID_CORRELATION
                ),
                addition6 = SelfContradictionResult(SelfContradictionStatus.NO_SELF_CONTRADICTION, emptyList()),
                addition7 = FinalAnswerClaimCoverageResult(FinalAnswerClaimCoverageStatus.COVERAGE_COMPLETE, emptyList())
            )
        )

        assertEquals(FinalDecision.PASS, result.decision)
        assertEquals(FinalDecisionReason.NO_BLOCKING_SIGNAL, result.reason)
    }

    @Test
    fun insufficient_states_block() {
        val cases = listOf(
            FinalDecisionReason.ADDITION_4_INSUFFICIENT_TEMPORAL_DATA to FinalDecisionInputFactory.addition4Insufficient(),
            FinalDecisionReason.ADDITION_5_INSUFFICIENT_AGREEMENT_DATA to FinalDecisionInputFactory.addition5Insufficient(),
            FinalDecisionReason.ADDITION_6_INSUFFICIENT_SELF_CONTRADICTION_DATA to FinalDecisionInputFactory.addition6Insufficient(),
            FinalDecisionReason.ADDITION_7_INSUFFICIENT_COVERAGE_DATA to FinalDecisionInputFactory.addition7Insufficient()
        )

        cases.forEach { (reason, decisionInput) ->
            val result = gate.evaluate(decisionInput)
            assertEquals(FinalDecision.BLOCK, result.decision)
            assertEquals(reason, result.reason)
        }
    }

    @Test
    fun priority_is_addition_one_through_seven_and_first_block_is_recorded() {
        val result = gate.evaluate(
            input(
                addition1 = BlockingResult(true, BlockingReason.UNSUPPORTED_CLAIM, listOf(
                    BlockedClaim("claim-1", 0, 0, 0)
                )),
                addition2 = AttributionResult(false, AttributionReason.MISSING_SOURCE_URI, listOf("fp-1")),
                addition3 = HallucinationDetectionResult(true, emptyList(), HallucinationSeverity.HIGH),
                addition4 = TemporalConsistencyResult(
                    TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA, emptyList(), listOf("answer-claim-0"), emptyList(), true
                ),
                addition5 = CrossSourceAgreementResult(
                    CrossSourceAgreementStatus.INSUFFICIENT_AGREEMENT_DATA, emptyList(), emptyList(), emptyList(), CorrelationReason.INSUFFICIENT_DATA
                ),
                addition6 = SelfContradictionResult(SelfContradictionStatus.SELF_CONTRADICTION_DETECTED, listOf(SelfContradictionPair(0, 1))),
                addition7 = FinalAnswerClaimCoverageResult(FinalAnswerClaimCoverageStatus.COVERAGE_INCOMPLETE, listOf("claim-1"))
            )
        )

        assertEquals(FinalDecision.BLOCK, result.decision)
        assertEquals(FinalDecisionReason.ADDITION_1_BLOCKED, result.reason)
    }

    @Test
    fun every_non_pass_signal_blocks() {
        val base = FinalDecisionInputFactory.allPassing()
        val mutations = listOf(
            base.copy(addition2 = AttributionResult(false, AttributionReason.NO_FINDINGS, emptyList())) to FinalDecisionReason.ADDITION_2_ATTRIBUTION_FAILED,
            base.copy(addition3 = HallucinationDetectionResult(true, emptyList(), HallucinationSeverity.MEDIUM)) to FinalDecisionReason.ADDITION_3_HALLUCINATION_DETECTED,
            base.copy(addition4 = FinalDecisionInputFactory.addition4Future().addition4) to FinalDecisionReason.ADDITION_4_FUTURE_TIMESTAMP,
            base.copy(addition4 = FinalDecisionInputFactory.addition4Inconsistent().addition4) to FinalDecisionReason.ADDITION_4_TEMPORALLY_INCONSISTENT,
            base.copy(addition5 = FinalDecisionInputFactory.addition5Disagreement().addition5) to FinalDecisionReason.ADDITION_5_DISAGREEMENT,
            base.copy(addition6 = FinalDecisionInputFactory.addition6Detected().addition6) to FinalDecisionReason.ADDITION_6_SELF_CONTRADICTION,
            base.copy(addition7 = FinalDecisionInputFactory.addition7Incomplete().addition7) to FinalDecisionReason.ADDITION_7_COVERAGE_INCOMPLETE
        )
        mutations.forEach { (input, reason) ->
            val result = gate.evaluate(input)
            assertEquals(FinalDecision.BLOCK, result.decision)
            assertEquals(reason, result.reason)
        }
    }

    private fun input(
        addition1: BlockingResult,
        addition2: AttributionResult,
        addition3: HallucinationDetectionResult,
        addition4: TemporalConsistencyResult,
        addition5: CrossSourceAgreementResult,
        addition6: SelfContradictionResult,
        addition7: FinalAnswerClaimCoverageResult
    ) = FinalHallucinationDecisionInput(addition1, addition2, addition3, addition4, addition5, addition6, addition7)
}

private object FinalDecisionInputFactory {
    fun allPassing() = FinalHallucinationDecisionInput(
        BlockingResult.passed(),
        AttributionResult(true, AttributionReason.ALL_ATTRIBUTED, emptyList()),
        HallucinationDetectionResult(false, emptyList(), HallucinationSeverity.NONE),
        TemporalConsistencyResult(TemporalConsistencyStatus.TEMPORALLY_CONSISTENT, emptyList(), emptyList(), emptyList(), false),
        CrossSourceAgreementResult(CrossSourceAgreementStatus.READY, emptyList(), emptyList(), emptyList(), CorrelationReason.VALID_CORRELATION),
        SelfContradictionResult(SelfContradictionStatus.NO_SELF_CONTRADICTION, emptyList()),
        FinalAnswerClaimCoverageResult(FinalAnswerClaimCoverageStatus.COVERAGE_COMPLETE, emptyList())
    )

    fun addition4Insufficient() = allPassing().copy(
        addition4 = TemporalConsistencyResult(TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA, emptyList(), listOf("answer-claim-0"), emptyList(), false)
    )
    fun addition4Future() = allPassing().copy(
        addition4 = TemporalConsistencyResult(TemporalConsistencyStatus.FUTURE_TIMESTAMP, emptyList(), emptyList(), listOf("answer-claim-0"), false)
    )
    fun addition4Inconsistent() = allPassing().copy(
        addition4 = TemporalConsistencyResult(TemporalConsistencyStatus.TEMPORALLY_INCONSISTENT, listOf("answer-claim-0"), emptyList(), emptyList(), false)
    )
    fun addition5Insufficient() = allPassing().copy(
        addition5 = CrossSourceAgreementResult(CrossSourceAgreementStatus.INSUFFICIENT_AGREEMENT_DATA, emptyList(), emptyList(), emptyList(), CorrelationReason.INSUFFICIENT_DATA)
    )
    fun addition5Disagreement() = allPassing().copy(
        addition5 = CrossSourceAgreementResult(CrossSourceAgreementStatus.READY, emptyList(), emptyList(), listOf(
            com.personal.gridbot.amaros.agent.correlation.CorrelatedGroup(emptySet(), com.personal.gridbot.amaros.agent.correlation.CorrelationType.DISAGREEMENT, emptyList())
        ), CorrelationReason.CONFLICT_UPSTREAM)
    )
    fun addition6Insufficient() = allPassing().copy(
        addition6 = SelfContradictionResult(SelfContradictionStatus.INSUFFICIENT_SELF_CONTRADICTION_DATA, emptyList())
    )
    fun addition6Detected() = allPassing().copy(
        addition6 = SelfContradictionResult(SelfContradictionStatus.SELF_CONTRADICTION_DETECTED, listOf(SelfContradictionPair(0, 1)))
    )
    fun addition7Insufficient() = allPassing().copy(
        addition7 = FinalAnswerClaimCoverageResult(FinalAnswerClaimCoverageStatus.INSUFFICIENT_COVERAGE_DATA, emptyList())
    )
    fun addition7Incomplete() = allPassing().copy(
        addition7 = FinalAnswerClaimCoverageResult(FinalAnswerClaimCoverageStatus.COVERAGE_INCOMPLETE, listOf("claim-1"))
    )
}

