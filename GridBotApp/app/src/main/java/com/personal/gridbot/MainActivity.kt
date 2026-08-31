package com.personal.gridbot

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.personal.gridbot.data.AppDatabase
import com.personal.gridbot.data.Bot
import com.personal.gridbot.ui.AddBotDialog
import com.personal.gridbot.ui.BotListScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDatabase.getInstance(applicationContext).botDao()

        setContent {
            var showDialog by remember { mutableStateOf(false) }
            val bots by dao.getAllBots().collectAsState(initial = emptyList())

            MaterialTheme {
                Surface(modifier = Modifier) {
                    BotListScreen(
                        bots = bots,
                        onAddBotClick = { showDialog = true },
                        onToggleActive = { bot ->
                            lifecycleScope.launch {
                                dao.updateBot(bot.copy(isActive = !bot.isActive))
                                ensureServiceRunning()
                            }
                        },
                        onDeleteBot = { bot -> lifecycleScope.launch { dao.deleteBot(bot) } }
                    )
                    if (showDialog) {
                        AddBotDialog(
                            onDismiss = { showDialog = false },
                            onConfirm = { bot ->
                                lifecycleScope.launch {
                                    dao.insertBot(bot)
                                    showDialog = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun ensureServiceRunning() {
        val intent = Intent(this, GridBotService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }
}
