package com.personal.gridbot.amaros.agent

/** Extracts an explicit market direction from model output without treating prose as authority. */
class AmarDecisionDirectionEngine {
    fun detect(text: String): AmarDecisionDirection {
        val normalized = text.lowercase()
        val buy = containsAny(normalized, "buy", "شراء", "شراءً", "شراء الآن")
        val sell = containsAny(normalized, "sell", "بيع", "بيعاً", "بيع الآن")
        val hold = containsAny(normalized, "hold", "انتظار", "محايد", "لا تدخل")

        return when {
            buy && !sell && !hold -> AmarDecisionDirection.BUY
            sell && !buy && !hold -> AmarDecisionDirection.SELL
            hold && !buy && !sell -> AmarDecisionDirection.HOLD
            else -> AmarDecisionDirection.UNKNOWN
        }
    }

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any(text::contains)
}
