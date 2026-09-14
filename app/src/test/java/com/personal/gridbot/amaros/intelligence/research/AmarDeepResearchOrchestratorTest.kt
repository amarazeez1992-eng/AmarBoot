package com.personal.gridbot.amaros.intelligence.research

import com.personal.gridbot.amaros.agent.AmarResearchEngine
import com.personal.gridbot.amaros.agent.AmarSourceType
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.ResearchReport
import com.personal.gridbot.amaros.agent.ResearchRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarDeepResearchOrchestratorTest {
    private class FakeResearchEngine : AmarResearchEngine {
        override suspend fun research(request: ResearchRequest): ResearchReport {
            delay(2L)
            return ResearchReport(
                findings = listOf(
                    researchFinding("https://official.example/${request.question}", "Official evidence for ${request.question}", Authority.OFFICIAL),
                    researchFinding("https://paper.example/${request.question}", "Peer reviewed evidence for ${request.question}", Authority.PEER_REVIEWED),
                    researchFinding("https://official.example/${request.question}", "Official evidence for ${request.question}", Authority.OFFICIAL)
                )
            )
        }
    }

    @Test
    fun parallel_research_is_bounded_ranked_and_deduplicated() = runBlocking {
        val orchestrator = AmarDeepResearchOrchestrator(
            FakeResearchEngine(),
            policy = AmarDeepResearchPolicy(
                maxQuestions = 4,
                maxParallel = 2,
                timeoutMs = 2_000L,
                retries = 0,
                maxSourcesPerQuestion = 4,
                targetIndependentSources = 2,
                maxTotalFindings = 20
            )
        )

        val report = orchestrator.research(listOf("gold", "risk"), nowEpochMs = 10_000L)

        assertEquals(2, report.tasks.size)
        assertEquals(4, report.rankedFindings.size)
        assertEquals(2, report.independentSources.size)
        assertEquals(2, report.rankedFindings.count { it.authority == Authority.OFFICIAL })
        assertTrue(report.confidence in 0.0..1.0)
    }

    @Test
    fun invalid_sources_are_filtered_before_global_verification() = runBlocking {
        val engine = object : AmarResearchEngine {
            override suspend fun research(request: ResearchRequest) = ResearchReport(
                findings = listOf(
                    researchFinding("", "invalid", Authority.UNKNOWN),
                    researchFinding("https://valid.example/${request.question}", "valid evidence", Authority.REPUTABLE)
                )
            )
        }
        val report = AmarDeepResearchOrchestrator(
            engine,
            policy = AmarDeepResearchPolicy(
                maxQuestions = 2,
                maxParallel = 1,
                retries = 0,
                targetIndependentSources = 1,
                maxTotalFindings = 10
            )
        ).research(listOf("test"), nowEpochMs = 10_000L)

        assertEquals(1, report.rankedFindings.size)
        assertEquals("valid evidence", report.rankedFindings.single().evidence)
        assertEquals(1, report.verification.usableEvidenceCount)
        assertTrue(report.verification.sourceRegistry.independentHosts.contains("valid.example"))
    }

    @Test
    fun conflicting_stances_are_preserved_and_conflict_is_not_duplicated() = runBlocking {
        val engine = object : AmarResearchEngine {
            override suspend fun research(request: ResearchRequest) = ResearchReport(
                findings = listOf(
                    researchFinding("https://a.example/${request.question}", "supporting evidence", Authority.PRIMARY, EvidenceStance.SUPPORTS),
                    researchFinding("https://b.example/${request.question}", "opposing evidence", Authority.PRIMARY, EvidenceStance.OPPOSES)
                )
            )
        }
        val report = AmarDeepResearchOrchestrator(
            engine,
            policy = AmarDeepResearchPolicy(maxQuestions = 1, maxParallel = 1, retries = 0, targetIndependentSources = 2)
        ).research(listOf("conflict"), nowEpochMs = 10_000L)

        assertEquals(1, report.conflicts.size)
        assertTrue(report.verification.conflicts.isNotEmpty())
        assertTrue(report.confidence < 0.8)
    }
}

private fun researchFinding(
    uri: String,
    evidence: String,
    authority: Authority,
    stance: EvidenceStance = EvidenceStance.SUPPORTS
): ResearchFinding = ResearchFinding(
    sourceTitle = uri,
    sourceUri = uri,
    evidence = evidence,
    authority = authority,
    stance = stance,
    sourceType = AmarSourceType.RESEARCH,
    retrievedAtEpochMs = 9_000L
)
