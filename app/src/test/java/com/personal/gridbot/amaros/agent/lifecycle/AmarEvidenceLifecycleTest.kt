package com.personal.gridbot.amaros.agent.lifecycle

import com.personal.gridbot.amaros.agent.chain.ChainIntegrity
import com.personal.gridbot.amaros.agent.chain.ChainLinkType
import com.personal.gridbot.amaros.agent.chain.EvidenceChainLink
import com.personal.gridbot.amaros.agent.chain.EvidenceChainReason
import com.personal.gridbot.amaros.agent.chain.EvidenceChainResult
import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceLifecycleTest {
    private val manager = AmarEvidenceLifecycleManager()
    private val policy = LifecyclePolicy(agedAfterMs = 100L, expiredAfterMs = 200L)

    @Test fun empty_input_returns_empty_result() {
        val result = manager.evaluate(input(emptyList(), emptyList(), 0L))
        assertTrue(result.currentStates.isEmpty())
        assertTrue(result.isDownstreamReady)
        assertEquals(LifecycleReason.NO_BASELINE_AVAILABLE, result.reason)
    }

    @Test fun no_baseline_creates_initial_snapshot() {
        val result = manager.evaluate(input(listOf("a"), emptyList(), 50L))
        assertEquals(EvidenceLifecycleState.ACTIVE, result.currentStates.single().state)
        assertEquals(0L, result.currentStates.single().createdAt)
        assertEquals(LifecycleReason.NO_BASELINE_AVAILABLE, result.reason)
    }

    @Test fun active_state_for_fresh_evidence() {
        val result = manager.evaluate(input(listOf("a"), emptyList(), 99L))
        assertEquals(EvidenceLifecycleState.ACTIVE, result.currentStates.single().state)
    }

    @Test fun aged_state_after_threshold() {
        val result = manager.evaluate(input(listOf("a"), emptyList(), 100L))
        assertEquals(EvidenceLifecycleState.AGED, result.currentStates.single().state)
    }

    @Test fun expired_state_after_threshold() {
        val result = manager.evaluate(input(listOf("a"), emptyList(), 200L))
        assertEquals(EvidenceLifecycleState.EXPIRED, result.currentStates.single().state)
    }

    @Test fun transitions_are_recorded() {
        val previous = snapshot("a", EvidenceLifecycleState.ACTIVE, 0L)
        val result = manager.evaluate(input(listOf("a"), listOf(previous), 100L))
        assertEquals(1, result.transitions.size)
        assertEquals(EvidenceLifecycleState.ACTIVE, result.transitions.single().fromState)
        assertEquals(EvidenceLifecycleState.AGED, result.transitions.single().toState)
    }

    @Test fun is_deterministic() {
        val value = input(listOf("a", "b"), emptyList(), 150L)
        assertEquals(manager.evaluate(value), manager.evaluate(value))
    }

    @Test fun is_stateless() {
        val value = input(listOf("a"), emptyList(), 150L)
        assertEquals(manager.evaluate(value), manager.evaluate(value))
    }

    @Test fun no_recalculation_of_upstream() {
        val chain = chain(listOf("a"))
        val provenance = provenance(listOf("a"))
        val before = Pair(chain, provenance)
        manager.evaluate(EvidenceLifecycleInput(chain, emptyMap(), provenance, 50L, policy))
        assertEquals(before, Pair(chain, provenance))
    }

    @Test fun preserves_evidence() {
        val result = manager.evaluate(input(listOf("a", "b"), emptyList(), 150L))
        assertEquals(listOf(fp("a"), fp("b")), result.currentStates.map { it.evidenceFingerprint })
    }

    @Test fun fail_closed_on_missing_provenance() {
        val result = manager.evaluate(
            input(listOf("a"), emptyList(), 50L, provenanceNodes = emptyList())
        )
        assertEquals(LifecycleReason.MISSING_PROVENANCE, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun fail_closed_on_future_timestamp() {
        val result = manager.evaluate(input(listOf("a"), emptyList(), 50L, retrievedAtMs = 100L))
        assertEquals(LifecycleReason.TRANSITION_FAILED, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun is_downstream_ready_when_valid() {
        val result = manager.evaluate(input(listOf("a"), emptyList(), 50L))
        assertTrue(result.isDownstreamReady)
    }

    @Test fun uses_provenance_fingerprint_not_entity_id() {
        val result = manager.evaluate(input(listOf("a"), emptyList(), 50L))
        assertEquals(fp("a"), result.currentStates.single().evidenceFingerprint)
    }

    @Test fun does_not_reimplement_change_detection() {
        val previous = snapshot("a", EvidenceLifecycleState.ACTIVE, 0L)
        val result = manager.evaluate(input(listOf("a"), listOf(previous), 50L))
        assertTrue(result.transitions.isEmpty())
        assertEquals(EvidenceLifecycleState.ACTIVE, result.currentStates.single().state)
    }

    private fun input(
        seeds: List<String>,
        snapshots: List<EvidenceLifecycleSnapshot>,
        currentTimeMs: Long,
        retrievedAtMs: Long = 0L,
        provenanceNodes: List<AmarProvenanceNode> = provenance(seeds, retrievedAtMs)
    ) = EvidenceLifecycleInput(
        currentEvidence = chain(seeds),
        previousSnapshots = snapshots.associateBy { it.evidenceFingerprint },
        provenanceNodes = provenanceNodes,
        currentTimeMs = currentTimeMs,
        policy = policy
    )

    private fun chain(seeds: List<String>) = EvidenceChainResult(
        chainLinks = seeds.map {
            EvidenceChainLink(fp("GENESIS"), fp(it), ChainLinkType.PROVENANCE, "test")
        },
        chainIntegrity = ChainIntegrity.INTACT,
        isDownstreamReady = true,
        reason = EvidenceChainReason.VALID_CHAIN
    )

    private fun provenance(seeds: List<String>, retrievedAtMs: Long = 0L) =
        seeds.mapIndexed { index, seed ->
            AmarProvenanceNode(
                sequence = index,
                sourceUri = "https://example.com/$seed",
                evidenceFingerprint = fp(seed),
                retrievedAtEpochMs = retrievedAtMs,
                previousHash = if (index == 0) "GENESIS" else "previous",
                chainHash = "chain-$seed"
            )
        }

    private fun snapshot(
        seed: String,
        state: EvidenceLifecycleState,
        createdAt: Long
    ) = EvidenceLifecycleSnapshot(
        evidenceFingerprint = fp(seed),
        state = state,
        createdAt = createdAt,
        lastTransitionAt = createdAt,
        transitions = emptyList()
    )

    private fun fp(seed: String) = seed.repeat(64).take(64)
}
