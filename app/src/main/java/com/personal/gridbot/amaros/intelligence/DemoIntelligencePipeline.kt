package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataProvider
import com.personal.gridbot.amaros.data.AmarDataSnapshot

/** B5 -> B6 bridge. Produces analysis only; it cannot place, modify, or close trades. */
class DemoIntelligencePipeline(
    private val provider: AmarDataProvider,
    private val analyzer: MarketAnalyzer = MarketAnalyzer()
) {
    data class AnalysisSnapshot(
        val data: AmarDataSnapshot,
        val context: MarketContext,
        val validation: DecisionValidation
    )

    fun evaluate(): AnalysisSnapshot {
        val data = provider.snapshot()
        val context = analyzer.analyze(data)
        return AnalysisSnapshot(data, context, DecisionValidation.forDemo())
    }
}
