package com.personal.gridbot.amaros.core

/** Independent phase switches. Demo remains the only enabled operating mode. */
data class AmarPhaseFlags(
    val dataFabric: Boolean = true,
    val intelligenceFoundation: Boolean = true,
    val liveTrading: Boolean = false
)
