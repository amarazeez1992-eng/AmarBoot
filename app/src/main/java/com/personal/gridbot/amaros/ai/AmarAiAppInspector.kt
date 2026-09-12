package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.navigation.AmarRoom

/** Deterministic app introspection surface for AI. Reports state; never invents it. */
object AmarAiAppInspector {
    data class Snapshot(val rooms: List<String>, val currentMarket: String, val executionStage: String)
    fun snapshot(): Snapshot = Snapshot(
        rooms = AmarRoom.entries.map { it.titleAr },
        currentMarket = AmarMarketStateStore.snapshot.let { s -> "symbol=${s.symbol};tf=${s.timeframe};bid=${s.bid};ask=${s.ask};spread=${s.spread};direction=${s.direction};strength=${s.strength};quality=${s.quality};source=${s.source}" },
        executionStage = "APP_READY -> PENDING_MT5 -> MT5/BRIDGE -> ACK -> VERIFY"
    )
    fun describe(): String { val s = snapshot(); return "APP_ROOMS=${s.rooms.joinToString(" | ")}; MARKET=${s.currentMarket}; EXECUTION=${s.executionStage}" }
}
