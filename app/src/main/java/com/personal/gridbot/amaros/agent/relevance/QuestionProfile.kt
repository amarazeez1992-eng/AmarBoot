package com.personal.gridbot.amaros.agent.relevance

/**
 * Placeholder question model for Step 3.1.
 *
 * The profile establishes the future Question Relevance contract boundary but
 * does not participate in scoring in Step 3.1. Semantic use is deferred to
 * Step 3.2.
 */
data class QuestionProfile(
    val question: String,
    val form: QuestionForm,
    val requiredFacets: Set<RequiredFacet>
) {
    init {
        require(question.isNotBlank()) { "question must not be blank" }
    }
}

enum class QuestionForm {
    GENERAL,
    CAPITAL,
    AGE,
    QUANTITY,
    CURRENT_VALUE
}

enum class RequiredFacet {
    ENTITY,
    QUESTION_FORM,
    QUANTITY,
    CURRENT_VALUE
}
