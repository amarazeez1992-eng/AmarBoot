package com.personal.gridbot.amaros.ai.knowledge

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.personal.gridbot.amaros.ai.AmarAiExternalResearch
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingSourceMesh
import java.util.concurrent.TimeUnit

object AmarAiKnowledgeSyncPolicy {
    const val WORK_NAME = "amar_ai_knowledge_sync"
    const val INTERVAL_HOURS: Long = 6
    val topics = listOf(
        "algorithmic trading research market regime risk management",
        "gold XAUUSD market research macroeconomic trading",
        "walk forward validation backtesting slippage execution",
        "TradingView Pine Script trading research",
        "open source quantitative trading frameworks"
    )
}

/** Refreshes the local research knowledge store from public evidence only. */
class AmarAiKnowledgeSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = runCatching {
        val research = AmarAiExternalResearch()
        AmarAiKnowledgeSyncPolicy.topics.forEach { topic ->
            AmarTradingSourceMesh.research(applicationContext, topic, research, maxQueries = 6)
        }
        Result.success()
    }.getOrElse { Result.retry() }
}

object AmarAiKnowledgeSyncScheduler {
    fun start(context: Context) {
        val request = androidx.work.PeriodicWorkRequestBuilder<AmarAiKnowledgeSyncWorker>(
            AmarAiKnowledgeSyncPolicy.INTERVAL_HOURS, TimeUnit.HOURS
        ).setConstraints(
            androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
        ).setBackoffCriteria(
            androidx.work.BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS
        ).build()
        androidx.work.WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            AmarAiKnowledgeSyncPolicy.WORK_NAME,
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
