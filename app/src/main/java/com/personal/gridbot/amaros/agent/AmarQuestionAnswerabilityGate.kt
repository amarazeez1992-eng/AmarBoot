package com.personal.gridbot.amaros.agent

/**
 * Canonical candidate -> answerable-evidence boundary.
 *
 * This gate is binary by contract: a candidate is admitted only when the
 * requested subject and requested answer facet are both represented by the
 * same candidate. It never ranks or upgrades source authority.
 */
class AmarQuestionAnswerabilityGate {

    data class Decision(
        val admitted: Boolean,
        val reason: String
    )

    fun decide(question: String, title: String, evidence: String): Decision {
        if (question.isBlank() || title.isBlank() || evidence.isBlank()) {
            return Decision(false, "missing_question_or_candidate_content")
        }

        val profile = profile(question)
        val candidate = tokens(title + " " + evidence)

        if (profile.subject.isNotEmpty()) {
            val subjectHits = profile.subject.count { it in candidate }
            if (subjectHits < profile.subject.size) {
                return Decision(false, "candidate_subject_mismatch")
            }
        }

        if (profile.facets.isNotEmpty()) {
            val facetHits = profile.facets.count { it in candidate }
            if (facetHits < profile.facets.size) {
                return Decision(false, "requested_facet_not_answered")
            }
        }

        if (profile.subject.isEmpty() && profile.facets.isEmpty()) {
            return Decision(false, "question_profile_not_answerable")
        }

        return Decision(true, "subject_and_requested_facet_same_candidate")
    }

    private fun profile(question: String): Profile {
        val q = tokens(question)
        val facets = linkedSetOf<String>()

        when {
            q.any { it in setOf("كم", "عدد", "many", "much", "number", "count") } ->
                facets += setOf("number", "count", "age", "price", "value", "rate", "letters", "alphabet", "born")
            q.any { it in setOf("متى", "when") } -> facets += setOf("when", "date", "year", "born")
            q.any { it in setOf("لماذا", "ليش", "why") } -> facets += setOf("because", "reason", "why")
            q.any { it in setOf("كيف", "شلون", "how") } -> facets += setOf("how", "method", "process")
        }

        val directFacets = q.filter { it in FACETS }
        facets += directFacets

        val subject = q
            .filter { it !in STOP_WORDS && it !in FACETS && it !in QUANTITY_WORDS }
            .toCollection(linkedSetOf())

        return Profile(subject = subject, facets = facets)
    }

    private fun tokens(value: String): Set<String> =
        value.lowercase()
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
            .replace('ى', 'ي').replace('ة', 'ه')
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 2 }
            .map { normalizeAlias(it) }
            .toSet()

    private fun normalizeAlias(token: String): String = when (token) {
        "عاصمه", "عاصمة" -> "capital"
        "امريكا", "أمريكا" -> "america"
        "العربيه", "عربيه", "العربية" -> "arabic"
        "الانكليزيه", "انكليزيه", "الانجليزية", "انجليزية" -> "english"
        "احرف", "الأحرف", "الحروف" -> "letters"
        "شيرين" -> "sherine"
        "عمر" -> "age"
        else -> token
    }

    private data class Profile(val subject: Set<String>, val facets: Set<String>)

    companion object {
        private val QUANTITY_WORDS = setOf("كم", "عدد", "many", "much", "number", "count")
        private val STOP_WORDS = setOf(
            "ما","ماذا","هو","هي","هل","من","في","عن","الى","إلى","على","مع",
            "the","a","an","is","are","of","to","in","on","what","who","please","tell","me"
        )
        private val FACETS = setOf(
            "capital","عاصمه","عاصمة","age","عمر","born","birth",
            "letters","alphabet","احرف","الأحرف","الحروف",
            "price","value","rate","time","date","year",
            "reason","why","because","method","process","how"
        )
    }
}
