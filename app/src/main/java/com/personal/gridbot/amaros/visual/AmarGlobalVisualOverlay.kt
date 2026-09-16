package com.personal.gridbot.amaros.visual

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AmarGlobalVisualOverlay(context: Context) {
    AmarGlobalVisualStateCollector(context)
    Box(Modifier.fillMaxSize()) {
        AmarGlobalVisualAtmosphere(Modifier.fillMaxSize())
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 52.dp)
        ) {
            AmarVisualEffectsToggle(context)
        }
    }
}
