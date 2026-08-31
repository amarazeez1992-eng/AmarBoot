package com.personal.gridbot

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.personal.gridbot.data.AppDatabase
import com.personal.gridbot.network.GridEngine
import kotlinx.coroutines.*

class GridBotService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var engine: GridEngine

    override fun onCreate() {
        super.onCreate()
        val dao = AppDatabase.getInstance(applicationContext).botDao()
        engine = GridEngine(dao)
        startForeground(1, buildNotification())

        scope.launch {
            while (isActive) {
                dao.getAllBots().collect { bots ->
                    bots.filter { it.isActive }.forEach { bot ->
                        engine.runCycle(bot)
                    }
                }
                delay(60_000L) // دورة كل دقيقة - عدّلها حسب الحاجة
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val channelId = "gridbot_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Grid Bot", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Grid Trade Bot يعمل")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .build()
    }
}
