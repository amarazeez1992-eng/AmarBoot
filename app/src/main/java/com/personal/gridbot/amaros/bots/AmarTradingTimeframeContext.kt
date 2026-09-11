package com.personal.gridbot.amaros.bots

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.personal.gridbot.amaros.chart.AmarTimeframe

/** Global UI context only. It does not alter any trading strategy or execution logic. */
object AmarTradingTimeframeContext {
    var selected: AmarTimeframe by mutableStateOf(AmarTimeframe.M1)
}

fun AmarTimeframe.remainingMillis(nowMillis: Long = System.currentTimeMillis()): Long {
    val minute = java.time.Instant.ofEpochMilli(nowMillis).atZone(java.time.ZoneOffset.UTC)
    val next = when (this) {
        AmarTimeframe.MN1 -> minute.withDayOfMonth(1).plusMonths(1).truncatedTo(java.time.temporal.ChronoUnit.DAYS)
        AmarTimeframe.W1 -> minute.toLocalDate().plusDays((8 - minute.dayOfWeek.value).toLong()).atStartOfDay(java.time.ZoneOffset.UTC)
        AmarTimeframe.D1 -> minute.toLocalDate().plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC)
        AmarTimeframe.H1 -> minute.truncatedTo(java.time.temporal.ChronoUnit.HOURS).plusHours(1)
        AmarTimeframe.H2 -> nextFixed(minute, 2, true)
        AmarTimeframe.H3 -> nextFixed(minute, 3, true)
        AmarTimeframe.H4 -> nextFixed(minute, 4, true)
        AmarTimeframe.H6 -> nextFixed(minute, 6, true)
        AmarTimeframe.H8 -> nextFixed(minute, 8, true)
        AmarTimeframe.H12 -> nextFixed(minute, 12, true)
        AmarTimeframe.M1 -> nextFixed(minute, 1, false)
        AmarTimeframe.M2 -> nextFixed(minute, 2, false)
        AmarTimeframe.M3 -> nextFixed(minute, 3, false)
        AmarTimeframe.M4 -> nextFixed(minute, 4, false)
        AmarTimeframe.M5 -> nextFixed(minute, 5, false)
        AmarTimeframe.M6 -> nextFixed(minute, 6, false)
        AmarTimeframe.M10 -> nextFixed(minute, 10, false)
        AmarTimeframe.M12 -> nextFixed(minute, 12, false)
        AmarTimeframe.M15 -> nextFixed(minute, 15, false)
        AmarTimeframe.M20 -> nextFixed(minute, 20, false)
        AmarTimeframe.M30 -> nextFixed(minute, 30, false)
    }
    return java.time.Duration.between(java.time.Instant.ofEpochMilli(nowMillis), next.toInstant()).toMillis().coerceAtLeast(0)
}

private fun nextFixed(now: java.time.ZonedDateTime, size: Int, hours: Boolean): java.time.ZonedDateTime {
    val current = if (hours) now.hour else now.minute
    val next = ((current / size) + 1) * size
    return if (hours) {
        if (next >= 24) now.truncatedTo(java.time.temporal.ChronoUnit.DAYS).plusDays(1)
        else now.truncatedTo(java.time.temporal.ChronoUnit.DAYS).plusHours(next.toLong())
    } else {
        if (next >= 60) now.truncatedTo(java.time.temporal.ChronoUnit.HOURS).plusHours(1)
        else now.truncatedTo(java.time.temporal.ChronoUnit.HOURS).plusMinutes(next.toLong())
    }
}

fun formatTimeframeRemaining(ms: Long): String {
    val seconds = (ms / 1000).coerceAtLeast(0)
    return if (seconds >= 3600) "%02d:%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60, seconds % 60)
    else "%02d:%02d".format(seconds / 60, seconds % 60)
}
