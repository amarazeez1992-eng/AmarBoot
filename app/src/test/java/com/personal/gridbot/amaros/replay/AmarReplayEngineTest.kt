package com.personal.gridbot.amaros.replay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarReplayEngineTest {
    private fun bars() = listOf(
        AmarReplayEngine.ReplayBar(1_000L, 100.0, 105.0, 99.0, 103.0, 10.0),
        AmarReplayEngine.ReplayBar(2_000L, 103.0, 108.0, 101.0, 106.0, 12.0),
        AmarReplayEngine.ReplayBar(3_000L, 106.0, 109.0, 104.0, 105.0, 11.0)
    )

    @Test
    fun validHistoricalBarsStartAtFirstBarAndAdvanceDeterministically() {
        val result = AmarReplayEngine.start(bars())
        assertTrue(result.isSuccess)
        val session = result.getOrThrow()
        assertEquals(1000L, session.current()!!.timestampEpochMillis)
        assertTrue(session.hasNext())
        assertEquals(50.0, session.next().progressPct(), 0.0001)
        assertEquals(3000L, session.next().next().current()!!.timestampEpochMillis)
        assertFalse(session.next().next().hasNext())
    }

    @Test
    fun nextAndPreviousDoNotMovePastReplayBoundaries() {
        val session = AmarReplayEngine.start(bars()).getOrThrow()
        val last = session.next().next()
        assertEquals(last, last.next())
        assertEquals(session, session.previous())
        assertEquals(session, last.reset())
    }

    @Test
    fun emptyAndNonAscendingDataAreRejected() {
        assertTrue(AmarReplayEngine.validate(emptyList()).contains("REPLAY_DATA_EMPTY"))
        val invalid = listOf(
            AmarReplayEngine.ReplayBar(2_000L, 100.0, 101.0, 99.0, 100.0),
            AmarReplayEngine.ReplayBar(1_000L, 100.0, 101.0, 99.0, 100.0)
        )
        assertTrue(AmarReplayEngine.validate(invalid).contains("BAR_1_TIMESTAMP_NOT_ASCENDING"))
    }

    @Test
    fun invalidOhlcAndVolumeAreRejected() {
        val invalid = listOf(
            AmarReplayEngine.ReplayBar(1_000L, 100.0, 90.0, 95.0, 98.0, -1.0)
        )
        val errors = AmarReplayEngine.validate(invalid)
        assertTrue("BAR_0_HIGH_INVALID" in errors)
        assertTrue("BAR_0_LOW_INVALID" in errors)
        assertTrue("BAR_0_VOLUME_INVALID" in errors)
    }
}
