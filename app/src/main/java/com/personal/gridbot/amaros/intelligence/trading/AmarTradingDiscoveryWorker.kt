package com.personal.gridbot.amaros.intelligence.trading

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.personal.gridbot.R

/** Periodic, fail-safe discovery. It updates evidence fingerprints and alerts only on new findings. */
class AmarTradingDiscoveryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val report = runCatching {
            AmarTradingSourceMesh.research(
                applicationContext,
                "new trading strategy indicator open source bot algorithmic trading research"
            )
        }.getOrElse { return Result.retry() }
        if (report.newItems.isNotEmpty()) {
            notifyNewFindings(report.newItems.size, report.newItems.take(5).map { it.title })
        }
        return Result.success()
    }

    private fun notifyNewFindings(count: Int, titles: List<String>) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "amar_trading_discovery"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(channelId, "اكتشافات التداول", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val body = titles.joinToString(" • ")
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.amar_launcher)
            .setContentTitle("عمار AI: معلومات تداول جديدة")
            .setContentText("اكتشف AI $count نتيجة/نتائج جديدة: $body")
            .setStyle(NotificationCompat.BigTextStyle().bigText("اكتشف AI $count نتيجة/نتائج جديدة في مكتبة التداول.\n$body\n\nهذه إشعارات اكتشاف فقط؛ لا يتم اعتماد أو تنفيذ أي استراتيجية تلقائياً."))
            .setAutoCancel(true)
            .build()
        manager.notify(7042, notification)
    }
}

object AmarTradingDiscoveryScheduler {
    private const val WORK_NAME = "amar_trading_discovery"

    fun start(context: Context) {
        runCatching {
            val app = context.applicationContext
            val request = androidx.work.PeriodicWorkRequestBuilder<AmarTradingDiscoveryWorker>(30, java.util.concurrent.TimeUnit.MINUTES)
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            androidx.work.WorkManager.getInstance(app).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
