package com.personal.gridbot.amaros.ai.core

import android.content.Context

/**
 * Compatibility boundary for the AI package.
 * The canonical authority lives in com.personal.gridbot.amaros.ai.AmarAiControlCenter;
 * this facade prevents package drift from creating a second authority implementation.
 */
class AmarAiControlCenter(context: Context) {
    private val delegate = com.personal.gridbot.amaros.ai.AmarAiControlCenter(context)

    val workspace: AmarAiWorkspace get() = delegate.workspace
    val mt5Office: AmarAiMt5Office get() = delegate.mt5Office
    val researchAuthority: AmarAiResearchAuthority get() = delegate.researchAuthority

    val aiEnabled: Boolean get() = delegate.aiEnabled
    val emergencyStopped: Boolean get() = delegate.emergencyStopped
    val aiAuthorityEnabled: Boolean get() = delegate.aiAuthorityEnabled
    val executionAuthorized: Boolean get() = delegate.executionAuthorized
    val brokerExecutionAuthorized: Boolean get() = delegate.brokerExecutionAuthorized

    fun setAiEnabled(enabled: Boolean) = delegate.setAiEnabled(enabled)
    fun setEmergencyStopped(stopped: Boolean) = delegate.setEmergencyStopped(stopped)
    fun enableAi(): Boolean = delegate.enableAi()
    fun disableAi() = delegate.disableAi()
    fun emergencyStop() = delegate.emergencyStop()
    fun clearEmergencyStop() = delegate.clearEmergencyStop()
}
