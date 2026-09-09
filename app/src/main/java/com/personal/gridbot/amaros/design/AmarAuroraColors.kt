package com.personal.gridbot.amaros.design

import androidx.compose.ui.graphics.Color

/** AMAR Aurora palette kept independent from trading logic. */
data class AmarAuroraColors(
    val background: Color,
    val surface: Color,
    val surfaceStrong: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val text: Color,
    val textMuted: Color
)

object AmarAuroraPalette {
    val default = AmarAuroraColors(
        background = Color(0xFF07101C),
        surface = Color(0xFF101A2A),
        surfaceStrong = Color(0xFF16243A),
        primary = Color(0xFF35D6FF),
        secondary = Color(0xFF8B5CFF),
        accent = Color(0xFFFFC857),
        success = Color(0xFF39E58C),
        warning = Color(0xFFFFB84D),
        danger = Color(0xFFFF5D73),
        text = Color(0xFFF3F7FF),
        textMuted = Color(0xFF9EABC0)
    )
}
