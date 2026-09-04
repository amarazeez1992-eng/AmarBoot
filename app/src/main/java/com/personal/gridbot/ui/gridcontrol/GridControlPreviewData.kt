package com.personal.gridbot.ui.gridcontrol

/**
 * بيانات تجريبية للواجهة.
 *
 * لا يوجد أي اتصال بـ MT5 أو MetaApi هنا.
 * الهدف منها إظهار الشكل النهائي للواجهة قبل مرحلة الربط.
 */
val PreviewGridControlState = GridControlState(
    botName = "AMAR GRID",
    symbol = "XAUUSD",

    isTrading = true,
    buyEnabled = true,
    sellEnabled = true,
    gridEnabled = true,

    positions = 6,
    pendingOrders = 18,

    floatingProfit = 12.84,
    equity = 1012.84,

    lotStart = 0.02,
    gridStep = 40,
    maxOrders = 30,
    martingale = 2.0,

    basketTp = 100.0,
    basketSl = -100.0,

    individualTp = 1.5,
    individualSl = -1.5,

    enhancementEnabled = false,
    trailingEnabled = false,
    autoRebuildEnabled = true,
    newEntriesEnabled = true,
    manageManualPositions = false
)
