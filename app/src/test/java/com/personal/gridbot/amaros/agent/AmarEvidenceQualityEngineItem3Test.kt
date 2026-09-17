package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceQualityEngineItem3Test {
    private val now = 1_000_000L
    private val engine = AmarEvidenceQualityEngine()

    @Test
    fun strong_unique_official_evidence_is_verified() {
        val finding = finding(
            uri = "https://example.com/primary",
            evidence = "Gold futures market structure remains supported by the reported data.",
            authority = Authority.OFFICIAL
        )
        val report = engine.assess(listOf(finding), now)
        assertEquals(AmarEvidenceQualityStatus.VERIFIED, report.status)
        assertTrue(report.score >= 0.70)
        assertEquals(1, report.independentSourceCount)
        assertEquals(0, report.duplicateEvidenceCount)
    }

    @Test
    fun duplicate_evidence_is_detected_and_ranked_deterministically() {
        val first = finding("https://a.example/item", "The same market evidence is published here.", Authority.REPUTABLE)
        val duplicate = finding("https://b.example/item", "The same market evidence is published here.", Authority.OFFICIAL)
        val report = engine.assess(listOf(first, duplicate), now)
        val ranked1 = engine.rank(listOf(first, duplicate), now)
        val ranked2 = engine.rank(listOf(first, duplicate), now)
        assertEquals(1, report.duplicateEvidenceCount)
        assertEquals(ranked1.map { it.fingerprint }, ranked2.map { it.fingerprint })
    }

    @Test
    fun fingerprint_tampering_fails_closed() {
        val valid = finding("https://example.com/tampered", "Evidence that must remain intact.", Authority.OFFICIAL)
        val tampered = valid.copy(fingerprint = "invalid")
        val report = engine.assess(listOf(valid, tampered), now)
        assertEquals(AmarEvidenceQualityStatus.UNVERIFIABLE, report.status)
        assertTrue(report.items.any { !it.integrityValid })
    }

    @Test
    fun stale_evidence_degrades_quality() {
        val stale = finding(
            uri = "https://example.com/stale",
            evidence = "Old evidence should lose freshness as time passes.",
            authority = Authority.OFFICIAL,
            retrievedAt = now - 10 * AmarEvidencePolicy.DEFAULT.freshnessHalfLifeMs
        )
        val report = engine.assess(listOf(stale), now)
        assertTrue(report.status != AmarEvidenceQualityStatus.VERIFIED)
        assertTrue(report.items.single().freshnessScore < 0.01)
    }

    @Test
    fun policy_is_versioned_and_validates_weights() {
        assertEquals(1, AmarEvidencePolicy.DEFAULT.version)
        assertTrue(runCatching {
            AmarEvidencePolicy(authorityWeight = .5, freshnessWeight = .5, independenceWeight = .1, uniquenessWeight = .1)
        }.isFailure)
    }

    @Test
    fun future_evidence_is_rejected() {
        val future = finding("https://example.com/future", "Future evidence is not valid.", Authority.OFFICIAL, now + 1)
        assertTrue(runCatching { engine.assess(listOf(future), now) }.isFailure)
    }

    private fun finding(
        uri: String,
        evidence: String,
        authority: Authority,
        retrievedAt: Long = now
    ): ResearchFinding = ResearchFinding(
        sourceTitle = "Test source",
        sourceUri = uri,
        evidence = evidence,
        authority = authority,
        retrievedAtEpochMs = retrievedAt,
        fingerprint = AmarEvidence.fingerprintOf("$uri|$evidence")
    )
}
