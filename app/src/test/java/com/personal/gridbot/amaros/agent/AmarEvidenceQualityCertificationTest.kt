package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class AmarEvidenceQualityCertificationTest {
    private val certification = AmarEvidenceQualityCertification()

    private fun input(
        evidenceIntakeValid: Boolean = true,
        sourceQualityVerified: Boolean = true,
        authorityVerified: Boolean = true,
        freshnessVerified: Boolean = true,
        sourceIndependenceVerified: Boolean = true,
        duplicateFree: Boolean = true,
        fingerprintIntegrityVerified: Boolean = true,
        tamperingFree: Boolean = true,
        uniquenessVerified: Boolean = true,
        authorityScore: Double = 1.0,
        freshnessScore: Double = 1.0
    ) = AmarEvidenceQualityCertificationInput(
        evidenceIntakeValid,
        sourceQualityVerified,
        authorityVerified,
        freshnessVerified,
        sourceIndependenceVerified,
        duplicateFree,
        fingerprintIntegrityVerified,
        tamperingFree,
        uniquenessVerified,
        authorityScore,
        freshnessScore
    )

    @Test
    fun every_upstream_gate_is_required() {
        val cases = listOf(
            input(evidenceIntakeValid = false),
            input(sourceQualityVerified = false),
            input(authorityVerified = false),
            input(freshnessVerified = false),
            input(sourceIndependenceVerified = false),
            input(duplicateFree = false),
            input(fingerprintIntegrityVerified = false),
            input(tamperingFree = false),
            input(uniquenessVerified = false)
        )
        cases.forEach { assertEquals(0.0, certification.certify(it), 0.0) }
    }

    @Test
    fun finite_audit_fields_are_required_but_not_weighted() {
        assertEquals(1.0, certification.certify(input(authorityScore = 0.0, freshnessScore = 0.0)), 0.0)
        assertEquals(0.0, certification.certify(input(authorityScore = Double.NaN)), 0.0)
        assertEquals(0.0, certification.certify(input(freshnessScore = Double.POSITIVE_INFINITY)), 0.0)
        assertEquals(0.0, certification.certify(input(authorityScore = -0.1)), 0.0)
        assertEquals(0.0, certification.certify(input(freshnessScore = 1.1)), 0.0)
    }

    @Test
    fun aggregate_is_non_empty_and_all_pass_only() {
        val valid = input()
        val invalid = input(tamperingFree = false)
        assertEquals(1.0, certification.certifyAll(listOf(valid)), 0.0)
        assertEquals(0.0, certification.certifyAll(listOf(valid, invalid)), 0.0)
        assertEquals(0.0, certification.certifyAll(emptyList()), 0.0)
    }

    @Test
    fun repeated_identical_inputs_are_deterministic_and_binary() {
        val valid = input()
        repeat(100) {
            assertEquals(1.0, certification.certify(valid), 0.0)
            assertEquals(0.0, certification.certify(input(uniquenessVerified = false)), 0.0)
        }
    }
}
