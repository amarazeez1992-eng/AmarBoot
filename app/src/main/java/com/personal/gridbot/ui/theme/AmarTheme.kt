package com.personal.gridbot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

enum class AmarThemeMode { DARK, LIGHT }

val LocalAmarPalette = compositionLocalOf { DefaultAmarPalette }
val LocalAmarThemeMode = compositionLocalOf { AmarThemeMode.DARK }

private fun amarColorScheme(palette: AmarPalette, mode: AmarThemeMode) = if (mode == AmarThemeMode.LIGHT) {
    lightColorScheme(
        primary = palette.accent, onPrimary = palette.background, secondary = palette.buy, onSecondary = palette.background,
        tertiary = palette.sell, background = palette.background, onBackground = palette.textPrimary,
        surface = palette.surface, onSurface = palette.textPrimary, surfaceVariant = palette.surfaceElevated,
        onSurfaceVariant = palette.textSecondary, outline = palette.border, error = palette.danger
    )
} else {
    darkColorScheme(
        primary = palette.accent, onPrimary = palette.background, secondary = palette.buy, onSecondary = palette.background,
        tertiary = palette.sell, background = palette.background, onBackground = palette.textPrimary,
        surface = palette.surface, onSurface = palette.textPrimary, surfaceVariant = palette.surfaceElevated,
        onSurfaceVariant = palette.textSecondary, outline = palette.border, error = palette.danger, onError = palette.textPrimary
    )
}

@Composable
fun AmarTheme(
    palette: AmarPalette = DefaultAmarPalette,
    mode: AmarThemeMode = AmarThemeMode.DARK,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAmarPalette provides palette, LocalAmarThemeMode provides mode) {
        MaterialTheme(colorScheme = amarColorScheme(palette, mode), content = content)
    }
}

object AmarThemeColors {
    @Composable fun current(): AmarPalette = LocalAmarPalette.current
    @Composable fun mode(): AmarThemeMode = LocalAmarThemeMode.current
}
