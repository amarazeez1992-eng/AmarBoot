package com.personal.gridbot.amaros.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AuroraDarkScheme = darkColorScheme(
    primary = AmarAuroraColors.Cyan,
    secondary = AmarAuroraColors.Violet,
    tertiary = AmarAuroraColors.Emerald,
    background = AmarAuroraColors.Background,
    surface = AmarAuroraColors.Surface
)

private val AuroraLightScheme = lightColorScheme(
    primary = AmarAuroraColors.Blue,
    secondary = AmarAuroraColors.Violet,
    tertiary = AmarAuroraColors.Emerald,
    background = AmarAuroraColors.LightBackground,
    surface = AmarAuroraColors.LightSurface
)

@Composable
fun AmarAuroraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) AuroraDarkScheme else AuroraLightScheme,
        content = content
    )
}
