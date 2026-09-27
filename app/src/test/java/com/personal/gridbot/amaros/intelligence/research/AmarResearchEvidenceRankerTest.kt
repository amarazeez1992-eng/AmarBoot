package com.personal.gridbot.amaros.intelligence.research

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarResearchEvidenceRankerTest {

    private fun result(host: String, title: String, snippet: String = "content about gold market analysis", time: Long = 1_000L) =
        AmarResearchResult("q", host, title, snippet, time)

    private fun ranker() = AmarResearchEvidenceRanker { 1_000L }

    @Test fun rank_emptyInputReturnsEmpty() {
        val r = ranker().rank(emptyList(), "gold")
        assertEquals(0, r.ranked.size)
        assertEquals("empty_input", r.explanation)
    }

    @Test fun rank_relevanceAffectsScore() {
        val high = result("a.com", "Gold price analysis", "gold gold gold")
        val low = result("b.com", "Weather report", "sunny outside")
        val ranked = ranker().rank(listOf(high, low), "gold").ranked
        assertEquals("a.com", ranked.first().result.sourceHost)
    }

    @Test fun rank_authorityHostsAreBoosted() {
        val a = result("authority.com", "Gold analysis", "gold market")
        val b = result("random.com", "Gold analysis", "gold market")
        val ranked = ranker().rank(listOf(a, b), "gold", authorityHosts = setOf("authority.com")).ranked
        assertEquals("authority.com", ranked.first().result.sourceHost)
    }

    @Test fun rank_removesDuplicates() {
        val a = result("a.com", "Gold up")
        val b = result("a.com", "Gold up")
        val report = ranker().rank(listOf(a, b), "gold")
        assertEquals(1, report.duplicatesRemoved)
        assertEquals(1, report.ranked.size)
    }

    @Test fun rank_detectsConflicts() {
        val a = result("a.com", "Gold analysis", "gold rose sharply today")
        val b = result("b.com", "Gold analysis", "gold rose sharply yesterday")
        val report = ranker().rank(listOf(a, b), "gold")
        assertTrue(report.conflictsDetected >= 1)
    }

    @Test fun rank_freshnessAffectsScore() {
        val fresh = result("a.com", "Gold analysis", "gold market", time = 1_000L)
        val stale = result("b.com", "Gold analysis", "gold market", time = -100_000_000L)
        val ranked = ranker().rank(listOf(fresh, stale), "gold").ranked
        assertEquals("a.com", ranked.first().result.sourceHost)
    }

    @Test fun rank_completenessAffectsScore() {
        val complete = result("a.com", "Gold analysis", "gold market has moved sharply this week")
        val short = result("b.com", "Gold analysis", "x")
        val ranked = ranker().rank(listOf(complete, short), "gold").ranked
        assertEquals("a.com", ranked.first().result.sourceHost)
    }

    @Test fun rank_explanationIsPopulated() {
        val report = ranker().rank(listOf(result("a.com", "Gold up")), "gold")
        assertTrue(report.explanation.contains("ranked="))
    }

    @Test fun rank_scoresAreBounded() {
        val report = ranker().rank(listOf(result("a.com", "Gold up")), "gold")
        assertTrue(report.ranked.all { it.score in 0.0..1.0 })
    }

    @Test fun rank_reasonsArePopulated() {
        val report = ranker().rank(listOf(result("a.com", "Gold market analysis")), "gold")
        assertTrue(report.ranked.first().reasons.isNotEmpty())
    }
}
