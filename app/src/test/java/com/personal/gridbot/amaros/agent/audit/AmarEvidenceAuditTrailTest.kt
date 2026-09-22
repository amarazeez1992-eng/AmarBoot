package com.personal.gridbot.amaros.agent.audit

import com.personal.gridbot.amaros.agent.chain.ChainIntegrity
import com.personal.gridbot.amaros.agent.chain.ChainLinkType
import com.personal.gridbot.amaros.agent.chain.EvidenceChainLink
import com.personal.gridbot.amaros.agent.chain.EvidenceChainReason
import com.personal.gridbot.amaros.agent.chain.EvidenceChainResult
import com.personal.gridbot.amaros.agent.correlation.CorrelationReason
import com.personal.gridbot.amaros.agent.correlation.CrossSourceCorrelationResult
import com.personal.gridbot.amaros.agent.historical.HistoricalValidationReason
import com.personal.gridbot.amaros.agent.historical.HistoricalValidationResult
import com.personal.gridbot.amaros.agent.historical.OutcomeSummary
import com.personal.gridbot.amaros.agent.lifecycle.EvidenceLifecycleResult
import com.personal.gridbot.amaros.agent.lifecycle.EvidenceLifecycleSnapshot
import com.personal.gridbot.amaros.agent.lifecycle.EvidenceLifecycleState
import com.personal.gridbot.amaros.agent.lifecycle.LifecycleReason
import com.personal.gridbot.amaros.agent.lifecycle.LifecycleTransition
import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceAuditTrailTest {
    private val builder = AmarEvidenceAuditTrailBuilder()

    @Test
    fun empty_input_returns_empty_result() {
        val result = builder.build(emptyInput())
        assertTrue(result.entries.isEmpty())
        assertTrue(result.isDownstreamReady)
        assertEquals(AuditReason.VALID_AUDIT, result.reason)
    }

    @Test
    fun single_entry_created() {
        val result = builder.build(input(nodes = listOf(node("a", 10L))))
        assertEquals(1, result.entries.size)
        assertEquals(AuditEventType.EVIDENCE_INTAKE, result.entries.single().eventType)
    }

    @Test
    fun multiple_entries_ordered_by_timestamp() {
        val result = builder.build(
            input(nodes = listOf(node("a", 30L), node("b", 10L), node("c", 20L)))
        )
        assertEquals(listOf(10L, 20L, 30L), result.entries.map { it.timestamp })
    }

    @Test
    fun entries_are_immutable() {
        val result = builder.build(input(nodes = listOf(node("a", 10L))))
        @Suppress("UNCHECKED_CAST")
        val mutableView = result.entries as MutableList<EvidenceAuditEntry>
        var rejected = false
        try {
            mutableView.add(result.entries.single())
        } catch (_: UnsupportedOperationException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    @Test
    fun audit_id_is_deterministic() {
        val value = input(nodes = listOf(node("a", 10L)))
        assertEquals(
            builder.build(value).entries.single().auditId,
            builder.build(value).entries.single().auditId
        )
    }

    @Test
    fun lifecycle_transition_recorded() {
        val transition = LifecycleTransition(
            fromState = EvidenceLifecycleState.ACTIVE,
            toState = EvidenceLifecycleState.AGED,
            timestamp = 20L,
            reason = "aged threshold reached"
        )
        val snapshot = EvidenceLifecycleSnapshot(
            evidenceFingerprint = fp("a"),
            state = EvidenceLifecycleState.AGED,
            createdAt = 0L,
            lastTransitionAt = 20L,
            transitions = listOf(transition)
        )
        val result = builder.build(
            input(
                nodes = listOf(node("a", 10L)),
                lifecycle = EvidenceLifecycleResult(
                    currentStates = listOf(snapshot),
                    transitions = listOf(transition),
                    isDownstreamReady = true,
                    reason = LifecycleReason.VALID_LIFECYCLE
                )
            )
        )
        assertTrue(result.entries.any { it.eventType == AuditEventType.LIFECYCLE_TRANSITION })
    }

    @Test
    fun chain_link_recorded() {
        val result = builder.build(
            input(
                nodes = listOf(node("a", 10L)),
                links = listOf(
                    EvidenceChainLink(
                        fromFingerprint = "GENESIS",
                        toFingerprint = fp("a"),
                        linkType = ChainLinkType.PROVENANCE,
                        reason = "existing provenance relationship"
                    )
                )
            )
        )
        assertTrue(result.entries.any { it.eventType == AuditEventType.CHAIN_LINK_CREATED })
    }

    @Test
    fun provenance_consumed_not_rebuilt() {
        val original = node("a", 10L)
        val result = builder.build(input(nodes = listOf(original)))
        val intake = result.entries.single()
        assertEquals(original.evidenceFingerprint, intake.evidenceFingerprint)
        assertEquals(original.previousHash, intake.details["previousHash"])
        assertEquals(original.chainHash, intake.details["chainHash"])
    }

    @Test
    fun is_deterministic() {
        val value = input(
            nodes = listOf(node("a", 10L), node("b", 20L)),
            links = listOf(
                EvidenceChainLink("GENESIS", fp("a"), ChainLinkType.PROVENANCE, "p")
            )
        )
        assertEquals(builder.build(value), builder.build(value))
    }

    @Test
    fun is_stateless() {
        val value = input(nodes = listOf(node("a", 10L)))
        val first = builder.build(value)
        builder.build(input(nodes = listOf(node("b", 10L))))
        val second = builder.build(value)
        assertEquals(first, second)
    }

    @Test
    fun no_recalculation_of_upstream() {
        val value = input(
            nodes = listOf(node("a", 10L)),
            links = listOf(
                EvidenceChainLink("GENESIS", fp("a"), ChainLinkType.PROVENANCE, "p")
            )
        )
        val before = value
        builder.build(value)
        assertEquals(before, value)
    }

    @Test
    fun preserves_evidence() {
        val result = builder.build(
            input(nodes = listOf(node("a", 10L), node("b", 20L)))
        )
        assertEquals(
            setOf(fp("a"), fp("b")),
            result.entries
                .filter { it.eventType == AuditEventType.EVIDENCE_INTAKE }
                .map { it.evidenceFingerprint }
                .toSet()
        )
    }

    @Test
    fun fail_closed_on_missing_upstream() {
        val base = input(nodes = listOf(node("a", 10L)))
        val value = base.copy(
            currentEvidence = EvidenceChainResult(
                chainLinks = base.currentEvidence.chainLinks,
                chainIntegrity = ChainIntegrity.INTACT,
                isDownstreamReady = false,
                reason = EvidenceChainReason.INVALID_INPUT
            )
        )
        val result = builder.build(value)
        assertFalse(result.isDownstreamReady)
        assertEquals(AuditReason.MISSING_UPSTREAM, result.reason)
        assertTrue(result.entries.isEmpty())
    }

    @Test
    fun is_downstream_ready_when_valid() {
        val result = builder.build(input(nodes = listOf(node("a", 10L))))
        assertTrue(result.isDownstreamReady)
        assertEquals(AuditReason.VALID_AUDIT, result.reason)
    }

    @Test
    fun does_not_reuse_general_audit_log() {
        val result = builder.build(input(nodes = listOf(node("a", 10L))))
        assertTrue(result.entries.all { it.actor != "AmarAuditLog" && it.actor != "AmarAuditTrail" })
    }

    private fun input(
        nodes: List<AmarProvenanceNode>,
        links: List<EvidenceChainLink> = emptyList(),
        lifecycle: EvidenceLifecycleResult = lifecycle(),
        currentTimeMs: Long = 100L
    ) = EvidenceAuditTrailInput(
        currentEvidence = EvidenceChainResult(
            chainLinks = links,
            chainIntegrity = ChainIntegrity.INTACT,
            isDownstreamReady = true,
            reason = EvidenceChainReason.VALID_CHAIN
        ),
        lifecycleResult = lifecycle,
        provenanceNodes = nodes,
        historicalValidation = HistoricalValidationResult(
            comparableCases = emptyList(),
            observedOutcomes = OutcomeSummary(0, emptyMap()),
            frequency = 0,
            differences = emptyList(),
            isDownstreamReady = true,
            reason = HistoricalValidationReason.NO_COMPARABLE_CASES
        ),
        crossSourceCorrelation = CrossSourceCorrelationResult(
            correlatedGroups = emptyList(),
            isDownstreamReady = true,
            reason = CorrelationReason.INSUFFICIENT_DATA
        ),
        currentTimeMs = currentTimeMs
    )

    private fun emptyInput() = EvidenceAuditTrailInput(
        currentEvidence = EvidenceChainResult(
            chainLinks = emptyList(),
            chainIntegrity = ChainIntegrity.INTACT,
            isDownstreamReady = true,
            reason = EvidenceChainReason.VALID_CHAIN
        ),
        lifecycleResult = lifecycle(),
        provenanceNodes = emptyList(),
        historicalValidation = HistoricalValidationResult(
            comparableCases = emptyList(),
            observedOutcomes = OutcomeSummary(0, emptyMap()),
            frequency = 0,
            differences = emptyList(),
            isDownstreamReady = true,
            reason = HistoricalValidationReason.NO_COMPARABLE_CASES
        ),
        crossSourceCorrelation = CrossSourceCorrelationResult(
            correlatedGroups = emptyList(),
            isDownstreamReady = true,
            reason = CorrelationReason.INSUFFICIENT_DATA
        ),
        currentTimeMs = 100L
    )

    private fun lifecycle() = EvidenceLifecycleResult(
        currentStates = emptyList(),
        transitions = emptyList(),
        isDownstreamReady = true,
        reason = LifecycleReason.NO_BASELINE_AVAILABLE
    )

    private fun node(seed: String, retrievedAt: Long) = AmarProvenanceNode(
        sequence = 0,
        sourceUri = "https://example.com/$seed",
        evidenceFingerprint = fp(seed),
        retrievedAtEpochMs = retrievedAt,
        previousHash = "GENESIS",
        chainHash = "chain-$seed"
    )

    private fun fp(seed: String) = seed.repeat(64).take(64)
}
