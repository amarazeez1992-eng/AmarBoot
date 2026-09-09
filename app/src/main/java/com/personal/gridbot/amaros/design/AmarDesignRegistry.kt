package com.personal.gridbot.amaros.design

/**
 * Single registry for visual configuration. Rooms must not depend on trading
 * implementation details; they read presentation configuration through this layer.
 */
object AmarDesignRegistry {
    var colors: AmarAuroraColors = AmarAuroraPalette.default
        private set

    var preferences: AmarUiPreferences = AmarUiPreferences()
        private set

    fun setColors(value: AmarAuroraColors) {
        colors = value
    }

    fun setPreferences(value: AmarUiPreferences) {
        preferences = value
    }

    fun reset() {
        colors = AmarAuroraPalette.default
        preferences = AmarUiPreferences()
    }
}
