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

    private fun finding(
        uri: String,
        stance: EvidenceStance,
        authority: Authority = Authority.OFFICIAL
    ) = ResearchFinding(
        sourceTitle = uri,
        sourceUri = uri,
        evidence = "Evidence from $uri",
        authority = authority,
        stance = stance
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
    fun critic_failure_gets_one_bounded_revision() = runBlocking {
        val provider = SequencedProvider(ArrayDeque(listOf("The result is guaranteed.", "The result is uncertain.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"))
        assertEquals(2, provider.calls)
        assertEquals(1, result.revisionCount)
        assertTrue(result.critique.accepted)
        assertEquals(AmarAgentResponse.Status.READY, result.response.status)
        assertTrue(result.trace.any { it.type == AmarReasoningStepType.REVISION })
        assertTrue(result.trace.any { it.type == AmarReasoningStepType.REVISED_DRAFT })
    }

    @Test
    fun repeated_critic_failure_is_blocked() = runBlocking {
        val provider = SequencedProvider(ArrayDeque(listOf("The result is guaranteed.", "The result is guaranteed.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(AmarAgentRequest("question"))
        assertEquals(2, provider.calls)
        assertEquals(1, result.revisionCount)
        assertTrue(result.critique.issues.isNotEmpty())
        assertEquals(AmarAgentResponse.Status.ERROR, result.response.status)
        assertEquals(AmarReasoningStepType.FINAL_STATE, result.trace.last().type)
    }

    @Test
    fun structured_trace_separates_facts_and_records_step_confidence() = runBlocking {
        val evidence = listOf(
            finding("https://source-a", EvidenceStance.SUPPORTS),
            finding("https://source-b", EvidenceStance.SUPPORTS)
        )
        val provider = SequencedProvider(ArrayDeque(listOf("Evidence-based answer.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(
            AmarAgentRequest("question"), evidence = evidence
        )

        assertTrue(result.critique.accepted)
        assertEquals(2, result.facts.size)
        assertTrue(result.inferences.isNotEmpty())
        assertTrue(result.assumptions.isEmpty())
        assertTrue(result.finalConfidence > 0.0)
        assertTrue(result.trace.map { it.type }.containsAll(listOf(
            AmarReasoningStepType.DRAFT,
            AmarReasoningStepType.FACTS,
            AmarReasoningStepType.INFERENCE,
            AmarReasoningStepType.CONTRADICTION_CHECK,
            AmarReasoningStepType.FINAL_STATE
        )))
        assertTrue(result.trace.all { it.confidenceBefore in 0.0..1.0 && (it.confidenceAfter == null || it.confidenceAfter in 0.0..1.0) })
    }

    @Test
    fun contradictory_evidence_is_explicitly_detected_and_reduces_confidence() = runBlocking {
        val evidence = listOf(
            finding("https://source-a", EvidenceStance.SUPPORTS),
            finding("https://source-b", EvidenceStance.OPPOSES)
        )
        val provider = SequencedProvider(ArrayDeque(listOf("The evidence is conflicting.")))
        val result = AmarStageThreeReasoningEngine(provider).reason(
            AmarAgentRequest("question"), evidence = evidence
        )

        assertFalse(result.critique.accepted)
        assertTrue(result.critique.issues.contains("evidence_conflict"))
        assertTrue(result.trace.any { it.type == AmarReasoningStepType.CONTRADICTION_CHECK && it.conflict })
        assertTrue(result.finalConfidence < 0.80)
    }
}
