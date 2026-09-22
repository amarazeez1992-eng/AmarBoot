package com.personal.gridbot.amaros.agent.chain

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.correlation.CorrelatedGroup
import com.personal.gridbot.amaros.agent.correlation.CorrelationReason
import com.personal.gridbot.amaros.agent.correlation.CorrelationType
import com.personal.gridbot.amaros.agent.correlation.CrossSourceCorrelationResult
import com.personal.gridbot.amaros.agent.deterministic.CanonicalEvidence
import com.personal.gridbot.amaros.agent.deterministic.DeterministicEvidenceResult
import com.personal.gridbot.amaros.agent.deterministic.DeterministicHandlingReason
import com.personal.gridbot.amaros.agent.historical.ComparableCase
import com.personal.gridbot.amaros.agent.historical.HistoricalCase
import com.personal.gridbot.amaros.agent.historical.HistoricalValidationReason
import com.personal.gridbot.amaros.agent.historical.HistoricalValidationResult
import com.personal.gridbot.amaros.agent.historical.OutcomeSummary
import com.personal.gridbot.amaros.agent.protection.ProtectedEvidence
import com.personal.gridbot.amaros.agent.protection.ProtectionReason
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.EvidenceStatus
import com.personal.gridbot.amaros.intelligence.advanced.AmarMarketRegime
import com.personal.gridbot.amaros.intelligence.advanced.AmarRegimeObservation
import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceChainTest {
    private val builder = AmarEvidenceChainBuilder()

    @Test fun empty_input_returns_empty_result() {
        val result = builder.build(input(emptyList()))
        assertFalse(result.isDownstreamReady)
        assertEquals(EvidenceChainReason.INVALID_INPUT, result.reason)
    }

    @Test fun single_evidence_chain_has_one_link() {
        val result = builder.build(input(listOf("a")))
        assertEquals(1, result.chainLinks.size)
        assertEquals(ChainLinkType.PROVENANCE, result.chainLinks.single().linkType)
    }

    @Test fun multiple_evidence_chain_links_ordered() {
        val result = builder.build(input(listOf("a", "b")))
        val provenance = result.chainLinks.filter { it.linkType == ChainLinkType.PROVENANCE }
        assertEquals(listOf(fp("a"), fp("b")), provenance.map { it.toFingerprint })
    }

    @Test fun provenance_consumed_not_rebuilt() {
        val nodes = listOf(node(fp("a"), "GENESIS", "chain-a"))
        val result = builder.build(input(listOf("a"), nodes))
        assertEquals(nodes.single().chainHash, result.chainLinks.single().fromFingerprint)
        assertEquals(nodes.single().evidenceFingerprint, result.chainLinks.single().toFingerprint)
    }

    @Test fun deterministic_links_created() {
        val result = builder.build(input(listOf("a", "b")))
        assertTrue(result.chainLinks.any { it.linkType == ChainLinkType.DETERMINISTIC })
    }

    @Test fun historical_links_created() {
        val id = fp("a")
        val result = builder.build(input(listOf("a"), historicalIds = listOf(id)))
        assertTrue(result.chainLinks.any { it.linkType == ChainLinkType.HISTORICAL })
    }

    @Test fun cross_source_links_created() {
        val result = builder.build(
            input(
                listOf("a", "b"),
                correlation = CrossSourceCorrelationResult(
                    correlatedGroups = listOf(
                        CorrelatedGroup(
                            setOf(fp("a"), fp("b")),
                            CorrelationType.AGREEMENT,
                            emptyList()
                        )
                    ),
                    isDownstreamReady = true,
                    reason = CorrelationReason.VALID_CORRELATION
                )
            )
        )
        assertTrue(result.chainLinks.any { it.linkType == ChainLinkType.CROSS_SOURCE })
    }

    @Test fun chain_integrity_intact_when_valid() {
        val result = builder.build(input(listOf("a", "b")))
        assertEquals(ChainIntegrity.INTACT, result.chainIntegrity)
    }

    @Test fun chain_integrity_broken_on_tamper() {
        val nodes = listOf(
            node(fp("a"), "GENESIS", "chain-a"),
            node(fp("b"), "tampered", "chain-b")
        )
        val result = builder.build(input(listOf("a", "b"), nodes))
        assertEquals(ChainIntegrity.BROKEN, result.chainIntegrity)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun is_deterministic() {
        val contract = input(listOf("a", "b"))
        assertEquals(builder.build(contract), builder.build(contract))
    }

    @Test fun is_stateless() {
        val contract = input(listOf("a", "b"))
        val first = builder.build(contract)
        val second = builder.build(contract)
        assertEquals(first, second)
    }

    @Test fun no_recalculation_of_upstream() {
        val deterministic = deterministic(listOf("a", "b"))
        val historical = historical()
        val correlation = correlation()
        val before = Triple(deterministic, historical, correlation)
        builder.build(EvidenceChainInput(deterministic, historical, correlation, nodesFor(listOf("a", "b"))))
        assertEquals(before, Triple(deterministic, historical, correlation))
    }

    @Test fun no_provenance_rebuild() {
        val nodes = nodesFor(listOf("a", "b"))
        val result = builder.build(input(listOf("a", "b"), nodes))
        assertEquals(
            nodes.map { it.chainHash },
            result.chainLinks.filter { it.linkType == ChainLinkType.PROVENANCE }.map { it.fromFingerprint }
        )
    }

    @Test fun fail_closed_on_missing_provenance() {
        val result = builder.build(
            EvidenceChainInput(
                deterministic(listOf("a")),
                historical(),
                correlation(),
                emptyList()
            )
        )
        assertEquals(EvidenceChainReason.MISSING_PROVENANCE, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun is_downstream_ready_when_valid() {
        assertTrue(builder.build(input(listOf("a"))).isDownstreamReady)
    }

    private fun input(
        seeds: List<String>,
        nodes: List<AmarProvenanceNode> = nodesFor(seeds),
        historicalIds: List<String> = emptyList(),
        correlation: CrossSourceCorrelationResult = correlation()
    ) = EvidenceChainInput(
        deterministicEvidence = deterministic(seeds),
        historicalValidation = historical(historicalIds),
        crossSourceCorrelation = correlation,
        provenanceNodes = nodes
    )

    private fun deterministic(seeds: List<String>) =
        DeterministicEvidenceResult(
            canonicalEvidence = seeds.map {
                val candidate = NormalizedCandidate(
                    provider = "provider-" + it,
                    title = "Title",
                    canonicalUrl = "https://example.com/" + it,
                    normalizedExcerpt = "Excerpt",
                    retrievedAtEpochMs = 1L,
                    fingerprint = fp(it),
                    normalizationFlags = emptySet()
                )
                val classified = ClassifiedEvidence(
                    candidate = RelevantCandidate(candidate, 1.0, "test"),
                    status = EvidenceStatus.COMPLETE,
                    reason = null,
                    explanation = "test"
                )
                CanonicalEvidence(
                    evidence = ProtectedEvidence(classified, ProtectionReason.PASSED_ALL_GATES),
                    canonicalKey = fp(it)
                )
            },
            invalidEvidence = emptyList(),
            futureEvidence = emptyList(),
            isDownstreamReady = true,
            handlingReason = DeterministicHandlingReason.CANONICAL_ORDER_APPLIED
        )

    private fun historical(ids: List<String> = emptyList()) =
        HistoricalValidationResult(
            comparableCases = ids.map { id ->
                ComparableCase(
                    historicalCase = HistoricalCase(
                        id = id,
                        market = "XAUUSD",
                        timeframe = "H1",
                        timestampEpochMs = 900L,
                        regimeObservation = AmarRegimeObservation(
                            AmarMarketRegime.TREND, 0.9, 0.4, 0.8, 0.2, 0.1, 900L
                        ),
                        hypothesisId = "h1",
                        outcome = "UP",
                        attributes = emptyMap()
                    ),
                    differences = emptyList()
                )
            },
            observedOutcomes = OutcomeSummary(
                ids.size,
                if (ids.isEmpty()) emptyMap() else mapOf("UP" to ids.size)
            ),
            frequency = ids.size,
            differences = emptyList(),
            isDownstreamReady = true,
            reason = HistoricalValidationReason.VALID_COMPARABLE_CASES
        )

    private fun correlation() =
        CrossSourceCorrelationResult(
            correlatedGroups = emptyList(),
            isDownstreamReady = true,
            reason = CorrelationReason.INSUFFICIENT_DATA
        )

    private fun nodesFor(seeds: List<String>) =
        seeds.mapIndexed { index, seed ->
            node(
                fp(seed),
                if (index == 0) "GENESIS" else "chain-" + seeds[index - 1],
                "chain-" + seed
            )
        }

    private fun node(fp: String, previous: String, hash: String) =
        AmarProvenanceNode(
            sequence = if (previous == "GENESIS") 0 else 1,
            sourceUri = "https://example.com/evidence",
            evidenceFingerprint = fp,
            retrievedAtEpochMs = 1L,
            previousHash = previous,
            chainHash = hash
        )

    private fun fp(seed: String): String = seed.repeat(64).take(64)
}
