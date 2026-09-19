package com.personal.gridbot.amaros.decision

import com.personal.gridbot.amaros.intelligence.DecisionEngine
import com.personal.gridbot.amaros.intelligence.MarketContext
import com.personal.gridbot.amaros.intelligence.confidence.AmarConfidenceEngine

/** Presentation contract carrying already-computed pipeline output. */
data class DecisionState(
    val context: MarketContext,
    val proposal: DecisionEngine.DecisionProposal,
    val confidence: AmarConfidenceEngine.Result
)