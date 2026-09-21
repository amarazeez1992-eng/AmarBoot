package com.personal.gridbot.amaros.agent

/**
 * Query-aware retrieval gate.
 *
 * Retrieval relevance is a pre-verification boundary: evidence that does not
 * materially match the user's question must not enter authority/consensus
 * scoring merely because its publisher is trustworthy.
 *
 * This is a deterministic lexical/entity-aware gate, not the final intelligence
 * layer. It is deliberately conservative: when relevance cannot be established,
 * the result is rejected rather than promoted as evidence.
 */
class AmarRetrievalRelevanceEngine {

    data class ScoredResult(
        val score: Double,
        val matchedTerms: Set<String>,
        val formMatched: Boolean = true,
        val facetsMatched: Boolean = true,
        val entityAnchorMatched: Boolean = true,
        val temporalMatched: Boolean = true,
        val rejectionReason: RejectionReason? = null
    )

    data class QuestionProfile(
        val entity: String?,
        val questionForm: QuestionForm,
        val requiredFacets: Set<RequiredFacet>,
        val temporalFlag: Boolean
    )

    enum class QuestionForm { CAPITAL, AGE, CURRENT_VALUE, QUANTITY, GENERAL }

    enum class RequiredFacet { CAPITAL_OF, AGE, CURRENT, VALUE, QUANTITY }

    enum class RejectionReason {
        SCORE_BELOW_THRESHOLD,
        QUESTION_FORM_MISMATCH,
        REQUIRED_FACET_MISSING,
        ENTITY_ANCHOR_MISMATCH,
        TEMPORAL_MISMATCH
    }

    fun score(question: String, title: String, excerpt: String): ScoredResult {
        val questionTerms = tokensForMatching(question)
        if (questionTerms.isEmpty()) {
            return ScoredResult(0.0, emptySet(), rejectionReason = RejectionReason.SCORE_BELOW_THRESHOLD)
        }

        val titleTerms = tokenize(title)
        val bodyTerms = tokenize(excerpt)
        val evidenceTerms = titleTerms + bodyTerms

        fun termMatches(term: String, terms: Set<String>): Boolean =
            term in terms || aliasesFor(term).any { it in terms }

        val matched = questionTerms.filter { termMatches(it, evidenceTerms) }.toSet()
        val titleMatched = questionTerms.filter { termMatches(it, titleTerms) }.toSet()

        val coverage = matched.size.toDouble() / questionTerms.size.toDouble()
        val titleCoverage = titleMatched.size.toDouble() / questionTerms.size.toDouble()
        val exactPhrase = normalized(question).let { q ->
            q.length >= 5 && (
                normalized(title).contains(q) ||
                    normalized(excerpt).contains(q)
                )
        }

        val entityTerms = questionTerms.filter { it.length >= 4 }.toSet()
        val entityMatched = entityTerms.count { termMatches(it, evidenceTerms) }
        val entityCoverage = if (entityTerms.isEmpty()) 0.0 else
            entityMatched.toDouble() / entityTerms.size.toDouble()

        val score = (
            coverage * 0.45 +
                titleCoverage * 0.25 +
                entityCoverage * 0.25 +
                if (exactPhrase) 0.05 else 0.0
            ).coerceIn(0.0, 1.0)

        val profile = extractProfile(question)
        val formMatched = formGate(profile, title, excerpt)
        val facetsMatched = facetGate(profile, title, excerpt)
        val entityAnchorMatched = entityAnchorGate(profile, title, excerpt)
        val temporalMatched = temporalGate(profile, title, excerpt)

        val rejectionReason = when {
            score < MIN_RELEVANCE_SCORE -> RejectionReason.SCORE_BELOW_THRESHOLD
            !formMatched -> RejectionReason.QUESTION_FORM_MISMATCH
            !facetsMatched -> RejectionReason.REQUIRED_FACET_MISSING
            !entityAnchorMatched -> RejectionReason.ENTITY_ANCHOR_MISMATCH
            !temporalMatched -> RejectionReason.TEMPORAL_MISMATCH
            else -> null
        }

        return ScoredResult(
            score = score,
            matchedTerms = matched,
            formMatched = formMatched,
            facetsMatched = facetsMatched,
            entityAnchorMatched = entityAnchorMatched,
            temporalMatched = temporalMatched,
            rejectionReason = rejectionReason
        )
    }

    fun accept(question: String, title: String, excerpt: String): Boolean =
        score(question, title, excerpt).rejectionReason == null

    private fun extractProfile(question: String): QuestionProfile {
        val normalizedQuestion = normalized(question)
        val form = when {
            containsAny(normalizedQuestion, AGE_PATTERNS) -> QuestionForm.AGE
            containsAny(normalizedQuestion, CAPITAL_PATTERNS) -> QuestionForm.CAPITAL
            containsAny(normalizedQuestion, CURRENT_PATTERNS) -> QuestionForm.CURRENT_VALUE
            containsAny(normalizedQuestion, QUANTITY_PATTERNS) -> QuestionForm.QUANTITY
            else -> QuestionForm.GENERAL
        }

        val requiredFacets = when (form) {
            QuestionForm.CAPITAL -> setOf(RequiredFacet.CAPITAL_OF)
            QuestionForm.AGE -> setOf(RequiredFacet.AGE)
            QuestionForm.CURRENT_VALUE -> setOf(RequiredFacet.CURRENT, RequiredFacet.VALUE)
            QuestionForm.QUANTITY -> setOf(RequiredFacet.QUANTITY)
            QuestionForm.GENERAL -> emptySet()
        }

        val temporalFlag = containsAny(normalizedQuestion, TEMPORAL_PATTERNS)
        val entity = extractEntity(normalizedQuestion, form)
        return QuestionProfile(entity, form, requiredFacets, temporalFlag)
    }

    private fun formGate(profile: QuestionProfile, title: String, excerpt: String): Boolean {
        val terms = tokenize("$title $excerpt")
        return when (profile.questionForm) {
            QuestionForm.CAPITAL -> containsAny(terms, CAPITAL_EVIDENCE_TERMS)
            QuestionForm.AGE -> containsAny(terms, AGE_EVIDENCE_TERMS)
            QuestionForm.CURRENT_VALUE -> containsAny(terms, CURRENT_EVIDENCE_TERMS + VALUE_EVIDENCE_TERMS)
            QuestionForm.QUANTITY -> containsAny(terms, QUANTITY_EVIDENCE_TERMS)
            QuestionForm.GENERAL -> true
        }
    }

    private fun facetGate(profile: QuestionProfile, title: String, excerpt: String): Boolean {
        val terms = tokenize("$title $excerpt")
        return profile.requiredFacets.all { facet ->
            when (facet) {
                RequiredFacet.CAPITAL_OF -> containsAny(terms, CAPITAL_EVIDENCE_TERMS)
                RequiredFacet.AGE -> containsAny(terms, AGE_EVIDENCE_TERMS)
                RequiredFacet.CURRENT -> containsAny(terms, CURRENT_EVIDENCE_TERMS)
                RequiredFacet.VALUE -> containsAny(terms, VALUE_EVIDENCE_TERMS)
                RequiredFacet.QUANTITY -> containsAny(terms, QUANTITY_EVIDENCE_TERMS)
            }
        }
    }

    private fun entityAnchorGate(profile: QuestionProfile, title: String, excerpt: String): Boolean {
        val entity = profile.entity ?: return true
        val evidenceTerms = tokenize("$title $excerpt")
        return entity in evidenceTerms || aliasesFor(entity).any { it in evidenceTerms }
    }

    private fun temporalGate(profile: QuestionProfile, title: String, excerpt: String): Boolean {
        if (!profile.temporalFlag) return true
        return containsAny(tokenize("$title $excerpt"), CURRENT_EVIDENCE_TERMS)
    }

    private fun extractEntity(question: String, form: QuestionForm): String? {
        val excluded = STOP_WORDS + QUESTION_WORDS + FACET_WORDS + FORM_WORDS
        val formSpecific = when (form) {
            QuestionForm.CAPITAL -> setOf("عاصمه", "capital")
            QuestionForm.AGE -> setOf("عمر", "age", "born", "birth")
            QuestionForm.CURRENT_VALUE -> setOf("حالي", "الحالي", "الان", "current", "latest")
            QuestionForm.QUANTITY -> setOf("عدد", "كم", "number", "count")
            QuestionForm.GENERAL -> emptySet()
        }
        return question
            .split(Regex("[^\p{L}\p{N}]+"))
            .map { it.trim() }
            .filter { it.length >= 2 && it !in excluded && it !in formSpecific }
            .maxByOrNull { it.length }
    }

    private fun tokensForMatching(question: String): Set<String> = tokenize(question)\n\n    private fun containsAny(value: String, patterns: Set<String>): Boolean =
        patterns.any { value.contains(it) }

    private fun containsAny(value: Set<String>, terms: Set<String>): Boolean =
        value.any { it in terms }

    private fun aliasesFor(token: String): Set<String> =
        when (token) {
            "عاصمه" -> setOf("capital")
            "امريكا" -> setOf("america", "united", "states", "usa")
            "الفنانه" -> setOf("artist", "actress", "singer")
            "عمر" -> setOf("age", "born", "birth")
            "شيرين" -> setOf("sherine", "sherin")
            "احرف", "الاحرف", "الحروف" -> setOf("letters", "alphabet")
            "انكليزيه", "الانكليزيه", "الانجليزية", "انجليزية" -> setOf("english")
            "عربيه", "العربيه", "العربية" -> setOf("arabic")
            "عدد", "كم" -> setOf("number", "count", "how")
            else -> emptySet()
        }

    private fun tokenize(value: String): Set<String> =
        normalized(value)
            .split(Regex("[^\p{L}\p{N}]+"))
            .map { it.trim() }
            .filter { it.length >= 2 && it !in STOP_WORDS }
            .toSet()

    private fun normalized(value: String): String =
        value.lowercase()
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ى', 'ي')
            .replace('ة', 'ه')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace(Regex("\s+"), " ")
            .trim()

    companion object {
        const val MIN_RELEVANCE_SCORE = 0.45

        private val AGE_PATTERNS = setOf("عمر", "age", "born", "birth")
        private val CAPITAL_PATTERNS = setOf("عاصمه", "capital")
        private val CURRENT_PATTERNS = setOf("حالي", "الحالي", "الان", "current", "latest")
        private val QUANTITY_PATTERNS = setOf("كم عدد", "عدد", "how many", "number", "count")
        private val TEMPORAL_PATTERNS = CURRENT_PATTERNS

        private val CAPITAL_EVIDENCE_TERMS = setOf("capital", "عاصمه")
        private val AGE_EVIDENCE_TERMS = setOf("age", "born", "birth", "عمر")
        private val CURRENT_EVIDENCE_TERMS = setOf("current", "latest", "now", "today", "حالي", "الان")
        private val VALUE_EVIDENCE_TERMS = setOf("price", "value", "سعر", "قيمه", "القيمه")
        private val QUANTITY_EVIDENCE_TERMS = setOf("number", "count", "quantity", "how", "عدد", "كم")

        private val QUESTION_WORDS = setOf(
            "ما", "ماذا", "من", "هل", "كيف", "كم", "what", "who", "how", "many",
            "which", "where", "when", "why", "please", "tell", "me", "about"
        )

        private val FACET_WORDS = setOf(
            "عاصمه", "عاصمة", "capital", "عمر", "age", "born", "birth",
            "عدد", "كم", "number", "count", "quantity",
            "سعر", "قيمه", "القيمه", "price", "value",
            "الفنانه", "الفنانة", "artist", "actress", "singer"
        )

        private val FORM_WORDS = setOf(
            "حالي", "الحالي", "الان", "الآن", "current", "latest", "now", "today"
        )

        private val STOP_WORDS = setOf(
            "اريد", "أريد", "معرفه", "معرفة", "عن", "ما", "هو", "هي", "هل",
            "من", "في", "الى", "إلى", "على", "هذا", "هذه", "ذلك", "تلك",
            "the", "a", "an", "is", "are", "of", "to", "in", "on", "what",
            "who", "how", "many", "please", "tell", "me", "about"
        )
    }
}
