package com.personal.gridbot.amaros.visual

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/** Minimal visual state retained for the new HTML workspace bridge. No legacy overlay or animation. */
enum class AmarTradingVisualState { NORMAL, PROFIT, LOSS }

data class AmarGlobalVisualState(
    val enabled: Boolean = true,
    val tradingState: AmarTradingVisualState = AmarTradingVisualState.NORMAL,
    val floatingProfitLoss: Double? = null,
    val sourceFresh: Boolean = false,
    val eventNonce: Long = 0L,
)

object AmarGlobalVisualStateStore {
    private val state = mutableStateOf(AmarGlobalVisualState())
    fun current(): AmarGlobalVisualState = state.value
    fun publish(next: AmarGlobalVisualState) { state.value = next }
    fun setEnabled(enabled: Boolean) { state.value = state.value.copy(enabled = enabled) }
}

object AmarVisualEffectsPreference {
    private const val PREFS = "amar_visual_effects"
    private const val KEY_ENABLED = "enabled"

    fun load(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, true)

    fun save(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}
