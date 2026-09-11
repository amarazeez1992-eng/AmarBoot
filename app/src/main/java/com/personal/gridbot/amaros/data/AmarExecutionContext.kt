package com.personal.gridbot.amaros.data

/**
 * Immutable execution context selected by the user before a rebuild/entry command.
 * Strategy parameters remain separate; this context identifies where the strategy runs.
 */
data class AmarExecutionContext(
    val symbol: String,
    val timeframe: String
) {
    fun validate(): Boolean = symbol.isNotBlank() && timeframe.isNotBlank()
}
