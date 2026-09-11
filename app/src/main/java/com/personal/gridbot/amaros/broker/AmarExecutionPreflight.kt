package com.personal.gridbot.amaros.broker

/** B61: final mobile-side preflight. It blocks commands before transport when identity, symbol or health is unsafe. */
data class AmarExecutionPreflightResult(
    val allowed: Boolean,
    val reason: String,
)

object AmarExecutionPreflight {
    fun check(
        health: AmarBot1Health,
        selectedSymbol: String,
        targetSymbol: String?,
        expectedMagic: Long,
        actualMagic: Long,
        expectedBotId: String = "BOT_1",
        actualBotId: String? = "BOT_1",
    ): AmarExecutionPreflightResult {
        if (!health.safeForExecution) return AmarExecutionPreflightResult(false, health.reason)
        if (selectedSymbol.isBlank()) return AmarExecutionPreflightResult(false, "SYMBOL_NOT_SELECTED")
        if (targetSymbol != null && (targetSymbol.isBlank() || targetSymbol != selectedSymbol)) {
            return AmarExecutionPreflightResult(false, "TARGET_SYMBOL_MISMATCH")
        }
        if (expectedMagic != actualMagic) return AmarExecutionPreflightResult(false, "MAGIC_MISMATCH")
        if (actualBotId != expectedBotId) return AmarExecutionPreflightResult(false, "BOT_ID_MISMATCH")
        return AmarExecutionPreflightResult(true, "EXECUTION_PREFLIGHT_OK")
    }
}
