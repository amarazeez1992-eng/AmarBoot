package com.personal.gridbot.amaros.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** ثيم Aurora مستقل؛ الثيم القديم محفوظ ولم يتم استبداله. */
private val AuroraScheme = darkColorScheme(
    primary = Color(0xFF63F5FF),
    secondary = Color(0xFF9C7BFF),
    tertiary = Color(0xFF45E6A8),
    background = Color(0xFF07111D),
    surface = Color(0xFF0C1928),
    surfaceVariant = Color(0xFF14263A),
    onPrimary = Color(0xFF001014),
    onSecondary = Color.White,
    onTertiary = Color(0xFF00130B),
    onBackground = Color(0xFFEAFBFF),
    onSurface = Color(0xFFEAFBFF)
)

@Composable
fun AmarAuroraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuroraScheme,
        content = content
    )
}

@Suppress("UNUSED_PARAMETER")
private fun keepThemeReference(isDark: Boolean = false) = isDark
