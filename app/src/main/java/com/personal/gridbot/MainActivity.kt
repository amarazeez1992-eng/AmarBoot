package com.personal.gridbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.personal.gridbot.amaros.shell.AmarAppShell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmarRoot()
        }
    }
}

@Composable
private fun AmarRoot() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AmarAppShell()
        }
    }
}
