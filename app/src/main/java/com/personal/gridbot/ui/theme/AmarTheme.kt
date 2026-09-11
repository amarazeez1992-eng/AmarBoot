package com.personal.gridbot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

enum class AmarThemeMode { DARK, LIGHT, AUTO }

val LocalAmarPalette = compositionLocalOf { DefaultAmarPalette }
val LocalAmarThemeMode = compositionLocalOf { AmarThemeMode.DARK }

private fun amarColorScheme(palette: AmarPalette, mode: AmarThemeMode, systemDark: Boolean) = if (mode == AmarThemeMode.LIGHT || (mode == AmarThemeMode.AUTO && !systemDark)) {
    lightColorScheme(primary = palette.accent, onPrimary = palette.background, secondary = palette.buy, onSecondary = palette.background, tertiary = palette.sell, background = palette.background, onBackground = palette.textPrimary, surface = palette.surface, onSurface = palette.textPrimary, surfaceVariant = palette.surfaceElevated, onSurfaceVariant = palette.textSecondary, outline = palette.border, error = palette.danger)
} else {
    darkColorScheme(primary = palette.accent, onPrimary = palette.background, secondary = palette.buy, onSecondary = palette.background, tertiary = palette.sell, background = palette.background, onBackground = palette.textPrimary, surface = palette.surface, onSurface = palette.textPrimary, surfaceVariant = palette.surfaceElevated, onSurfaceVariant = palette.textSecondary, outline = palette.border, error = palette.danger, onError = palette.textPrimary)
}

@Composable
fun AmarTheme(palette: AmarPalette = DefaultAmarPalette, mode: AmarThemeMode = AmarThemeMode.DARK, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAmarPalette provides palette, LocalAmarThemeMode provides mode) {
        MaterialTheme(colorScheme = amarColorScheme(palette, mode, isSystemInDarkTheme()), content = content)
    }
}

object AmarThemeColors {
    @Composable fun current(): AmarPalette = LocalAmarPalette.current
    @Composable fun mode(): AmarThemeMode = LocalAmarThemeMode.current
}
