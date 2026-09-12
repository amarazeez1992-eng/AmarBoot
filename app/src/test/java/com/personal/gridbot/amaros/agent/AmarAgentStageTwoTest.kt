package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAgentStageTwoTest {
    @Test fun coordinator_deduplicates_roles_and_exposes_conflict() = runBlocking {
        val roleA = object : AmarAnalystRole {
            override val id = "A"
            override suspend fun analyze(context: AmarAnalysisContext) = AmarRoleReport("A", "BUY", 0.90)
        }
        val roleDuplicate = object : AmarAnalystRole {
            override val id = "A"
            override suspend fun analyze(context: AmarAnalysisContext) = AmarRoleReport("A", "SELL", 0.90)
        }
        val roleB = object : AmarAnalystRole {
            override val id = "B"
            override suspend fun analyze(context: AmarAnalysisContext) = AmarRoleReport("B", "SELL", 0.90)
        }

        val result = AmarDeliberationCoordinator().deliberate(
            AmarAnalysisContext("test"),
            listOf(roleA, roleDuplicate, roleB)
        )

        assertEquals(2, result.reports.size)
        assertTrue(result.conflicts.isNotEmpty())
        assertFalse(result.approvedForSimulation)
    }

    @Test fun reasoning_roles_are_isolated_and_never_receive_execution_authority() = runBlocking {
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
        assertFalse(result.executionAllowed)
        assertFalse(result.brokerAccessAllowed)
    }

    @Test fun opposing_evidence_reduces_role_confidence() = runBlocking {
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext) = AmarAgentResponse("BUY مع تحفظ")
        }
        val evidence = listOf(
            ResearchFinding("support", "https://a.example", "support", stance = EvidenceStance.SUPPORTS),
            ResearchFinding("oppose", "https://b.example", "oppose", stance = EvidenceStance.OPPOSES)
        )

        val result = AmarStageTwoEngine(provider).deliberate("تحليل", evidence)

        assertEquals(4, result.deliberation.reports.size)
        assertTrue(result.confidence < 0.75)
        assertFalse(result.approvedForSimulation)
    }

    @Test fun same_direction_high_confidence_can_be_approved_for_simulation_only() = runBlocking {
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext) = AmarAgentResponse("HOLD")
        }

        val result = AmarStageTwoEngine(provider).deliberate("تحليل", emptyList())

        assertEquals(AmarDecisionDirection.HOLD, result.chosenDirection)
        assertTrue(result.approvedForSimulation)
        assertFalse(result.executionAllowed)
        assertFalse(result.brokerAccessAllowed)
    }
}
