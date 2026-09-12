package com.personal.gridbot.amaros.ai.core

import java.net.URI

/** Evidence authority: confidence in evidence quality, never a fabricated profitability probability. */
object AmarAiResearchAuthority {
    enum class Grade { LOW, MODERATE, HIGH, VERY_HIGH, VERIFIED }

    data class Evidence(
        val source: String,
        val title: String,
        val url: String,
        val excerpt: String,
        val publishedAtMs: Long? = null,
        val methodology: String = "",
        val validation: String = ""
    )

    data class Report(
        val grade: Grade,
        val confidencePct: Double,
        val independencePct: Double,
        val freshnessPct: Double,
        val provenancePct: Double,
        val conflictPenaltyPct: Double,
        val evidenceCount: Int,
        val independentChannels: Int,
        val conflicts: List<String>,
        val reasons: List<String>
    )

    fun evaluate(items: List<Evidence>, nowMs: Long = System.currentTimeMillis()): Report {
        if (items.isEmpty()) return Report(Grade.LOW, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, emptyList(), listOf("لا توجد أدلة قابلة للتقييم"))
        val domains = items.mapNotNull { runCatching { URI(it.url).host?.lowercase()?.removePrefix("www.") }.getOrNull() }.distinct()
        val independent = domains.size
        val independence = (independent.toDouble() / items.size * 100.0).coerceIn(0.0, 100.0)
        val freshness = items.map { freshness(it.publishedAtMs, nowMs) }.average().coerceIn(0.0, 100.0)
        val provenance = items.map { provenance(it) }.average().coerceIn(0.0, 100.0)
        val conflicts = detectConflicts(items)
        val conflictPenalty = (conflicts.size * 12.5).coerceAtMost(50.0)
        val confidence = (0.30 * independence + 0.25 * freshness + 0.30 * provenance + 0.15 * 100.0 - conflictPenalty).coerceIn(0.0, 100.0)
        val verified = items.any { it.validation.contains("VERIFIED", ignoreCase = true) } && provenance >= 85.0 && independence >= 50.0 && conflicts.isEmpty()
        val grade = when {
            verified -> Grade.VERIFIED
            confidence >= 85.0 && independent >= 3 && conflicts.isEmpty() -> Grade.VERY_HIGH
            confidence >= 70.0 && independent >= 2 -> Grade.HIGH
            confidence >= 45.0 -> Grade.MODERATE
            else -> Grade.LOW
        }
        val reasons = buildList {
            add("الأدلة: ${items.size}؛ القنوات المستقلة: $independent")
            add("استقلال المصادر: ${"%.1f".format(independence)}%")
            add("حداثة: ${"%.1f".format(freshness)}%")
            add("اكتمال المصدر والمنهجية: ${"%.1f".format(provenance)}%")
            if (conflicts.isNotEmpty()) add("تم تخفيض الثقة بسبب تعارضات دلالية محتملة")
            add("هذه الدرجة لا تعني احتمال نجاح الصفقة")
        }
        return Report(grade, confidence, independence, freshness, provenance, conflictPenalty, items.size, independent, conflicts, reasons)
    }

    private fun provenance(e: Evidence): Double {
        var score = 35.0
        if (e.url.startsWith("https://")) score += 15
        if (e.source.isNotBlank()) score += 15
        if (e.title.isNotBlank()) score += 10
        if (e.excerpt.length >= 80) score += 10
        if (e.methodology.isNotBlank()) score += 10
        if (e.validation.isNotBlank()) score += 5
        return score
    }

    private fun freshness(publishedAtMs: Long?, nowMs: Long): Double {
        if (publishedAtMs == null || publishedAtMs <= 0L) return 55.0
        val days = ((nowMs - publishedAtMs).coerceAtLeast(0L) / 86_400_000.0)
        return when {
            days <= 7 -> 100.0
            days <= 30 -> 90.0
            days <= 90 -> 80.0
            days <= 365 -> 65.0
            days <= 730 -> 45.0
            else -> 25.0
        }
    }

    private fun detectConflicts(items: List<Evidence>): List<String> {
        val positive = setOf("supports", "positive", "benefit", "improves", "effective", "bullish", "increase", "يدعم", "إيجابي", "فعال", "ارتفاع")
        val negative = setOf("fails", "negative", "risk", "ineffective", "bearish", "decrease", "worse", "يفشل", "سلبي", "خطر", "غير فعال", "هبوط")
        val prepared = items.map { item ->
            val text = normalizeWords(item.title + " " + item.excerpt)
            Triple(item, text, polarity(text, positive, negative))
        }
        val out = mutableListOf<String>()
        for (i in prepared.indices) {
            for (j in i + 1 until prepared.size) {
                val a = prepared[i]; val b = prepared[j]
                if (a.first.source.equals(b.first.source, true)) continue
                if (a.third == 0 || b.third == 0 || a.third == b.third) continue
                val overlap = tokenOverlap(a.second, b.second)
                if (overlap >= 0.30) out += "تعارض بين ${a.first.source} و${b.first.source}: overlap=${"%.2f".format(overlap)}"
                if (out.size >= 10) return out.distinct()
            }
        }
        return out.distinct()
    }

    private fun polarity(text: Set<String>, positive: Set<String>, negative: Set<String>): Int {
        val pos = text.count { it in positive }; val neg = text.count { it in negative }
        return when { pos > neg -> 1; neg > pos -> -1; else -> 0 }
    }

    private fun tokenOverlap(a: Set<String>, b: Set<String>): Double {
        val common = a.intersect(b).count { it.length >= 4 }
        val base = minOf(a.count { it.length >= 4 }, b.count { it.length >= 4 }).coerceAtLeast(1)
        return common.toDouble() / base
    }

    private fun normalizeWords(text: String): Set<String> = text.lowercase()
        .replace(Regex("https?://\\S+"), " ")
        .replace(Regex("[^\\p{L}\\p{Nd} ]"), " ")
        .split(Regex("\\s+"))
        .filter { it.length >= 3 }
        .toSet()
}
