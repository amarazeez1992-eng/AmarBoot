package com.personal.gridbot.ui.gridcontrol

/**
 * الحالة المرئية للوحة AMAR GRID CONTROL.
 *
 * هذه الطبقة لا تنفذ أي أوامر تداول حقيقية حاليًا.
 * هي فقط تمثل الحالة التي ستُربط لاحقًا بمحرك البوت.
 */
data class GridControlState(
    val botName: String = "AMAR GRID",
    val symbol: String = "XAUUSD",

    val isTrading: Boolean = false,
    val buyEnabled: Boolean = true,
    val sellEnabled: Boolean = true,
    val gridEnabled: Boolean = true,

    val positions: Int = 0,
    val pendingOrders: Int = 0,

    val floatingProfit: Double = 0.0,
    val equity: Double = 0.0,

    val lotStart: Double = 0.01,
    val gridStep: Int = 30,
    val maxOrders: Int = 10,
    val martingale: Double = 2.0,

    val basketTp: Double = 50.0,
    val basketSl: Double = -30.0,

    val individualTp: Double = 0.0,
    val individualSl: Double = 0.0,

    val enhancementEnabled: Boolean = false,
    val trailingEnabled: Boolean = false,
    val autoRebuildEnabled: Boolean = true,
    val newEntriesEnabled: Boolean = true,
    val manageManualPositions: Boolean = false
)
