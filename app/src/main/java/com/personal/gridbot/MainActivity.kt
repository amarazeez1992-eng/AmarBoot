package com.personal.gridbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.personal.gridbot.amaros.core.AmarAppState
import com.personal.gridbot.amaros.shell.AmarAuroraShell
import com.personal.gridbot.ui.theme.AmarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmarTheme {
                Surface {
                    AmarAuroraShell(
                        initialState = AmarAppState(),
                        onOpenLegacyGrid = { }
                    )
                }
            }
        }
    }
}
