package com.personal.gridbot.amaros.agent

/** Single boundary between retrieval candidates and evidence accepted by AMAR. */
class AmarEvidenceAdmissionLayer : AmarEvidenceAdmissionContract {
    override fun admit(question: String, findings: List<ResearchFinding>): AdmissionResult {
        if (findings.isEmpty()) return AdmissionResult(emptyList(), emptyList())
        val admitted = mutableListOf<AdmittedFinding>()
        val rejected = mutableListOf<AdmittedFinding>()
        findings.forEach { finding ->
            val a = assess(question, finding)
            val item = AdmittedFinding(finding, if (a.accepted) AdmissionState.ADMITTED else AdmissionState.REJECTED, a.score, a.reason)
            if (a.accepted) admitted += item else rejected += item
        }
        return AdmissionResult(admitted, rejected)
    }

    private fun assess(question: String, finding: ResearchFinding): Assessment {
        val q = tokenize(question)
        val e = tokenize(finding.sourceTitle + " " + finding.evidence)
        if (q.isEmpty() || e.isEmpty()) return Assessment(false, 0.0, "missing_query_or_evidence_tokens")
        val entities = q.filter { it.length >= 4 && it !in QUESTION_WORDS }
        val predicates = q.filter { it in PREDICATE_FACETS }
        val entityCoverage = coverage(entities, e)
        val predicateCoverage = coverage(predicates, e)
        val score = (entityCoverage * 0.60 + predicateCoverage * 0.40).coerceIn(0.0, 1.0)
        val accepted = when {
            entities.isNotEmpty() && predicates.isNotEmpty() -> entityCoverage >= 0.5 && predicateCoverage >= 0.5
            entities.isNotEmpty() -> entityCoverage >= 0.75
            else -> predicateCoverage >= 0.75
        }
        val reason = when {
            accepted -> "subject_and_requested_facet_supported"
            entityCoverage < 0.5 -> "requested_subject_not_supported"
            predicateCoverage < 0.5 && predicates.isNotEmpty() -> "requested_facet_not_supported"
            else -> "candidate_does_not_answer_question"
        }
        return Assessment(accepted, score, reason)
    }

    private fun coverage(tokens: List<String>, evidence: Set<String>): Double {
        if (tokens.isEmpty()) return 0.0
        return tokens.count { matches(it, evidence) }.toDouble() / tokens.size.toDouble()
    }

    private fun matches(token: String, evidence: Set<String>): Boolean = token in evidence || aliases(token).any { it in evidence }

    private fun aliases(token: String): Set<String> = when (token) {
        "عاصمه", "عاصمة" -> setOf("capital")
        "امريكا", "أمريكا" -> setOf("america", "united", "states", "usa")
        "الفنانه", "الفنانة" -> setOf("artist", "actress", "singer")
        "عمر" -> setOf("age", "born", "birth")
        "شيرين" -> setOf("sherine", "sherin")
        "احرف", "الأحرف", "الحروف" -> setOf("letters", "alphabet")
        "انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية" -> setOf("english")
        "عربيه", "العربيه", "العربية" -> setOf("arabic")
        "عدد", "كم" -> setOf("number", "count", "how", "many")
        else -> emptySet()
    }

    private fun tokenize(value: String): Set<String> = value.lowercase()
        .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ى', 'ي').replace('ة', 'ه').replace('ؤ', 'و').replace('ئ', 'ي')
        .split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.length >= 2 && it !in QUESTION_WORDS }
        .toSet()

    private data class Assessment(val accepted: Boolean, val score: Double, val reason: String)

    companion object {
        private val QUESTION_WORDS = setOf("اريد","أريد","معرفه","معرفة","ما","هو","هي","هل","من","في","عن","الى","إلى","على","the","a","an","is","are","of","to","in","on","what","who","how","many","please","tell","me","about")
        private val PREDICATE_FACETS = setOf("عاصمه","عاصمة","capital","عمر","age","born","birth","احرف","الأحرف","الحروف","letters","alphabet","عدد","number","count","how","many","سعر","price","value","rate","وقت","time","date")
    }
}
