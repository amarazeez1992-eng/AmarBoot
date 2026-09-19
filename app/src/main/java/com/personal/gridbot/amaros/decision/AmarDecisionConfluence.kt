package com.personal.gridbot.amaros.decision

import com.personal.gridbot.amaros.data.AmarDataProvider
import com.personal.gridbot.amaros.intelligence.DecisionEngine
import com.personal.gridbot.amaros.intelligence.MarketAnalyzer
import com.personal.gridbot.amaros.intelligence.MarketContext
import com.personal.gridbot.amaros.intelligence.confidence.AmarConfidenceEngine

/**
 * Single read-only composition path for the existing demo decision engines.
 * It wires analysis/fusion output into the decision proposal and confidence engine.
 * It adds no execution authority and does not replace any existing engine.
 */
class AmarDecisionConfluence(
    private val provider: AmarDataProvider,
    private val analyzer: MarketAnalyzer = MarketAnalyzer(),
    private val decisionEngine: DecisionEngine = DecisionEngine()
) {
    data class State(
        val context: MarketContext,
        val proposal: DecisionEngine.DecisionProposal,
        val confidence: AmarConfidenceEngine.Result
    )

    fun evaluate(): State {
        val data = provider.snapshot()
        val context = analyzer.analyze(data)
        val proposal = decisionEngine.evaluate(data, context)
        val evidence = context.evidence
        val completeness = (evidence.size / 4.0).coerceIn(0.0, 1.0)
        val agreement = if (evidence.isEmpty()) 0.0 else {
            val deviation = evidence.map { kotlin.math.abs(it.score - context.directionalScore) }.average()
            (1.0 - deviation / 2.0).coerceIn(0.0, 1.0)
        }
        val sourceReliability = if (evidence.isEmpty()) 0.0 else evidence.map { it.confidence }.average()
        val confidence = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(
                quality = context.confidence,
                completeness = completeness,
                freshness = 1.0,
                agreement = agreement,
                sourceReliability = sourceReliability
            )
        )
        return State(context, proposal, confidence)
    }
}
