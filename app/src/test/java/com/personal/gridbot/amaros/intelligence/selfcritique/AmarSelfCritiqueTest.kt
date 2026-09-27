package com.personal.gridbot.amaros.intelligence.selfcritique

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarSelfCritiqueTest {

    private fun critic() = AmarSelfCritique()

    private fun draft(
        answer: String,
        claims: List<AmarSelfCritique.Claim> = emptyList()
    ) = AmarSelfCritique.Draft(answer, claims, emptyList(), emptyList())

    private fun claim(text: String, supported: Boolean = true, evidence: List<String> = listOf("e1")) =
        AmarSelfCritique.Claim(text, supported, evidence)

    @Test fun review_emptyAnswerRejected() {
        val r = critic().review(draft(""))
        assertEquals(AmarSelfCritique.RevisionDecision.REJECT, r.decision)
    }

    @Test fun review_cleanAnswerAccepted() {
        val d = draft("Gold market analysis shows steady growth over time.",
            listOf(claim("gold is up"), claim("silver is flat")))
        val r = critic().review(d)
        assertEquals(AmarSelfCritique.RevisionDecision.ACCEPT, r.decision)
    }

    @Test fun review_unsupportedClaimsTriggersReject() {
        val d = draft("Some answer.",
            listOf(claim("x", false, emptyList()), claim("y", false, emptyList()), claim("z")))
        val r = critic().review(d)
        assertTrue(r.decision != AmarSelfCritique.RevisionDecision.ACCEPT)
    }

    @Test fun review_overclaimingTriggersRevise() {
        val d = draft("This is 100% guaranteed gold analysis.",
            listOf(claim("gold up")))
        val r = critic().review(d)
        assertEquals(AmarSelfCritique.RevisionDecision.REVISE, r.decision)
        assertTrue(r.overclaiming.isNotEmpty())
    }

    @Test fun review_contradictionDetected() {
        val d = draft("Gold is up and also not up today.",
            listOf(claim("gold is up"), claim("gold is not up")))
        val r = critic().review(d)
        assertTrue(r.contradictions.isNotEmpty())
    }

    @Test fun review_coverageComputed() {
        val d = draft("Analysis.",
            listOf(claim("a", true, listOf("e1")), claim("b", true, emptyList())))
        val r = critic().review(d)
        assertEquals(0.5, r.coverageScore, 0.01)
    }

    @Test fun review_qualityComputed() {
        val d = draft("Short.", listOf(claim("a")))
        val r = critic().review(d)
        assertTrue(r.qualityScore in 0.0..1.0)
    }

    @Test fun review_summaryPopulated() {
        val d = draft("Some good analysis content here.", listOf(claim("a")))
        val r = critic().review(d)
        assertTrue(r.summary.contains("quality="))
    }

    @Test fun review_unsupportedListed() {
        val d = draft("Some answer.",
            listOf(claim("supported", true, listOf("e1")), claim("unsupported", false, emptyList())))
        val r = critic().review(d)
        assertEquals(1, r.unsupported.size)
    }
}
