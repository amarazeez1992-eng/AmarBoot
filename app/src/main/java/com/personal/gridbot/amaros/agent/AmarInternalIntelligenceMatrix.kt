package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Single pre-reasoning intelligence matrix; never a second Agent or final-answer generator. */
class AmarInternalIntelligenceMatrix(
    private val planner: AmarAgentPlanner = AmarAgentPlanner(),
    private val knowledge: AmarKnowledgeEngine = AmarKnowledgeEngine()
) {
    data class Report(
        val intent: AgentIntent,
        val understanding: UnderstandingSignal,
        val knowledge: KnowledgeSignal,
        val discovery: DiscoverySignal,
        val analysis: AnalysisSignal,
        val inspection: InspectionSignal,
        val reasoning: ReasoningSignal,
        val speed: SpeedSignal
    ) {
        fun asContext(): String = buildString {
            appendLine("AMAR internal intelligence matrix:")
            appendLine("intent=${intent}")
            appendLine("understanding=${understanding.summary}")
            appendLine("knowledge=matches:${knowledge.matches}")
            appendLine("discovery=tools:${discovery.toolCount};research:${discovery.researchCapable}")
            appendLine("analysis=complexity:${analysis.complexity};signals:${analysis.signals.joinToString("|")}")
            appendLine("inspection=valid:${inspection.valid};issues:${inspection.issues.joinToString("|")}")
            appendLine("reasoning=ready:${reasoning.ready};evidenceNeeded:${reasoning.evidenceNeeded}")
            appendLine("speed=matrixMs:${speed.elapsedMs}")
        }
    }
    data class UnderstandingSignal(val tokenCount: Int, val questionCount: Int, val summary: String)
    data class KnowledgeSignal(val matches: Int)
    data class DiscoverySignal(val toolCount: Int, val researchCapable: Boolean)
    data class AnalysisSignal(val complexity: String, val signals: List<String>)
    data class InspectionSignal(val valid: Boolean, val issues: List<String>)
    data class ReasoningSignal(val ready: Boolean, val evidenceNeeded: Boolean)
    data class SpeedSignal(val elapsedMs: Long)

    suspend fun evaluate(request: AmarAgentRequest, tools: List<AmarAgentTool>, plan: AmarAgentPlan): Report = coroutineScope {
        val started = System.nanoTime()
        val text = request.text.trim()
        val jobs = listOf(
            async(Dispatchers.Default) { "intent" to plan.intent },
            async(Dispatchers.Default) { "understanding" to understand(text) },
            async(Dispatchers.Default) { "knowledge" to KnowledgeSignal(knowledge.search(text).size) },
            async(Dispatchers.Default) { "discovery" to DiscoverySignal(tools.size, tools.any { it.scope == AmarToolScope.RESEARCH }) },
            async(Dispatchers.Default) { "analysis" to analyze(text) },
            async(Dispatchers.Default) { "inspection" to inspect(text, request) },
            async(Dispatchers.Default) { "reasoning" to reasoningSignal(text, request) }
        )
        val results = jobs.awaitAll().toMap()
        Report(
            intent = results["intent"] as AgentIntent,
            understanding = results["understanding"] as UnderstandingSignal,
            knowledge = results["knowledge"] as KnowledgeSignal,
            discovery = results["discovery"] as DiscoverySignal,
            analysis = results["analysis"] as AnalysisSignal,
            inspection = results["inspection"] as InspectionSignal,
            reasoning = results["reasoning"] as ReasoningSignal,
            speed = SpeedSignal((System.nanoTime() - started) / 1_000_000L)
        )
    }

    private fun understand(text: String): UnderstandingSignal {
        val tokens = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val questions = text.count { it == "?"[0] || it == "؟"[0] }
        val summary = when {
            tokens.isEmpty() -> "empty_request"
            questions > 0 -> "question:${tokens.size}tokens"
            tokens.size > 24 -> "multi_part:${tokens.size}tokens"
            else -> "statement:${tokens.size}tokens"
        }
        return UnderstandingSignal(tokens.size, questions, summary)
    }

    private fun analyze(text: String): AnalysisSignal {
        val lower = text.lowercase()
        val signals = buildList {
            if (text.contains(Regex("\\b[A-Z]{3,6}[/_-][A-Z]{3,6}\\b"))) add("instrument")
            if (listOf("الآن", "today", "now", "اليوم").any { lower.contains(it) }) add("time_sensitive")
            if (listOf("لماذا", "why", "سبب").any { lower.contains(it) }) add("causal")
            if (listOf("قارن", "compare").any { lower.contains(it) }) add("comparison")
            if (listOf("تحليل", "analy").any { lower.contains(it) }) add("analysis")
            if (listOf("مصدر", "خبر", "research").any { lower.contains(it) }) add("evidence")
        }
        val complexity = when { signals.size >= 4 || text.length > 320 -> "high"; signals.size >= 2 || text.length > 120 -> "medium"; else -> "low" }
        return AnalysisSignal(complexity, signals)
    }

    private fun inspect(text: String, request: AmarAgentRequest): InspectionSignal {
        val issues = buildList {
            if (text.isBlank()) add("empty_request")
            if (text.length > 12_000) add("request_too_large")
            if (request.requestedSourceCount < 1) add("invalid_source_budget")
            if (request.maximumSourceCount < request.requestedSourceCount) add("source_budget_inconsistent")
        }
        return InspectionSignal(issues.isEmpty(), issues)
    }

    private fun reasoningSignal(text: String, request: AmarAgentRequest): ReasoningSignal {
        val timeSensitive = listOf("الآن", "now", "today", "اليوم").any { text.contains(it, true) }
        val evidenceTerms = listOf("سعر", "خبر", "مصدر", "حدث", "price", "news", "source")
        val evidenceNeeded = timeSensitive || evidenceTerms.any { text.contains(it, true) } || request.requireCrossValidation
        return ReasoningSignal(text.isNotBlank(), evidenceNeeded)
    }
}