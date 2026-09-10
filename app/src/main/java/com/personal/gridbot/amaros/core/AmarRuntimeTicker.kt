package com.personal.gridbot.amaros.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * B10 demo event runtime with B11/B12 lifecycle and health integration.
 * Advances the B9 controller on a controlled interval and remains isolated
 * from UI and trading execution.
 */
class AmarRuntimeTicker(
    private val controller: AmarRuntimeController,
    private val intervalMs: Long = 1000L
) {
    private var job: Job? = null

    fun start(scope: CoroutineScope): Job {
        job?.cancel()
        controller.onRuntimeStarted()
        val newJob = scope.launch {
            while (isActive) {
                val state = controller.advance()
                AmarEventBus.publish(
                    AmarEvent.SystemMessage(
                        "B10 cycle=${state.cycleNumber} • ${state.telemetry.symbol}/${state.telemetry.timeframe} • DEMO"
                    )
                )
                delay(intervalMs.coerceAtLeast(250L))
            }
        }
        job = newJob
        return newJob
    }

    fun stop() {
        job?.cancel()
        job = null
        controller.onRuntimeStopped()
    }
}
