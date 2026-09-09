package com.personal.gridbot.amaros.design

import androidx.compose.ui.unit.dp

/**
 * Central visual tokens. UI modules consume these values instead of hard-coding
 * dimensions, making future redesigns localized to the design layer.
 */
object AmarUiTokens {
    val pagePadding = 16.dp
    val compactPadding = 8.dp
    val sectionGap = 12.dp
    val cardGap = 10.dp
    val cardRadius = 20.dp
    val controlRadius = 14.dp
    val headerHeight = 72.dp
    val navigationWidth = 112.dp
}

enum class AmarAnimationLevel { OFF, LOW, MEDIUM, HIGH, CINEMATIC }

data class AmarUiPreferences(
    val animation: AmarAnimationLevel = AmarAnimationLevel.MEDIUM,
    val glassStrength: Float = 0.78f,
    val glowStrength: Float = 0.65f,
    val compactMode: Boolean = false,
    val showRoomNavigation: Boolean = true,
    val showRobot: Boolean = true
)
