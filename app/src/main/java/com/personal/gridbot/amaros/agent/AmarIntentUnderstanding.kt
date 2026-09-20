package com.personal.gridbot.amaros.agent

/**
 * AMAR-native request understanding layer.
 * Deterministic linguistic parsing only; no hosted AI or external model.
 */
class AmarIntentUnderstanding {
    fun understand(text: String): AmarIntentAnalysis {
        require(text.isNotBlank())
        val normalized = normalize(text)
        if (isGreeting(normalized)) return AmarIntentAnalysis(AgentIntent.GENERAL, 1.0, emptyList(), "greeting")

        val signals = mutableListOf<Pair<AgentIntent, Double>>()
        val trade = score(normalized, setOf("تداول","صفقة","mt5","trade","gold","ذهب","forex","سوق","market","شراء","بيع","سعر"))
        val strategy = score(normalized, setOf("استراتيجية","strategy","روبوت","bot","خطة","قواعد","مؤشر","indicator","backtest"))
        val research = score(normalized, setOf("ابحث","بحث","research","مصادر","دليل","دراسة","اشرح","ما هو","لماذا","كيف","تحقق","verify"))
        if (trade > 0.0) signals += AgentIntent.TRADE_ANALYSIS to trade
        if (strategy > 0.0) signals += AgentIntent.STRATEGY_DESIGN to strategy
        if (research > 0.0) signals += AgentIntent.RESEARCH to research

        val chosen = signals.maxByOrNull { it.second } ?: (AgentIntent.RESEARCH to 0.20)
        val competitors = signals.filter { it.first != chosen.first }.sumOf { it.second }
        val confidence = (chosen.second / (chosen.second + competitors + 0.25)).coerceIn(0.20, 0.98)

        val questionForm = when {
            normalized.startsWith("لماذا") || normalized.startsWith("why") -> "WHY"
            normalized.startsWith("كيف") || normalized.startsWith("how") -> "HOW"
            normalized.startsWith("هل") || normalized.startsWith("is ") || normalized.startsWith("can ") -> "YES_NO"
            normalized.contains("متى") || normalized.contains("when") -> "WHEN"
            normalized.contains("كم") || normalized.contains("how much") || normalized.contains("how many") -> "QUANTITY"
            else -> "OPEN"
        }

        return AmarIntentAnalysis(
            intent = chosen.first,
            confidence = confidence,
            signals = signals.sortedByDescending { it.second }.map { pair ->
                pair.first.name + ":" + String.format(java.util.Locale.US, "%.2f", pair.second)
            },
            questionForm = questionForm
        )
    }

    private fun score(text: String, concepts: Set<String>): Double {
        val hits = concepts.count { text.contains(it) }
        val questionBoost = if (text.contains("?") || text.contains("؟")) 0.10 else 0.0
        return hits * 0.35 + questionBoost
    }

    private fun normalize(text: String): String =
        text.lowercase()
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
            .replace('ة', 'ه')
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun isGreeting(text: String): Boolean =
        setOf("هلو","مرحبا","مرحباً","السلام عليكم","hello","hi","hey").any { text == it || text.startsWith("$it ") }
}

data class AmarIntentAnalysis(
    val intent: AgentIntent,
    val confidence: Double,
    val signals: List<String>,
    val questionForm: String
)
