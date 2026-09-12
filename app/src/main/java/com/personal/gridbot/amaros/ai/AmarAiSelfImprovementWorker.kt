package com.personal.gridbot.amaros.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.personal.gridbot.R
import java.util.concurrent.TimeUnit

/** Periodically audits the trading-only AI/bot-lab stack and alerts on governed improvement proposals. */
class AmarAiSelfImprovementWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val audit = runCatching { AmarAiSelfImprovementEngine(applicationContext).audit() }
            .getOrElse { return Result.retry() }
        val pending = audit.proposals.filter { it.status == AmarAiSelfImprovementEngine.Status.PROPOSED }
        if (pending.isNotEmpty()) notifyProposal(pending.size, pending.take(4).map { it.title })
        return Result.success()
    }

    private fun notifyProposal(count: Int, titles: List<String>) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "amar_ai_self_improvement"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "تطوير Amar AI", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val body = titles.joinToString(" • ")
        manager.notify(
            7043,
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.amar_launcher)
                .setContentTitle("AMAR AI: مقترحات تطوير جديدة")
                .setContentText("$count مقترحًا يحتاج مراجعتك: $body")
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    "اكتشف AI تحسينات خاصة بالتداول تحتاج مراجعتك.\n$body\n\nلا يتم تعديل الكود أو اعتماد استراتيجية تلقائيًا."
                ))
                .setAutoCancel(true)
                .build()
        )
    }
}

object AmarAiSelfImprovementScheduler {
    private const val WORK_NAME = "amar_ai_self_improvement"

    fun start(context: Context) {
        runCatching {
            val app = context.applicationContext
            val request = androidx.work.PeriodicWorkRequestBuilder<AmarAiSelfImprovementWorker>(30, TimeUnit.MINUTES)
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            androidx.work.WorkManager.getInstance(app).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
