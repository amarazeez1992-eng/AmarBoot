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

    @Test fun blank_role_id_is_a_hard_block_and_is_not_filtered_out() = runBlocking {
        val invalid = object : AmarAnalystRole {
            override val id = ""
            override suspend fun analyze(context: AmarAnalysisContext) = AmarRoleReport("", "BUY", 0.90, AmarDecisionDirection.BUY)
        }
        val valid = object : AmarAnalystRole {
            override val id = "B"
            override suspend fun analyze(context: AmarAnalysisContext) = AmarRoleReport("B", "BUY", 0.90, AmarDecisionDirection.BUY)
        }

        val result = AmarDeliberationCoordinator().deliberate(AmarAnalysisContext("test"), listOf(invalid, valid))

        assertEquals(2, result.reports.size)
        assertTrue(result.conflicts.contains("blank_role_id"))
        assertTrue(result.conflicts.contains("blank_report_role_id"))
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
        assertTrue(result.deliberation.conflicts.contains("insufficient_independent_evidence"))
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

    @Test fun unknown_evidence_does_not_count_as_hold_support() = runBlocking {
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext) = AmarAgentResponse("HOLD")
        }
        val evidence = listOf(
            ResearchFinding("unknown", "https://a.example", "unknown", authority = Authority.PRIMARY, stance = EvidenceStance.UNKNOWN),
            ResearchFinding("unknown2", "https://b.example", "unknown2", authority = Authority.OFFICIAL, stance = EvidenceStance.UNKNOWN)
        )

        val result = AmarStageTwoEngine(provider).deliberate("تحليل", evidence)

        assertEquals(AmarDecisionDirection.HOLD, result.chosenDirection)
        assertEquals(0.0, result.confidence, 0.0)
        assertTrue(result.deliberation.conflicts.contains("insufficient_role_confidence"))
        assertFalse(result.approvedForSimulation)
    }

    @Test fun same_direction_high_confidence_requires_independent_mixed_evidence() = runBlocking {
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext) = AmarAgentResponse("HOLD")
        }
        val evidence = listOf(
            ResearchFinding("mixed", "https://a.example", "mixed", authority = Authority.PRIMARY, stance = EvidenceStance.MIXED),
            ResearchFinding("mixed2", "https://b.example", "mixed2", authority = Authority.OFFICIAL, stance = EvidenceStance.MIXED)
        )

        val result = AmarStageTwoEngine(provider).deliberate("تحليل", evidence)

        assertEquals(AmarDecisionDirection.HOLD, result.chosenDirection)
        assertTrue(result.approvedForSimulation)
        assertFalse(result.executionAllowed)
        assertFalse(result.brokerAccessAllowed)
    }

    @Test fun blank_fingerprints_do_not_collapse_distinct_evidence() = runBlocking {
        val provider = object : AmarReasoningProvider {
            override suspend fun respond(context: AmarAgentContext) = AmarAgentResponse("BUY")
        }
        val evidence = listOf(
            ResearchFinding("one", "https://a.example", "one", fingerprint = "", authority = Authority.PRIMARY, stance = EvidenceStance.SUPPORTS),
            ResearchFinding("two", "https://b.example", "two", fingerprint = "", authority = Authority.OFFICIAL, stance = EvidenceStance.SUPPORTS)
        )

        val result = AmarStageTwoEngine(provider).deliberate("تحليل", evidence)

        assertEquals(2, result.deliberation.reports.first().supportingEvidence.size)
        assertTrue(result.approvedForSimulation)
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
