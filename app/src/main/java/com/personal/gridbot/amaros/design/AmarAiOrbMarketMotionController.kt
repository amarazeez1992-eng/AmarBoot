package com.personal.gridbot.amaros.design

import com.personal.gridbot.amaros.bots.AmarMarketDataQuality
import com.personal.gridbot.amaros.bots.AmarMarketDirection
import com.personal.gridbot.amaros.bots.AmarMarketSnapshot

/**
 * Converts verified market-state metadata into bounded visual motion only.
 * No signal is generated here and no trading behavior is changed.
 */
object AmarAiOrbMarketMotionController {
    data class VisualState(
        val direction: AmarMarketDirection,
        val strength: Double,
        val quality: AmarMarketDataQuality,
        val intensity: Double,
        val driftPx: Double,
        val durationSeconds: Double
    )

    fun from(snapshot: AmarMarketSnapshot): VisualState {
        val strength = snapshot.strength?.takeIf { it.isFinite() }?.coerceIn(0.0, 1.0) ?: 0.0
        val qualityFactor = when (snapshot.quality) {
            AmarMarketDataQuality.LIVE -> 1.0
            AmarMarketDataQuality.DELAYED -> 0.65
            AmarMarketDataQuality.STALE -> 0.35
            AmarMarketDataQuality.UNAVAILABLE -> 0.0
        }
        val intensity = (strength * qualityFactor).coerceIn(0.0, 1.0)
        return VisualState(
            direction = snapshot.direction,
            strength = strength,
            quality = snapshot.quality,
            intensity = intensity,
            driftPx = (2.5 + intensity * 6.0).coerceIn(2.5, 8.5),
            durationSeconds = (7.0 - intensity * 2.0).coerceIn(5.0, 7.0)
        )
    }

    fun javascript(snapshot: AmarMarketSnapshot): String {
        val state = from(snapshot)
        val direction = state.direction.name.lowercase()
        val quality = state.quality.name.lowercase()
        return "window.amarAiOrbSetMarketState && window.amarAiOrbSetMarketState('$direction',${state.intensity},'$quality',${state.driftPx},${state.durationSeconds})"
    }
}
