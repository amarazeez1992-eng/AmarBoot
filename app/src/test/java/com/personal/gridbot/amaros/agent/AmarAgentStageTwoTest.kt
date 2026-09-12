package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAgentStageTwoTest {
    @Test fun coordinator_detects_direction_conflict_and_never_approves_it() = runBlocking {
        val roleA = object : AmarAnalystRole {
            override val id = "A"
            override suspend fun analyze(context: AmarAnalysisContext) = AmarRoleReport("A", "BUY", 0.90, AmarDecisionDirection.BUY)
        }
        val roleB = object : AmarAnalystRole {
            override val id = "B"
            override suspend fun analyze(context: AmarAnalysisContext) = AmarRoleReport("B", "SELL", 0.90, AmarDecisionDirection.SELL)
        }

        val result = AmarDeliberationCoordinator().deliberate(AmarAnalysisContext("test"), listOf(roleA, roleB))

        assertEquals(2, result.reports.size)
        assertTrue(result.conflicts.contains("direction_conflict"))
        assertEquals(AmarDecisionDirection.UNKNOWN, result.consensusDirection)
        assertFalse(result.approvedForSimulation)
    }

    @Test fun duplicate_role_ids_are_reported_instead_of_silently_dropped() = runBlocking {
        val roleA = object : AmarAnalystRole {
            override val id = "A"
            override suspend fun analyze(context: AmarAnalysisContext) = AmarRoleReport("A", "BUY", 0.90, AmarDecisionDirection.BUY)
        }
        val duplicate = object : AmarAnalystRole {
            override val id = "A"
            override suspend fun analyze(context: AmarAnalysisContext) = AmarRoleReport("A", "BUY", 0.90, AmarDecisionDirection.BUY)
        }

        val result = AmarDeliberationCoordinator().deliberate(AmarAnalysisContext("test"), listOf(roleA, duplicate))

        assertTrue(result.conflicts.contains("duplicate_role_ids:A"))
        assertFalse(result.approvedForSimulation)
    }

    @Test fun roles_never_receive_execution_authority() = runBlocking {
        val calls = mutableListOf<String>()
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
                calls += context.userText.substringBefore('\n')
                assertFalse(context.executionAllowed)
                assertFalse(context.brokerAccessAllowed)
                return AmarAgentResponse("HOLD: لا توجد إشارة قابلة للاعتماد")
            }
        }

        val result = AmarStageTwoEngine(provider).deliberate("تحليل", emptyList())

        assertEquals(4, calls.size)
        assertEquals(4, result.deliberation.reports.size)
        assertEquals(AmarDecisionDirection.HOLD, result.chosenDirection)
        assertFalse(result.approvedForSimulation)
        assertFalse(result.executionAllowed)
        assertFalse(result.brokerAccessAllowed)
    }

    @Test fun opposing_evidence_reduces_confidence_and_blocks_approval() = runBlocking {
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext) = AmarAgentResponse("BUY مع تحفظ")
        }
        val evidence = listOf(
            ResearchFinding("support", "https://a.example", "support", authority = Authority.OFFICIAL, stance = EvidenceStance.SUPPORTS),
            ResearchFinding("oppose", "https://b.example", "oppose", authority = Authority.REPUTABLE, stance = EvidenceStance.OPPOSES)
        )

        val result = AmarStageTwoEngine(provider).deliberate("تحليل", evidence)

        assertEquals(4, result.deliberation.reports.size)
        assertTrue(result.confidence < 0.80)
        assertFalse(result.approvedForSimulation)
    }

    @Test fun same_direction_high_confidence_is_approved_for_simulation_only() = runBlocking {
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext) = AmarAgentResponse("HOLD")
        }
        val evidence = listOf(
            ResearchFinding("mixed", "https://a.example", "mixed", authority = Authority.PRIMARY, stance = EvidenceStance.MIXED),
            ResearchFinding("unknown", "https://b.example", "unknown", authority = Authority.OFFICIAL, stance = EvidenceStance.UNKNOWN)
        )

        val result = AmarStageTwoEngine(provider).deliberate("تحليل", evidence)

        assertEquals(AmarDecisionDirection.HOLD, result.chosenDirection)
        assertTrue(result.approvedForSimulation)
        assertFalse(result.executionAllowed)
        assertFalse(result.brokerAccessAllowed)
    }

    @Test fun unknown_direction_is_a_hard_block() = runBlocking {
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext) = AmarAgentResponse("البيانات غير كافية")
        }

        val result = AmarStageTwoEngine(provider).deliberate("تحليل", emptyList())

        assertEquals(AmarDecisionDirection.UNKNOWN, result.chosenDirection)
        assertTrue(result.deliberation.conflicts.contains("unknown_role_direction"))
        assertFalse(result.approvedForSimulation)
    }
}
