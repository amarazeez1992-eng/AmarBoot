package com.personal.gridbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.personal.gridbot.ui.amarapproved.AmarApprovedHomeScreen
import com.personal.gridbot.ui.theme.AmarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmarTheme {
                AmarApprovedHomeScreen()
            }
        }
    }
}
