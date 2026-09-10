package com.personal.gridbot.amaros.data

import com.personal.gridbot.amaros.core.AmarUiContract

/** Read-only adapter: B5 data can feed any UI engine without coupling UI to trading logic. */
object AmarUiDataAdapter {
    fun toTelemetry(snapshot: AmarDataSnapshot): AmarUiContract.Telemetry = AmarUiContract.Telemetry(
        symbol = snapshot.market.symbol,
        timeframe = snapshot.market.timeframe,
        bid = snapshot.market.bid,
        ask = snapshot.market.ask,
        spread = snapshot.market.spread,
        equity = snapshot.account.equity,
        drawdownPercent = snapshot.account.drawdownPercent,
        openPositions = snapshot.positions.size,
        demo = snapshot.demoOnly
    )
}
