package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAgentStageOneTest {
    @Test fun budget_normalization_keeps_limits_safe() {
        val budget = AmarAgentBudget(
            maxSteps = 0,
            maxToolCalls = 999,
            maxSources = 500,
            targetIndependentSources = 900,
            maxContextTokens = 1,
            timeoutMs = 999_999L
        ).normalized()

        assertEquals(1, budget.maxSteps)
        assertEquals(500, budget.maxToolCalls)
        assertEquals(100, budget.maxSources)
        assertEquals(100, budget.targetIndependentSources)
        assertEquals(1024, budget.maxContextTokens)
        assertEquals(120_000L, budget.timeoutMs)
    }

    @Test fun direction_engine_rejects_negated_buy() {
        val engine = AmarDecisionDirectionEngine()
        assertEquals(AmarDecisionDirection.UNKNOWN, engine.detect("لا أنصح بالشراء الآن"))
        assertEquals(AmarDecisionDirection.BUY, engine.detect("BUY بعد تحقق الشروط"))
    }

    @Test fun decision_council_blocks_direction_conflict() {
        val council = AmarDecisionCouncil()
        val review = council.review(
            listOf(
                AmarAgentOpinion(AmarAgentRole.ANALYST, "buy", 0.90, AmarDecisionDirection.BUY),
                AmarAgentOpinion(AmarAgentRole.ADVISOR, "sell", 0.90, AmarDecisionDirection.SELL)
            )
        )

        assertFalse(review.approved)
        assertTrue(review.conflicts.isNotEmpty())
    }

    @Test fun role_opinions_keep_execution_disabled_and_surface_opposition() {
        val answer = AmarAgentResponse("BUY")
        val evidence = listOf(
            ResearchFinding("support", "https://example.com/a", "supports", stance = EvidenceStance.SUPPORTS),
            ResearchFinding("oppose", "https://example.com/b", "opposes", stance = EvidenceStance.OPPOSES)
        )

        val opinions = AmarRoleOpinionEngine().buildOpinions(
            answer,
            AmarDecisionDirection.BUY,
            0.90,
            evidence
        )

        assertEquals(3, opinions.size)
        assertEquals(AmarAgentRole.ANALYST, opinions[0].role)
        assertEquals(AmarAgentRole.ADVISOR, opinions[1].role)
        assertEquals(AmarAgentRole.RISK_GUARD, opinions[2].role)
        assertEquals(AmarDecisionDirection.HOLD, opinions[1].direction)
        assertEquals(AmarDecisionDirection.HOLD, opinions[2].direction)
        assertTrue(opinions.all { "execution_disabled" in it.risks })
    }

    @Test fun critic_requires_evidence_only_when_requested() {
        val critic = AmarAgentCritic()
        assertTrue(critic.review("شرح عام", emptyList(), requireEvidence = false).accepted)
        assertFalse(critic.review("تحليل تداول", emptyList(), requireEvidence = true).accepted)
    }

    @Test fun trading_tools_respect_capability_scopes_and_read_only_flags() {
        val tools = AmarTradingTools()
        val noResearch = tools.availableTools(
            AmarAgentPolicy(allowResearch = false, allowSimulation = false, allowStrategyDrafting = false)
        )

        assertTrue(noResearch.none { it.id == "market_research" })
        assertTrue(noResearch.none { it.id == "simulation" })
        assertTrue(noResearch.none { it.id == "strategy_draft" })

        val full = tools.availableTools(AmarAgentPolicy())
        assertEquals(AmarToolScope.RESEARCH, full.first { it.id == "market_research" }.scope)
        assertEquals(AmarToolScope.SIMULATION, full.first { it.id == "simulation" }.scope)
        assertEquals(AmarToolScope.STRATEGY_WRITE, full.first { it.id == "strategy_draft" }.scope)
        assertFalse(full.first { it.id == "strategy_draft" }.readOnly)
        assertTrue(full.first { it.id == "simulation" }.readOnly)
    }

    @Test fun source_verifier_requires_independent_evidence_and_ignores_invalid_findings() {
        val verifier = AmarSourceVerifier()
        val result = verifier.verify(
            listOf(
                ResearchFinding("valid-a", "https://example.com/a", "evidence-a", authority = Authority.PRIMARY),
                ResearchFinding("valid-b", "https://example.org/b", "evidence-b", authority = Authority.OFFICIAL),
                ResearchFinding("invalid", "", "ignored", authority = Authority.PRIMARY),
                ResearchFinding("invalid-2", "https://example.net/c", "", authority = Authority.PRIMARY)
            )
        )

        assertTrue(result.accepted)
        assertEquals(2, result.totalSources)
        assertEquals(2, result.independentSources)
        assertTrue(result.confidence >= 0.70)
    }
}
