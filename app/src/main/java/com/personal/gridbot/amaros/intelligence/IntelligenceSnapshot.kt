package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataSnapshot

/** Stable, read-only output of the B6 intelligence layer. */
data class IntelligenceSnapshot(
    val sourceData: AmarDataSnapshot,
    val context: MarketContext,
    val validation: DecisionValidation,
    val generatedAtEpochMs: Long
)
