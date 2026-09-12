package com.personal.gridbot.amaros.trading

import com.personal.gridbot.amaros.intelligence.trading.AmarQuantResearchPlatform
import org.junit.Assert.*
import org.junit.Test

class AmarQuantResearchPlatformTest {
    private fun bars(n: Int): List<AmarQuantResearchPlatform.Bar> = (0 until n).map {
        val p = 100.0 + it
        AmarQuantResearchPlatform.Bar(it.toLong(), p, p + 1.0, p - 1.0, p + 0.5)
    }

    @Test fun rejectsFutureDataAndNonMonotonicTime() {
        val valid = AmarQuantResearchPlatform.validateNoLeakage(bars(5), 3)
        assertFalse(valid.valid)
        val bad = bars(3).mapIndexed { i, b -> if (i == 2) b.copy(timestampMs = 1) else b }
        assertFalse(AmarQuantResearchPlatform.validateNoLeakage(bad).valid)
    }

    @Test fun fingerprintIsStable() {
        val a = AmarQuantResearchPlatform.fingerprint(bars(10))
        val b = AmarQuantResearchPlatform.fingerprint(bars(10))
        assertEquals(a.value, b.value)
        assertEquals(10, a.sampleSize)
    }

    @Test fun walkForwardKeepsOosSeparate() {
        val result = AmarQuantResearchPlatform.walkForward(bars(30), 10, 5, 5, 5) { it.last().close - it.first().open }
        assertEquals(3, result.windows.size)
        assertEquals(3, result.oosScores.size)
        assertTrue(result.windows.all { it.oos.first > it.validation.last })
    }

    @Test fun stressCanFailWhenCostsEraseEdge() {
        val result = AmarQuantResearchPlatform.stressTrades(listOf(0.1, 0.2, -0.1), 0.2, 0.1, scenario = "WIDE_SPREAD")
        assertTrue(result.netR < 0.0)
        assertFalse(result.passed)
    }

    @Test fun mutationRequiresLeakageStressSampleAndOos() {
        val mutation = AmarQuantResearchPlatform.mutate("v1", listOf("add regime filter")).single()
        assertFalse(AmarQuantResearchPlatform.gateMutation(mutation, 0.5, -0.1, 50, false, true).acceptedAsChallenger)
        assertFalse(AmarQuantResearchPlatform.gateMutation(mutation, 0.5, -0.1, 50, true, false).acceptedAsChallenger)
        assertFalse(AmarQuantResearchPlatform.gateMutation(mutation, 0.5, -0.1, 10, true, true).acceptedAsChallenger)
        assertTrue(AmarQuantResearchPlatform.gateMutation(mutation, 0.5, -0.1, 50, true, true).acceptedAsChallenger)
    }

    @Test fun driftAndDnaAreDeterministic() {
        assertTrue(AmarQuantResearchPlatform.drift(1.0, 1.3).drifted)
        assertEquals(AmarQuantResearchPlatform.strategyDna(listOf("A", "B")), AmarQuantResearchPlatform.strategyDna(listOf("B", "A")))
    }
}
