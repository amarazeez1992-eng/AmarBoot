package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStageThreeReasoningTest {
    private class SequencedProvider(private val answers: ArrayDeque<String>) : AmarReasoningProvider {
        var calls = 0
        override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
            calls++
            return AmarAgentResponse(answers.removeFirst())
        }
    }

    private fun finding(uri: String, stance: EvidenceStance, fingerprint: String = "fp-$uri") = ResearchFinding(
        sourceTitle = uri,
        sourceUri = uri,
        evidence = "Evidence from $uri",
        authority = Authority.OFFICIAL,
        stance = stance,
        fingerprint = fingerprint
    )

    @Test
    fun accepted_draft_does_not_retry() = runBlocking {
        val provider = SequencedProvider(ArrayDeque(listOf("A supported answer.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"))
        assertEquals(1, provider.calls)
        assertEquals(0, result.revisionCount)
        assertTrue(result.critique.accepted)
        assertEquals(AmarAgentResponse.Status.READY, result.response.status)
    }

    @Test
    fun approved_reasoning_trace_has_explicit_bounded_steps_and_safe_audit_data() = runBlocking {
        val evidence = listOf(finding("source-a", EvidenceStance.SUPPORTS), finding("source-b", EvidenceStance.SUPPORTS))
        val provider = SequencedProvider(ArrayDeque(listOf("Evidence-based answer.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"), evidence)

        assertTrue(result.trace.map { it.step }.containsAll(listOf(
            AmarReasoningStep.PLANNING,
            AmarReasoningStep.EVIDENCE_CLASSIFICATION,
            AmarReasoningStep.CONTRADICTION_CHECK,
            AmarReasoningStep.DRAFT,
            AmarReasoningStep.INFERENCE_CLASSIFICATION,
            AmarReasoningStep.FINAL_STATE
        )))
        assertTrue(result.trace.size <= 32)
        assertTrue(result.trace.all { it.confidenceBefore in 0.0..1.0 && it.confidenceAfter in 0.0..1.0 })
        assertTrue(result.trace.zipWithNext().all { it.first.timestampEpochMs <= it.second.timestampEpochMs })
        assertEquals(2, result.facts.size)
        assertEquals(1, result.inferences.size)
        assertTrue(result.assumptions.isEmpty())
    }

    @Test
    fun independent_confidence_is_separate_from_critic_and_supports_independent_sources() = runBlocking {
        val evidence = listOf(
            finding("source-a", EvidenceStance.SUPPORTS),
            finding("source-b", EvidenceStance.SUPPORTS),
            finding("source-c", EvidenceStance.SUPPORTS)
        )
        val provider = SequencedProvider(ArrayDeque(listOf("Evidence-based answer.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"), evidence)

        assertTrue(result.finalConfidence > 0.80)
        assertTrue(result.trace.any { it.step == AmarReasoningStep.EVIDENCE_CLASSIFICATION && it.confidenceAfter > 0.80 })
        assertTrue(result.trace.any { it.step == AmarReasoningStep.INFERENCE_CLASSIFICATION && it.criticResult == "INFERENCE" })
    }

    @Test
    fun contradiction_is_detected_and_confidence_is_reduced_before_acceptance() = runBlocking {
        val evidence = listOf(finding("source-a", EvidenceStance.SUPPORTS), finding("source-b", EvidenceStance.OPPOSES))
        val provider = SequencedProvider(ArrayDeque(listOf("The evidence conflicts.", "The evidence still conflicts.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"), evidence)

        assertFalse(result.critique.accepted)
        assertTrue(result.critique.issues.contains("evidence_conflict"))
        assertTrue(result.trace.any { it.step == AmarReasoningStep.CONTRADICTION_CHECK && it.conflict })
        assertTrue(result.finalConfidence < 0.80)
        assertEquals(2, provider.calls)
    }

    @Test
    fun critic_failure_gets_exactly_one_bounded_revision() = runBlocking {
        val provider = SequencedProvider(ArrayDeque(listOf("The result is guaranteed.", "The result is uncertain.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"))
        assertEquals(2, provider.calls)
        assertEquals(1, result.revisionCount)
        assertTrue(result.critique.accepted)
        assertTrue(result.trace.any { it.step == AmarReasoningStep.REVISION })
        assertTrue(result.trace.any { it.step == AmarReasoningStep.REVISED_DRAFT })
    }

    @Test
    fun repeated_failure_is_blocked_without_another_retry() = runBlocking {
        val provider = SequencedProvider(ArrayDeque(listOf("The result is guaranteed.", "The result is guaranteed.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"))
        assertEquals(2, provider.calls)
        assertEquals(1, result.revisionCount)
        assertEquals(AmarAgentResponse.Status.ERROR, result.response.status)
        assertEquals(AmarReasoningStep.FINAL_STATE, result.trace.last().step)
    }

    @Test
    fun no_evidence_is_explicitly_recorded_as_assumption() = runBlocking {
        val provider = SequencedProvider(ArrayDeque(listOf("A reasonable answer.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"))
        assertEquals(listOf("No external evidence supplied; answer must preserve uncertainty"), result.assumptions)
        assertEquals(0, result.finalConfidence.toInt())
    }

    @Test
    fun evidence_is_bounded_and_invalid_entries_are_not_promoted_to_facts() = runBlocking {
        val invalid = ResearchFinding("", "", "", Authority.UNKNOWN, EvidenceStance.UNKNOWN)
        val valid = finding("source-a", EvidenceStance.SUPPORTS)
        val provider = SequencedProvider(ArrayDeque(listOf("Answer.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"), listOf(invalid, valid))
        assertEquals(1, result.facts.size)
        assertTrue(result.trace.any { it.step == AmarReasoningStep.EVIDENCE_CLASSIFICATION && it.claim.contains("invalid=1") })
    }
}
