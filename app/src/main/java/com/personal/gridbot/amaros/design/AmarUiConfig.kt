package com.personal.gridbot.amaros.design

/** Serializable-style presentation configuration model for future settings storage. */
data class AmarUiConfig(
    val themeId: String = "aurora-default",
    val animation: AmarAnimationLevel = AmarAnimationLevel.MEDIUM,
    val glassStrength: Float = 0.78f,
    val glowStrength: Float = 0.65f,
    val compactMode: Boolean = false,
    val navigationVisible: Boolean = true,
    val robotVisible: Boolean = true
)

fun AmarUiConfig.toPreferences(): AmarUiPreferences = AmarUiPreferences(
    animation = animation,
    glassStrength = glassStrength,
    glowStrength = glowStrength,
    compactMode = compactMode,
    showRoomNavigation = navigationVisible,
    showRobot = robotVisible
)
