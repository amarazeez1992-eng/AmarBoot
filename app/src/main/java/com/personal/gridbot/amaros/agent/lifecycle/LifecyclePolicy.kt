package com.personal.gridbot.amaros.agent.lifecycle

data class LifecyclePolicy(
    val agedAfterMs: Long,
    val expiredAfterMs: Long
) {
    init {
        require(agedAfterMs >= 0L) { "agedAfterMs must be non-negative" }
        require(expiredAfterMs > agedAfterMs) { "expiredAfterMs must be greater than agedAfterMs" }
    }
}
