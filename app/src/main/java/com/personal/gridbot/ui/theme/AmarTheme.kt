package com.personal.gridbot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

/**
 * AMAR Theme Engine
 *
 * المسؤول عن تمرير ألوان AMAR إلى جميع شاشات التطبيق.
 */

val LocalAmarPalette = compositionLocalOf {
    DefaultAmarPalette
}

private fun amarColorScheme(
    palette: AmarPalette
) = darkColorScheme(
    primary = palette.accent,
    onPrimary = palette.background,

    secondary = palette.buy,
    onSecondary = palette.background,

    tertiary = palette.sell,

    background = palette.background,
    onBackground = palette.textPrimary,

    surface = palette.surface,
    onSurface = palette.textPrimary,

    surfaceVariant = palette.surfaceElevated,
    onSurfaceVariant = palette.textSecondary,

    outline = palette.border,

    error = palette.danger,
    onError = palette.textPrimary
)

@Composable
fun AmarTheme(
    palette: AmarPalette = DefaultAmarPalette,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAmarPalette provides palette
    ) {
        MaterialTheme(
            colorScheme = amarColorScheme(palette),
            content = content
        )
    }
}

/**
 * وصول سريع إلى لوحة AMAR الحالية.
 */
object AmarThemeColors {

    @Composable
    fun current(): AmarPalette {
        return LocalAmarPalette.current
    }
}
