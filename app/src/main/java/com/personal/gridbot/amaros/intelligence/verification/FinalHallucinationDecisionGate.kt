package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AttributionResult
import com.personal.gridbot.amaros.agent.BlockingResult
import com.personal.gridbot.amaros.agent.HallucinationDetectionResult
import com.personal.gridbot.amaros.agent.TemporalConsistencyResult
import com.personal.gridbot.amaros.agent.correlation.CrossSourceAgreementResult

/**
 * Stage 11 Item 4 — Addition 8.
 *
 * The only blocking authority for the Item 4 hallucination-firewall additions.
 * Deterministic, fail-closed, and confidence-free.
 */
class FinalHallucinationDecisionGate {

    fun evaluate(input: FinalHallucinationDecisionInput): FinalDecisionResult {
        val missingAdditions = input.missingAdditionNumbers()
        if (missingAdditions.isNotEmpty()) {
            return block(
                reason = FinalDecisionReason.INSUFFICIENT_ADDITION_N_DATA,
                missingAdditionNumbers = missingAdditions
            )
        }

        input.addition1!!.let {
            if (it.blocked) {
                return block(FinalDecisionReason.ADDITION_1_BLOCKED)
            }
        }

        if (!input.addition2!!.attributed) {
            return block(FinalDecisionReason.ADDITION_2_ATTRIBUTION_FAILED)
        }

        if (input.addition3!!.hallucinationDetected) {
            return block(FinalDecisionReason.ADDITION_3_HALLUCINATION_DETECTED)
        }

        when (input.addition4!!.status) {
            com.personal.gridbot.amaros.agent.TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA ->
                return block(FinalDecisionReason.ADDITION_4_INSUFFICIENT_TEMPORAL_DATA)
            com.personal.gridbot.amaros.agent.TemporalConsistencyStatus.FUTURE_TIMESTAMP ->
                return block(FinalDecisionReason.ADDITION_4_FUTURE_TIMESTAMP)
            com.personal.gridbot.amaros.agent.TemporalConsistencyStatus.TEMPORALLY_INCONSISTENT ->
                return block(FinalDecisionReason.ADDITION_4_TEMPORALLY_INCONSISTENT)
            com.personal.gridbot.amaros.agent.TemporalConsistencyStatus.TEMPORALLY_CONSISTENT -> Unit
        }

        if (input.addition5!!.status == com.personal.gridbot.amaros.agent.correlation.CrossSourceAgreementStatus.INSUFFICIENT_AGREEMENT_DATA) {
            return block(FinalDecisionReason.ADDITION_5_INSUFFICIENT_AGREEMENT_DATA)
        }
        if (input.addition5.disagreementGroups.isNotEmpty()) {
            return block(FinalDecisionReason.ADDITION_5_DISAGREEMENT)
        }

        when (input.addition6!!.status) {
            SelfContradictionStatus.INSUFFICIENT_SELF_CONTRADICTION_DATA ->
                return block(FinalDecisionReason.ADDITION_6_INSUFFICIENT_SELF_CONTRADICTION_DATA)
            SelfContradictionStatus.SELF_CONTRADICTION_DETECTED ->
                return block(FinalDecisionReason.ADDITION_6_SELF_CONTRADICTION)
            SelfContradictionStatus.NO_SELF_CONTRADICTION -> Unit
        }

        when (input.addition7!!.status) {
            FinalAnswerClaimCoverageStatus.INSUFFICIENT_COVERAGE_DATA ->
                return block(FinalDecisionReason.ADDITION_7_INSUFFICIENT_COVERAGE_DATA)
            FinalAnswerClaimCoverageStatus.COVERAGE_INCOMPLETE ->
                return block(FinalDecisionReason.ADDITION_7_COVERAGE_INCOMPLETE)
            FinalAnswerClaimCoverageStatus.COVERAGE_COMPLETE -> Unit
        }

        return FinalDecisionResult(FinalDecision.PASS, FinalDecisionReason.NO_BLOCKING_SIGNAL)
    }

    private fun block(
        reason: FinalDecisionReason,
        missingAdditionNumbers: List<Int> = emptyList()
    ): FinalDecisionResult =
        FinalDecisionResult(
            decision = FinalDecision.BLOCK,
            reason = reason,
            missingAdditionNumbers = missingAdditionNumbers
        )
}

data class FinalHallucinationDecisionInput(
    val addition1: BlockingResult?,
    val addition2: AttributionResult?,
    val addition3: HallucinationDetectionResult?,
    val addition4: TemporalConsistencyResult?,
    val addition5: CrossSourceAgreementResult?,
    val addition6: SelfContradictionResult?,
    val addition7: FinalAnswerClaimCoverageResult?
) {
    fun missingAdditionNumbers(): List<Int> = buildList {
        if (addition1 == null) add(1)
        if (addition2 == null) add(2)
        if (addition3 == null) add(3)
        if (addition4 == null) add(4)
        if (addition5 == null) add(5)
        if (addition6 == null) add(6)
        if (addition7 == null) add(7)
    }
}

enum class FinalDecision {
    PASS,
    BLOCK
}

enum class FinalDecisionReason {
    INSUFFICIENT_ADDITION_N_DATA,
    ADDITION_1_BLOCKED,
    ADDITION_2_ATTRIBUTION_FAILED,
    ADDITION_3_HALLUCINATION_DETECTED,
    ADDITION_4_INSUFFICIENT_TEMPORAL_DATA,
    ADDITION_4_FUTURE_TIMESTAMP,
    ADDITION_4_TEMPORALLY_INCONSISTENT,
    ADDITION_5_INSUFFICIENT_AGREEMENT_DATA,
    ADDITION_5_DISAGREEMENT,
    ADDITION_6_INSUFFICIENT_SELF_CONTRADICTION_DATA,
    ADDITION_6_SELF_CONTRADICTION,
    ADDITION_7_INSUFFICIENT_COVERAGE_DATA,
    ADDITION_7_COVERAGE_INCOMPLETE,
    NO_BLOCKING_SIGNAL
}

data class FinalDecisionResult(
    val decision: FinalDecision,
    val reason: FinalDecisionReason,
    val missingAdditionNumbers: List<Int> = emptyList()
)
