package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class TemporalConsistencyCheckTest {
    private val cleanHallucination = HallucinationDetectionResult(
        hallucinationDetected = false,
        reasons = emptyList(),
        severity = HallucinationSeverity.NONE
    )

    private fun report(vararg claims: String) =
        AmarClaimVerificationReport(
            claims.mapIndexed { index, claim ->
                AmarClaimVerification(
                    claim = claim,
                    supportingEvidence = 1,
                    opposingEvidence = 0,
                    accepted = true,
                    matchedEvidence = 1
                )
            },
            accepted = claims.isNotEmpty()
        )

    private fun record(
        claimId: String = "answer-claim-0",
        claimTime: Long? = 1_000L,
        eventTime: Long? = 900L,
        retrievedAt: Long? = 950L
    ) = TemporalEvidenceRecord(
        claimId,
        claimTime,
        "evidence-1",
        eventTime,
        retrievedAt
    )

    @Test
    fun consistent_when_evidence_event_precedes_claim_reference() {
        val result = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            listOf(record()),
            cleanHallucination,
            2_000L
        )
        assertEquals(TemporalConsistencyStatus.TEMPORALLY_CONSISTENT, result.status)
        assertEquals(false, result.flaggedClaims.isNotEmpty())
    }

    @Test
    fun inconsistent_is_flag_only() {
        val result = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            listOf(record(eventTime = 1_100L)),
            cleanHallucination,
            2_000L
        )
        assertEquals(TemporalConsistencyStatus.TEMPORALLY_INCONSISTENT, result.status)
        assertEquals(listOf("answer-claim-0"), result.flaggedClaims)
    }

    @Test
    fun missing_temporal_data_is_insufficient() {
        val result = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            listOf(record(claimTime = null)),
            cleanHallucination,
            2_000L
        )
        assertEquals(TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA, result.status)
        assertEquals(listOf("answer-claim-0"), result.insufficientClaims)
    }

    @Test
    fun negative_timestamp_is_not_revalidated_here() {
        val result = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            listOf(record(eventTime = -1L)),
            cleanHallucination,
            2_000L
        )
        assertEquals(TemporalConsistencyStatus.TEMPORALLY_CONSISTENT, result.status)
    }

    @Test
    fun future_timestamp_is_not_inconsistency() {
        val result = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            listOf(record(eventTime = 2_100L)),
            cleanHallucination,
            2_000L
        )
        assertEquals(TemporalConsistencyStatus.FUTURE_TIMESTAMP, result.status)
        assertEquals(emptyList<String>(), result.flaggedClaims)
    }

    @Test
    fun empty_temporal_input_is_insufficient() {
        val result = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            emptyList(),
            cleanHallucination,
            2_000L
        )
        assertEquals(TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA, result.status)
    }

    @Test
    fun no_temporal_match_is_insufficient() {
        val result = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            listOf(record(claimId = "answer-claim-9")),
            cleanHallucination,
            2_000L
        )
        assertEquals(TemporalConsistencyStatus.INSUFFICIENT_TEMPORAL_DATA, result.status)
    }

    @Test
    fun consumes_addition3_output_without_recomputing_it() {
        val upstream = cleanHallucination.copy(hallucinationDetected = true)
        val result = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            listOf(record()),
            upstream,
            2_000L
        )
        assertEquals(true, result.upstreamHallucinationDetected)
        assertEquals(TemporalConsistencyStatus.TEMPORALLY_CONSISTENT, result.status)
    }

    @Test
    fun does_not_create_blocking_authority() {
        val result = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            listOf(record(eventTime = 1_100L)),
            cleanHallucination,
            2_000L
        )
        assertEquals(TemporalConsistencyStatus.TEMPORALLY_INCONSISTENT, result.status)
        assertEquals(listOf("answer-claim-0"), result.flaggedClaims)
    }

    @Test
    fun deterministic_repeated_evaluation() {
        val input = listOf(record(eventTime = 1_100L))
        val first = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            input,
            cleanHallucination,
            2_000L
        )
        val second = TemporalConsistencyCheck.evaluate(
            report("The claim is temporally grounded."),
            input,
            cleanHallucination,
            2_000L
        )
        assertEquals(first, second)
    }
}
