package com.personal.gridbot.amaros.intelligence.research

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarResearchEngineAuthorityTest {

    private fun provider(vararg items: AmarResearchResult) = object : AmarResearchProvider {
        override suspend fun search(query: String) = items.toList()
    }

    private fun result(host: String, title: String, snippet: String = "snippet content here for testing") =
        AmarResearchResult("q", host, title, snippet, 1_000L)

    private fun engine(vararg items: AmarResearchResult): AmarResearchEngineAuthority {
        val p = provider(*items)
        return AmarResearchEngineAuthority(p) { 1_000L }
    }

    @Test fun understand_extractsKeywordsAndIntent() {
        val e = engine()
        val q = e.understand("Compare gold vs silver")
        assertEquals(AmarResearchEngineAuthority.Intent.COMPARATIVE, q.intent)
        assertTrue(q.keywords.contains("gold"))
    }

    @Test fun understand_verificationIntent() {
        val e = engine()
        assertEquals(AmarResearchEngineAuthority.Intent.VERIFICATION, e.understand("verify the gold price").intent)
    }

    @Test fun decompose_shortQueryReturnsSingle() {
        val e = engine()
        val q = e.understand("gold price")
        assertEquals(1, e.decompose(q).size)
    }

    @Test fun decompose_longQueryReturnsMultiple() {
        val e = engine()
        val q = e.understand("gold silver platinum copper analysis market")
        assertTrue(e.decompose(q).size >= 2)
    }

    @Test fun strategy_factualIsSingleRound() {
        val e = engine()
        val q = e.understand("price of gold")
        assertEquals(1, e.strategyFor(q).maxRounds)
    }

    @Test fun strategy_exploratoryIsMultiRound() {
        val e = engine()
        val q = e.understand("explore why markets move")
        assertTrue(e.strategyFor(q).maxRounds >= 3)
    }

    @Test fun run_stopsWhenConditionsMet() = runBlocking {
        val e = engine(
            result("a.com", "Gold price up"),
            result("b.com", "Silver flat")
        )
        val report = e.run("gold silver", AmarResearchEngineAuthority.StoppingCondition(2, 2, 2))
        assertEquals("conditions_met", report.audit.stopReason)
    }

    @Test fun run_recordsAudit() = runBlocking {
        val e = engine(result("a.com", "Gold price up"))
        val report = e.run("gold")
        assertEquals(1, report.audit.rounds)
        assertTrue(report.audit.endedAtEpochMs >= report.audit.startedAtEpochMs)
    }

    @Test fun run_emptyProviderReturnsEmpty() = runBlocking {
        val e = engine()
        val report = e.run("nothing", AmarResearchEngineAuthority.StoppingCondition(2, 1, 1))
        assertEquals(0, report.results.size)
    }

    @Test fun understand_rejectsBlank() {
        val e = engine()
        assertTrue(runCatching { e.understand("  ") }.isFailure)
    }
}
