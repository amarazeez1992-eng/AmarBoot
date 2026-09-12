package com.personal.gridbot.amaros.intelligence.trading

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.personal.gridbot.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Lightweight in-process discovery loop. It only discovers and notifies; it never changes strategy or executes trades. */
object AmarTradingDiscoveryScheduler {
    private const val CHANNEL_ID = "amar_trading_discovery"
    private const val NOTIFICATION_ID = 4101
    private const val INTERVAL_MS = 30L * 60L * 1000L
    private var started = false
    private var scope: CoroutineScope? = null

    fun start(context: Context) {
        if (started) return
        started = true
        val appContext = context.applicationContext
        ensureChannel(appContext)
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO).also { worker ->
            worker.launch {
                runDiscovery(appContext)
                while (true) {
                    delay(INTERVAL_MS)
                    runDiscovery(appContext)
                }
            }
        }
    }

    private suspend fun runDiscovery(context: Context) {
        runCatching {
            val report = AmarTradingSourceMesh.research(
                context = context,
                query = "new open source trading strategy bot indicator quantitative research"
            )
            if (report.newItems.isNotEmpty()) {
                postNotification(context, report.newItems.size, report.supportingChannels)
            }
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "اكتشافات التداول", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "تنبيهات المعلومات والمصادر الجديدة في مكتبة عمار"
                }
            )
        }
    }

    private fun postNotification(context: Context, newCount: Int, sources: Int) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("معلومة تداول جديدة في عمار")
            .setContentText("اكتشف AI $newCount نتيجة جديدة من $sources مصدر/قناة بحثية.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }
}
