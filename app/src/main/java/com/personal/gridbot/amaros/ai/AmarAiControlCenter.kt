package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.ai.core.AmarAiMt5Office
import com.personal.gridbot.amaros.ai.core.AmarAiResearchAuthority
import com.personal.gridbot.amaros.ai.core.AmarAiWorkspace

/**
 * Central authority for the AI room.
 * AI may be enabled/disabled independently from manual bot operation.
 * Emergency stop is a higher-priority fail-closed gate for AI execution authority.
 * Research and MT5-office data remain isolated from application runtime state.
 */
class AmarAiControlCenter(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    val workspace: AmarAiWorkspace by lazy { AmarAiWorkspace(appContext) }
    val mt5Office: AmarAiMt5Office get() = AmarAiMt5Office
    val researchAuthority: AmarAiResearchAuthority get() = AmarAiResearchAuthority

    val aiEnabled: Boolean get() = prefs.getBoolean(KEY_AI_ENABLED, false)
    val emergencyStopped: Boolean get() = prefs.getBoolean(KEY_EMERGENCY_STOP, false)
    val executionAuthorized: Boolean get() = aiEnabled && !emergencyStopped

    fun setAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_ENABLED, enabled).apply()
    }

    fun setEmergencyStopped(stopped: Boolean) {
        prefs.edit().putBoolean(KEY_EMERGENCY_STOP, stopped).apply()
    }

    fun enableAi(): Boolean {
        if (emergencyStopped) return false
        setAiEnabled(true)
        return true
    }

    fun disableAi() = setAiEnabled(false)

    /** Immediate fail-closed action. It does not close broker positions by itself. */
    fun emergencyStop() {
        prefs.edit()
            .putBoolean(KEY_EMERGENCY_STOP, true)
            .putBoolean(KEY_AI_ENABLED, false)
            .apply()
    }

    fun clearEmergencyStop() {
        setEmergencyStopped(false)
    }

    companion object {
        private const val PREFS = "amar_ai_control_v1"
        private const val KEY_AI_ENABLED = "ai_enabled"
        private const val KEY_EMERGENCY_STOP = "emergency_stop"
    }
}
