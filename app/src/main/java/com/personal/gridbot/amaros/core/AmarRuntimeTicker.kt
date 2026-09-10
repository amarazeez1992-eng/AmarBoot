package com.personal.gridbot.amaros.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * B10 demo event runtime. Advances the B9 controller on a controlled interval
 * and publishes observable system events. The ticker is deliberately isolated
 * from UI and trading execution.
 */
class AmarRuntimeTicker(
    private val controller: AmarRuntimeController,
    private val intervalMs: Long = 1000L
) {
    private var job: Job? = null

    fun start(scope: CoroutineScope): Job {
        job?.cancel()
        job = scope.launch {
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
        return job as Job
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
