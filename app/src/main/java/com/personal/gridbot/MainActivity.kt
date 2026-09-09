package com.personal.gridbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.personal.gridbot.ui.amarapproved.AmarReferenceHomeScreen
import com.personal.gridbot.ui.theme.AmarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmarTheme {
                AmarReferenceHomeScreen()
            }
        }
    }
}
