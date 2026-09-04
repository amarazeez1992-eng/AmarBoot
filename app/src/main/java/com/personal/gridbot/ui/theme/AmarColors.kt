package com.personal.gridbot.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * AMAR UI - Dynamic Trading Color System
 *
 * هذا الملف يحتوي على لوحات الألوان الأساسية.
 * لا يحتاج إلى أي مكتبات خارجية.
 */

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

/**
 * اللوحة الرئيسية - AMAR Platinum
 */
val AmarPlatinum = AmarPalette(
    background = Color(0xFF080A0D),
    surface = Color(0xFF0E1116),
    surfaceElevated = Color(0xFF151A21),
    panel = Color(0xFF11161D),
    border = Color(0xFF27303A),

    textPrimary = Color(0xFFF5F7FA),
    textSecondary = Color(0xFFB5BDC8),
    textMuted = Color(0xFF68727E),

    buy = Color(0xFF00E676),
    buySoft = Color(0x3322E676),
    sell = Color(0xFFFF3D57),
    sellSoft = Color(0x33FF3D57),

    neutral = Color(0xFF7D8794),
    warning = Color(0xFFFFB300),
    danger = Color(0xFFFF1744),

    accent = Color(0xFFFFC107),
    accentSoft = Color(0x33FFC107),

    profit = Color(0xFF00E676),
    loss = Color(0xFFFF3D57)
)

/**
 * لوحة AMAR Blue
 */
val AmarBlue = AmarPlatinum.copy(
    accent = Color(0xFF40C4FF),
    accentSoft = Color(0x3340C4FF)
)

/**
 * لوحة AMAR Purple
 */
val AmarPurple = AmarPlatinum.copy(
    accent = Color(0xFFB388FF),
    accentSoft = Color(0x33B388FF)
)

/**
 * لوحة AMAR Orange
 */
val AmarOrange = AmarPlatinum.copy(
    accent = Color(0xFFFF9100),
    accentSoft = Color(0x33FF9100)
)

/**
 * لوحة AMAR Emerald
 */
val AmarEmerald = AmarPlatinum.copy(
    accent = Color(0xFF00C853),
    accentSoft = Color(0x3300C853)
)

/**
 * اللوحة الافتراضية.
 */
val DefaultAmarPalette = AmarPlatinum
