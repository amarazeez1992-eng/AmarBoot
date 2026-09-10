package com.personal.gridbot.ui.theme

import androidx.compose.ui.graphics.Color

data class AmarPalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val panel: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val buy: Color,
    val buySoft: Color,
    val sell: Color,
    val sellSoft: Color,
    val neutral: Color,
    val warning: Color,
    val danger: Color,
    val accent: Color,
    val accentSoft: Color,
    val profit: Color,
    val loss: Color
)

/** الوضع الليلي: أسود مزرق/سماوي مع ذهبي AMAR. */
val AmarPlatinum = AmarPalette(
    background = Color(0xFF07121B), surface = Color(0xFF0D202B), surfaceElevated = Color(0xFF102A37),
    panel = Color(0xFF0F1D26), border = Color(0xFF214452),
    textPrimary = Color(0xFFE9FBFF), textSecondary = Color(0xFF9BB8C2), textMuted = Color(0xFF637F8A),
    buy = Color(0xFF00E6A0), buySoft = Color(0x2200E6A0), sell = Color(0xFFFF5364), sellSoft = Color(0x22FF5364),
    neutral = Color(0xFF7D96A0), warning = Color(0xFFFFC84D), danger = Color(0xFFFF5364),
    accent = Color(0xFF19E6FF), accentSoft = Color(0x2219E6FF),
    profit = Color(0xFF00E6A0), loss = Color(0xFFFF5364)
)

/** الوضع النهاري: أبيض نظيف مع حدود سماوية وذهبية. */
val AmarDay = AmarPalette(
    background = Color(0xFFF4F8FA), surface = Color(0xFFFFFFFF), surfaceElevated = Color(0xFFEAF7FA),
    panel = Color(0xFFFFFFFF), border = Color(0xFFB8D8E0),
    textPrimary = Color(0xFF10232B), textSecondary = Color(0xFF49636D), textMuted = Color(0xFF78909A),
    buy = Color(0xFF00A878), buySoft = Color(0x1800A878), sell = Color(0xFFD9364B), sellSoft = Color(0x18D9364B),
    neutral = Color(0xFF607D86), warning = Color(0xFFB87900), danger = Color(0xFFD9364B),
    accent = Color(0xFF009FC0), accentSoft = Color(0x18009FC0),
    profit = Color(0xFF00A878), loss = Color(0xFFD9364B)
)

val AmarBlue = AmarPlatinum.copy(accent = Color(0xFF40C4FF), accentSoft = Color(0x3340C4FF))
val AmarPurple = AmarPlatinum.copy(accent = Color(0xFFB388FF), accentSoft = Color(0x33B388FF))
val AmarOrange = AmarPlatinum.copy(accent = Color(0xFFFF9100), accentSoft = Color(0x33FF9100))
val AmarEmerald = AmarPlatinum.copy(accent = Color(0xFF00C853), accentSoft = Color(0x3300C853))
val DefaultAmarPalette = AmarPlatinum
