package com.personal.gridbot.amaros.replay

/**
 * Deterministic, data-only historical replay boundary.
 *
 * This module never fetches market data, never creates broker commands, and never
 * presents replay state as live broker state. The caller must supply historical bars.
 */
object AmarReplayEngine {
    data class ReplayBar(
        val timestampEpochMillis: Long,
        val open: Double,
        val high: Double,
        val low: Double,
        val close: Double,
        val volume: Double = 0.0
    )

    data class ReplaySession private constructor(
        val bars: List<ReplayBar>,
        val index: Int,
        val isReplay: Boolean = true
    ) {
        fun current(): ReplayBar? = bars.getOrNull(index)
        fun hasNext(): Boolean = index + 1 < bars.size
        fun hasPrevious(): Boolean = index > 0

        fun next(): ReplaySession =
            if (hasNext()) copy(index = index + 1) else this

        fun previous(): ReplaySession =
            if (hasPrevious()) copy(index = index - 1) else this

        fun reset(): ReplaySession = copy(index = 0)

        fun progressPct(): Double =
            if (bars.size <= 1) 100.0 else index.toDouble() / (bars.size - 1).toDouble() * 100.0

        companion object {
            internal fun create(bars: List<ReplayBar>): ReplaySession =
                ReplaySession(bars = bars, index = 0)
        }
    }

    fun validate(bars: List<ReplayBar>): List<String> {
        val errors = mutableListOf<String>()
        if (bars.isEmpty()) errors += "REPLAY_DATA_EMPTY"

        var previousTimestamp: Long? = null
        bars.forEachIndexed { index, bar ->
            if (bar.timestampEpochMillis < 0L) errors += "BAR_${index}_TIMESTAMP_INVALID"
            if (previousTimestamp != null && bar.timestampEpochMillis <= previousTimestamp!!) {
                errors += "BAR_${index}_TIMESTAMP_NOT_ASCENDING"
            }
            previousTimestamp = bar.timestampEpochMillis

            val prices = listOf(bar.open, bar.high, bar.low, bar.close)
            if (prices.any { !it.isFinite() || it <= 0.0 }) {
                errors += "BAR_${index}_PRICE_INVALID"
            } else {
                if (bar.high < maxOf(bar.open, bar.close, bar.low)) errors += "BAR_${index}_HIGH_INVALID"
                if (bar.low > minOf(bar.open, bar.close, bar.high)) errors += "BAR_${index}_LOW_INVALID"
            }
            if (!bar.volume.isFinite() || bar.volume < 0.0) errors += "BAR_${index}_VOLUME_INVALID"
        }
        return errors
    }

    fun start(bars: List<ReplayBar>): Result<ReplaySession> {
        val errors = validate(bars)
        return if (errors.isEmpty()) {
            Result.success(ReplaySession.create(bars.toList()))
        } else {
            Result.failure(IllegalArgumentException(errors.joinToString(",")))
        }
    }
}
