package com.personal.gridbot.amaros.ai.core

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiResearchAuthorityTest {
    @Test
    fun independentFreshWellDocumentedEvidenceGetsHighGrade() {
        val items = listOf(
            AmarAiResearchAuthority.Evidence("SEC", "Market structure", "https://www.sec.gov/example", "Official methodology and documented evidence with sufficient detail.", methodology = "official report"),
            AmarAiResearchAuthority.Evidence("BIS", "Market structure", "https://www.bis.org/example", "Independent official research with documented methodology and evidence.", methodology = "official report"),
            AmarAiResearchAuthority.Evidence("CME", "Market structure", "https://www.cmegroup.com/example", "Exchange source with documented methodology and evidence.", methodology = "official report")
        )
        val report = AmarAiResearchAuthority.evaluate(items)
        assertTrue(report.independentChannels >= 3)
        assertTrue(report.provenancePct >= 80.0)
        assertTrue(report.confidencePct >= 70.0)
    }

    @Test
    fun emptyEvidenceIsLow() {
        val report = AmarAiResearchAuthority.evaluate(emptyList())
        assertTrue(report.grade == AmarAiResearchAuthority.Grade.LOW)
        assertTrue(report.confidencePct == 0.0)
    }
}
