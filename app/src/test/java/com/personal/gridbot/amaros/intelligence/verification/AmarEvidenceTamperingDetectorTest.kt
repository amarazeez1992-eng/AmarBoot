package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarEvidence
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.ResearchFinding
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceTamperingDetectorTest {
    private val detector = AmarEvidenceTamperingDetector()

    @Test
    fun unchanged_recorded_chain_is_intact() {
        val findings = listOf(finding("https://a.example/e", "Gold rises", 100L))
        assertTrue(detector.detect(findings, AmarProvenanceChain.build(findings)).intact)
    }

    @Test
    fun changed_source_is_detected_as_tampering() {
        val original = finding("https://a.example/e", "Gold rises", 100L)
        val changed = original.copy(sourceUri = "https://tampered.example/e")
        assertFalse(detector.detect(listOf(changed), AmarProvenanceChain.build(listOf(original))).intact)
    }

    @Test
    fun changed_evidence_is_detected_as_tampering() {
        val original = finding("https://a.example/e", "Gold rises", 100L)
        val changed = original.copy(evidence = "Gold falls")
        assertFalse(detector.detect(listOf(changed), AmarProvenanceChain.build(listOf(original))).intact)
    }

    @Test
    fun changed_retrieval_time_is_detected_as_tampering() {
        val original = finding("https://a.example/e", "Gold rises", 100L)
        val changed = original.copy(retrievedAtEpochMs = 101L)
        assertFalse(detector.detect(listOf(changed), AmarProvenanceChain.build(listOf(original))).intact)
    }

    @Test
    fun changed_recorded_chain_hash_is_detected() {
        val findings = listOf(finding("https://a.example/e", "Gold rises", 100L))
        val chain = AmarProvenanceChain.build(findings)
        val tamperedNode = chain.single().copy(chainHash = "tampered")
        assertFalse(detector.detect(findings, listOf(tamperedNode)).intact)
    }

    @Test
    fun chain_length_mutation_is_detected() {
        val findings = listOf(
            finding("https://a.example/e", "Gold rises", 100L),
            finding("https://b.example/e", "Gold stable", 101L)
        )
        val chain = AmarProvenanceChain.build(findings)
        assertFalse(detector.detect(findings.drop(1), chain).intact)
    }

    @Test
    fun detection_is_deterministic() {
        val findings = listOf(finding("https://a.example/e", "Gold rises", 100L))
        val chain = AmarProvenanceChain.build(findings)
        assertTrue(detector.detect(findings, chain) == detector.detect(findings, chain))
    }

    private fun finding(uri: String, evidence: String, time: Long) = ResearchFinding(
        sourceTitle = uri,
        sourceUri = uri,
        evidence = evidence,
        authority = Authority.PRIMARY,
        retrievedAtEpochMs = time,
        fingerprint = AmarEvidence.fingerprintOf("$uri|$evidence")
    )
}
