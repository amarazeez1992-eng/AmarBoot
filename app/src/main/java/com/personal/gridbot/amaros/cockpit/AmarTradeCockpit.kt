package com.personal.gridbot.amaros.cockpit

/**
 * Read-only trade cockpit boundary.
 *
 * Aggregates caller-supplied positions into a deterministic action preview. It
 * never sends commands, changes broker state, or reports a preview as execution.
 */
object AmarTradeCockpit {
    enum class Action { CLOSE, BREAK_EVEN, SET_STOP_LOSS, SET_TAKE_PROFIT, TRAILING }

    data class Position(
        val id: Long,
        val symbol: String,
        val volume: Double,
        val profit: Double,
        val selected: Boolean = false,
    )

    data class Preview(
        val action: Action,
        val selectedCount: Int,
        val selectedVolume: Double,
        val selectedProfit: Double,
        val executable: Boolean,
        val reason: String?,
    )

    fun preview(positions: List<Position>, action: Action): Preview {
        val selected = positions.filter { it.selected }
        val invalid = selected.any {
            it.id <= 0L || it.symbol.isBlank() || !it.volume.isFinite() || it.volume <= 0.0 || !it.profit.isFinite()
        }
        if (selected.isEmpty()) {
            return Preview(action, 0, 0.0, 0.0, false, "NO_POSITIONS_SELECTED")
        }
        if (invalid) {
            return Preview(action, selected.size, 0.0, 0.0, false, "INVALID_POSITION_DATA")
        }
        return Preview(
            action = action,
            selectedCount = selected.size,
            selectedVolume = selected.sumOf { it.volume },
            selectedProfit = selected.sumOf { it.profit },
            executable = false,
            reason = "PREVIEW_ONLY",
        )
    }
}
