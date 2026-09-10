package com.personal.gridbot.amaros.core

/**
 * Stable contract shared by the Android shell and future visual engines.
 * UI modules consume state; they do not decide trading actions.
 */
object AmarUiContract {
    const val VERSION = "1.0"
    const val MODE_DEMO = "DEMO"
    const val MODE_LIVE = "LIVE"

    enum class Room {
        HOME, MARKET, CHART, BOT, RISK, ANALYSIS, PERFORMANCE, TESTING, TOOLS, ALERTS, SETTINGS, LIBRARY
    }

    enum class MotionLevel { OFF, LOW, MEDIUM, HIGH, CINEMATIC }

    data class Telemetry(
        val symbol: String = "XAUUSD",
        val timeframe: String = "M5",
        val bid: Double = 0.0,
        val ask: Double = 0.0,
        val spread: Double = 0.0,
        val equity: Double = 0.0,
        val drawdownPercent: Double = 0.0,
        val openPositions: Int = 0,
        val demo: Boolean = true
    )

    data class UiState(
        val room: Room = Room.HOME,
        val telemetry: Telemetry = Telemetry(),
        val motion: MotionLevel = MotionLevel.MEDIUM,
        val selectedBotId: Long? = null,
        val guardianArmed: Boolean = true,
        val connectionReady: Boolean = false
    )
}
