package com.personal.gridbot.amaros.grid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarGridPolicyTest {
    @Test
    fun validProgressionIsAcceptedAndCalculated() {
        val config = AmarGridPolicy.Config(
            symbol = "XAUUSD",
            anchorPrice = 2500.0,
            levelsPerSide = 3,
            distance = 5.0,
            baseQuantity = 0.01,
            quantityStep = 0.01,
            quantityMultiplier = 1.0,
            direction = AmarGridPolicy.DirectionMode.BOTH,
            basketTakeProfit = 10.0,
            basketStopLoss = 20.0,
        )

        assertTrue(AmarGridPolicy.validate(config).isEmpty())
        assertEquals(listOf(0.01, 0.02, 0.03), AmarGridPolicy.quantities(config))
    }

    @Test
    fun invalidRisklessGridParametersAreRejected() {
        val config = AmarGridPolicy.Config(
            symbol = "XAUUSD",
            anchorPrice = 0.0,
            levelsPerSide = 0,
            distance = -1.0,
            baseQuantity = 0.0,
            quantityMultiplier = 0.5,
        )

        assertEquals(
            listOf(
                "ANCHOR_PRICE_INVALID",
                "LEVEL_COUNT_INVALID",
                "GRID_DISTANCE_INVALID",
                "BASE_QUANTITY_INVALID",
                "QUANTITY_MULTIPLIER_INVALID",
            ),
            AmarGridPolicy.validate(config)
        )
    }
}
