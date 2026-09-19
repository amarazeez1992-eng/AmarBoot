package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.max

/** Local specialist matrix. No hosted AI provider is used. */
class AmarInternalIntelligenceMatrix(
    private val intent: AmarIntentEngine = AmarIntentEngine(),
    private val understanding: AmarUnderstandingEngine = AmarUnderstandingEngine(),
    private val knowledge: AmarKnowledgeEngine = AmarKnowledgeEngine(),
    private val discovery: AmarDiscoveryEngine = AmarDiscoveryEngine(),
    private val analysis: AmarAnalysisEngine = AmarAnalysisEngine(),
    private val inspection: AmarInspectionEngine = AmarInspectionEngine(),
    private val speed: AmarSpeedEngine = AmarSpeedEngine()
) {
    suspend fun analyze(text: String): AmarIntelligenceReport = coroutineScope {
        val jobs = listOf(
            async(Dispatchers.Default) { "INTENT=" + intent.run(text) },
            async(Dispatchers.Default) { "UNDERSTANDING=" + understanding.run(text) },
            async(Dispatchers.Default) { "KNOWLEDGE=" + knowledge.run(text) },
            async(Dispatchers.Default) { "DISCOVERY=" + discovery.run(text) },
            async(Dispatchers.Default) { "ANALYSIS=" + analysis.run(text) },
            async(Dispatchers.Default) { "INSPECTION=" + inspection.run(text) }
        )
        val started = System.nanoTime()
        val signals = jobs.awaitAll()
        val elapsed = (System.nanoTime() - started) / 1_000_000L
        AmarIntelligenceReport(signals, speed.run(elapsed, signals.size))
    }
}

data class AmarIntelligenceReport(
    val signals: List<String>,
    val speed: String
) {
    fun asContext(): String = buildString {
        appendLine("Internal intelligence matrix:")
        signals.forEach { appendLine(it) }
        appendLine("SPEED=" + speed)
        appendLine("MODE=LOCAL_AMAR_CORE_ONLY")
    }
}

private class AmarIntentEngine {
    fun run(text: String): String {
        val q = normalize(text)
        return when {
            containsAny(q, "سعر", "price", "gold", "ذهب", "xau", "دولار", "usd", "news", "خبر") -> "research_or_current"
            containsAny(q, "حلل", "تحليل", "لماذا", "سبب", "قارن", "analyze", "why", "compare") -> "analysis"
            containsAny(q, "وقت", "ساعة", "تاريخ", "اليوم", "date", "time", "day") -> "local_time_context"
            containsAny(q, "من انت", "عرف نفسك", "قدرات", "who are you", "capabilities") -> "identity_or_capability"
            containsAny(q, "مرحبا", "هلو", "اهلا", "hello", "hi", "hey") -> "conversation"
            else -> "general_request"
        }
    }
}

private class AmarUnderstandingEngine {
    fun run(text: String): String {
        val q = normalize(text)
        val words = q.split(" ").filter { it.isNotBlank() }
        val question = containsAny(q, "؟", "?", "ما", "ماذا", "هل", "كم", "كيف", "لماذا", "what", "how", "why")
        return "tokens=" + words.size + ";question=" + question + ";language=" + if (q.any { it in 'ء'..'ي' }) "ar" else "mixed_or_other"
    }
}

private class AmarKnowledgeEngine {
    fun run(text: String): String {
        val q = normalize(text)
        val domains = mutableListOf<String>()
        if (containsAny(q, "ذهب", "gold", "xau")) domains += "gold"
        if (containsAny(q, "دولار", "usd", "forex", "عملات")) domains += "fx"
        if (containsAny(q, "خبر", "news", "حدث", "الأخبار")) domains += "news"
        if (containsAny(q, "تداول", "سوق", "market", "scalp", "سكالب")) domains += "trading"
        return "domains=" + if (domains.isEmpty()) "general" else domains.distinct().joinToString("+") +
            ";requires_external_facts=" + domains.any { it == "gold" || it == "fx" || it == "news" }
    }
}

private class AmarDiscoveryEngine {
    fun run(text: String): String {
        val q = normalize(text)
        val entities = listOf(
            "XAUUSD" to containsAny(q, "xauusd", "xau", "ذهب"),
            "USD" to containsAny(q, "usd", "دولار"),
            "forex" to containsAny(q, "forex", "فوركس", "عملات"),
            "news" to containsAny(q, "خبر", "news", "اخبار")
        ).filter { it.second }.map { it.first }
        return "entities=" + if (entities.isEmpty()) "none" else entities.joinToString(",") +
            ";ambiguity=" + if (q.isBlank()) "high" else "normal"
    }
}

private class AmarAnalysisEngine {
    fun run(text: String): String {
        val q = normalize(text)
        val dimensions = listOf(
            "cause" to containsAny(q, "لماذا", "سبب", "why", "بسبب"),
            "comparison" to containsAny(q, "قارن", "مقارنة", "compare"),
            "change" to containsAny(q, "ارتفع", "هبط", "تغير", "صعد", "نزل", "rise", "fall", "change"),
            "current_state" to containsAny(q, "الان", "حاليا", "اليوم", "now", "current")
        ).filter { it.second }.map { it.first }
        return "dimensions=" + if (dimensions.isEmpty()) "direct_answer" else dimensions.joinToString("+") +
            ";needs_synthesis=" + (dimensions.size > 1)
    }
}

private class AmarInspectionEngine {
    fun run(text: String): String {
        val q = normalize(text)
        val unsafe = containsAny(q, "نفذ صفقة", "execute trade", "buy now", "sell now")
        return "input_valid=" + q.isNotBlank() + ";execution_request=" + unsafe + ";fail_closed=true"
    }
}

private class AmarSpeedEngine {
    fun run(elapsedMs: Long, completed: Int): String {
        val throughput = if (elapsedMs <= 0L) completed else max(1L, completed * 1000L / elapsedMs)
        return "matrix_elapsed_ms=" + elapsedMs + ";completed=" + completed +
            ";signals_per_second_est=" + throughput + ";parallel=true"
    }
}

private fun normalize(value: String): String =
    value.lowercase()
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
        .replace('ة', 'ه')
        .replace(Regex("[؟?!.,،؛:]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun containsAny(text: String, vararg terms: String): Boolean =
    terms.any { text.contains(it) }
