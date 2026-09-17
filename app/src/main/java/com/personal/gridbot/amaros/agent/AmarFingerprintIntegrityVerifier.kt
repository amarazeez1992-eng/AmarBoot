package com.personal.gridbot.amaros.agent

/** Point 7 boundary: verifies that each supplied fingerprint matches its source URI and evidence payload. */
class AmarFingerprintIntegrityVerifier {
    fun verify(findings: List<ResearchFinding>): AmarFingerprintIntegrityReport {
        val results = findings.mapIndexed { index, finding ->
            val expected = if (finding.sourceUri.isNotBlank() && finding.evidence.isNotBlank()) {
                AmarEvidence.fingerprintOf("${finding.sourceUri}|${finding.evidence}")
            } else {
                ""
            }
            val present = finding.fingerprint.isNotBlank()
            val matches = present && expected.isNotBlank() && finding.fingerprint == expected
            AmarFingerprintIntegrityItem(index, present, matches, expected)
        }
        return AmarFingerprintIntegrityReport(
            totalFindings = findings.size,
            validFindings = results.count { it.valid },
            invalidFindings = results.count { !it.valid },
            items = results
        )
    }
}

data class AmarFingerprintIntegrityItem(
    val index: Int,
    val fingerprintPresent: Boolean,
    val fingerprintMatchesContent: Boolean,
    val expectedFingerprint: String
) {
    val valid: Boolean get() = fingerprintPresent && fingerprintMatchesContent
}

data class AmarFingerprintIntegrityReport(
    val totalFindings: Int,
    val validFindings: Int,
    val invalidFindings: Int,
    val items: List<AmarFingerprintIntegrityItem>
) {
    val intact: Boolean get() = invalidFindings == 0
}
