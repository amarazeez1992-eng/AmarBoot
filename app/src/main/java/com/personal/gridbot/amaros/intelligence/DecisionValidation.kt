package com.personal.gridbot.amaros.intelligence

/** Safety gate for future strategy outputs. This layer never executes trades. */
data class DecisionValidation(
    val allowed: Boolean,
    val reason: String,
    val demoOnly: Boolean = true
) {
    companion object {
        fun forDemo(): DecisionValidation = DecisionValidation(
            allowed = false,
            reason = "التنفيذ الحقيقي معطل في وضع DEMO"
        )
    }
}
