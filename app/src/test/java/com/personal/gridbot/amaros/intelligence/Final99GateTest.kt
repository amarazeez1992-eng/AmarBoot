package com.personal.gridbot.amaros.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Final99GateTest {
    private val gate = Final99Gate()

    private fun completeInput(
        dimensions: List<Final99Gate.EvidenceDimension> = listOf(
            Final99Gate.EvidenceDimension("architecture", 20.0, 100.0),
            Final99Gate.EvidenceDimension("evidence", 20.0, 100.0),
            Final99Gate.EvidenceDimension("regression", 20.0, 100.0),
            Final99Gate.EvidenceDimension("safety", 20.0, 100.0),
            Final99Gate.EvidenceDimension("audit", 20.0, 100.0)
        )
    ) = Final99Gate.CertificationInput(
        dimensions = dimensions,
        mandatoryEvidenceComplete = true,
        regressionClean = true,
        buildVerified = true,
        architectureVerified = true,
        safetyBoundaryVerified = true,
        finalAuditPassed = true,
        closureChainComplete = true
    )

    @Test
    fun completeEvidenceAt100PercentCertifies() {
        val result = gate.certify(completeInput())
        assertEquals(100.0, result.scorePercent, 0.0001)
        assertTrue(result.certified)
        assertTrue(result.blockers.isEmpty())
    }

    @Test
    fun ninetyNinePercentExactlyMeetsThreshold() {
        val result = gate.certify(
            completeInput(
                listOf(
                    Final99Gate.EvidenceDimension("architecture", 100.0, 99.0)
                )
            )
        )
        assertEquals(99.0, result.scorePercent, 0.0001)
        assertTrue(result.certified)
    }

    @Test
    fun scoreBelowNinetyNineDoesNotCertify() {
        val result = gate.certify(
            completeInput(
                listOf(
                    Final99Gate.EvidenceDimension("architecture", 100.0, 98.99)
                )
            )
        )
        assertFalse(result.certified)
        assertEquals(listOf("score_below_threshold"), result.blockers)
    }

    @Test
    fun missingEvidenceOverridesPerfectScore() {
        val result = gate.certify(
            completeInput().copy(mandatoryEvidenceComplete = false)
        )
        assertFalse(result.certified)
        assertTrue("mandatory_evidence_incomplete" in result.blockers)
    }

    @Test
    fun staleEvidenceIsAHardBlocker() {
        val result = gate.certify(
            completeInput(
                listOf(
                    Final99Gate.EvidenceDimension(
                        "current-ci", 100.0, 100.0, evidenceCurrent = false
                    )
                )
            )
        )
        assertFalse(result.certified)
        assertTrue("stale_evidence:current-ci" in result.blockers)
    }

    @Test
    fun invalidEvidenceIsAHardBlocker() {
        val result = gate.certify(
            completeInput(
                listOf(
                    Final99Gate.EvidenceDimension(
                        "evidence-ledger", 100.0, 100.0, valid = false
                    )
                )
            )
        )
        assertFalse(result.certified)
        assertTrue("invalid_evidence:evidence-ledger" in result.blockers)
    }

    @Test
    fun explicitBlockerCannotBeOverriddenByScore() {
        val result = gate.certify(
            completeInput(
                listOf(
                    Final99Gate.EvidenceDimension(
                        "safety", 100.0, 100.0, blocking = true
                    )
                )
            )
        )
        assertFalse(result.certified)
        assertTrue("explicit_blocker:safety" in result.blockers)
    }

    @Test
    fun missingSafetyBoundaryBlocksCertification() {
        val result = gate.certify(
            completeInput().copy(safetyBoundaryVerified = false)
        )
        assertFalse(result.certified)
        assertTrue("safety_boundary_not_verified" in result.blockers)
    }

    @Test
    fun incompleteClosureChainBlocksCertification() {
        val result = gate.certify(
            completeInput().copy(closureChainComplete = false)
        )
        assertFalse(result.certified)
        assertTrue("closure_chain_incomplete" in result.blockers)
    }

    @Test
    fun noDimensionsFailsClosed() {
        val result = gate.certify(completeInput(dimensions = emptyList()))
        assertFalse(result.certified)
        assertTrue("no_dimensions" in result.blockers)
    }
}
