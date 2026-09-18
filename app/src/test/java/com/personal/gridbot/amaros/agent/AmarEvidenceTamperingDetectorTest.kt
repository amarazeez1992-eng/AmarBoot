package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceChain
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceTamperingDetectorTest {
    private val detector = AmarEvidenceTamperingDetector()

    @Test
    fun unchanged_recorded_chain_is_intact() {
        val findings = listOf(finding("https://a.example/e", "Gold rises", 100L))
        val chain = AmarProvenanceChain.build(findings)
        assertTrue(detector.detect(findings, chain).intact)
    }

    @Test
    fun changed_source_is_detected_as_tampering() {
        val original = finding("https://a.example/e", "Gold rises", 100L)
        val chain = AmarProvenanceChain.build(listOf(original))
        val changed = original.copy(sourceUri = "https://tampered.example/e")
        assertFalse(detector.detect(listOf(changed), chain).intact)
    }

    @Test
    fun changed_retrieval_time_is_detected_as_tampering() {
        val original = finding("https://a.example/e", "Gold rises", 100L)
        val chain = AmarProvenanceChain.build(listOf(original))
        val changed = original.copy(retrievedAtEpochMs = 101L)
        assertFalse(detector.detect(listOf(changed), chain).intact)
    }

    @Test
    fun truncated_or_extended_chain_is_detected() {
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
