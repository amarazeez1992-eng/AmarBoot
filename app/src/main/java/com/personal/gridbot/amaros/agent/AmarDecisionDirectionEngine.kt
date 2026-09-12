package com.personal.gridbot.amaros.agent

/** Extracts an explicit market direction without treating negated prose as authority. */
class AmarDecisionDirectionEngine {
    fun detect(text: String): AmarDecisionDirection {
        val normalized = text.lowercase()
        val buy = hasAction(normalized, BUY_TERMS, NEGATED_BUY_TERMS)
        val sell = hasAction(normalized, SELL_TERMS, NEGATED_SELL_TERMS)
        val hold = containsAny(normalized, *HOLD_TERMS)

        return when {
            buy && !sell && !hold -> AmarDecisionDirection.BUY
            sell && !buy && !hold -> AmarDecisionDirection.SELL
            hold && !buy && !sell -> AmarDecisionDirection.HOLD
            else -> AmarDecisionDirection.UNKNOWN
        }
    }

    private fun hasAction(text: String, positiveTerms: Array<String>, negatedTerms: Array<String>): Boolean =
        containsAny(text, *positiveTerms) && !containsAny(text, *negatedTerms)

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any(text::contains)

    private companion object {
        val BUY_TERMS = arrayOf("buy", "شراء", "شراءً", "شراء الآن")
        val SELL_TERMS = arrayOf("sell", "بيع", "بيعاً", "بيع الآن")
        val HOLD_TERMS = arrayOf("hold", "انتظار", "محايد", "لا تدخل")
        val NEGATED_BUY_TERMS = arrayOf("لا أنصح بالشراء", "لا أوصي بالشراء", "لا تدخل شراء", "not buy", "do not buy", "don't buy")
        val NEGATED_SELL_TERMS = arrayOf("لا أنصح بالبيع", "لا أوصي بالبيع", "لا تدخل بيع", "not sell", "do not sell", "don't sell")
    }
}
