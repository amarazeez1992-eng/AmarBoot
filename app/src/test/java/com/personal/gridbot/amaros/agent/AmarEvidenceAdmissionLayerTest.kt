package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceAdmissionLayerTest {
    private val admission = AmarEvidenceAdmissionLayer()
    private fun finding(title: String, evidence: String) = ResearchFinding(title, "https://example.test/source", evidence)

    @Test fun capital_question_rejects_unrelated_biography() {
        val r = admission.admit("ما عاصمة امريكا؟", listOf(finding("Emma Goldman", "American activist and writer in the United States.")))
        assertTrue(r.admitted.isEmpty()); assertTrue(r.rejected.size == 1)
    }

    @Test fun capital_question_accepts_matching_evidence() {
        val r = admission.admit("ما عاصمة امريكا؟", listOf(finding("Washington, D.C.", "Washington, D.C. is the capital of the United States.")))
        assertTrue(r.admitted.size == 1)
    }

    @Test fun age_question_rejects_other_singer_and_accepts_named_person() {
        val r = admission.admit("كم عمر الفنانة شيرين؟", listOf(finding("Another singer", "A singer biography and age information."), finding("Sherine biography", "Sherine was born in 1980 and is an Egyptian singer.")))
        assertTrue(r.admitted.size == 1); assertTrue(r.admitted.single().finding.sourceTitle.contains("Sherine"))
    }

    @Test fun letters_question_rejects_unrelated_country() {
        val r = admission.admit("كم عدد الاحرف العربية والانكليزية؟", listOf(finding("Estonia", "Estonia is a country in Northern Europe.")))
        assertTrue(r.admitted.isEmpty())
    }

    @Test fun empty_findings_fail_closed() {
        val r = admission.admit("ما عاصمة العراق؟", emptyList())
        assertTrue(r.admitted.isEmpty()); assertTrue(r.rejected.isEmpty())
    }

    @Test fun admission_is_deterministic() {
        val findings = listOf(finding("Washington, D.C.", "Washington, D.C. is the capital of the United States."), finding("Emma Goldman", "American activist and writer in the United States."))
        val first = admission.admit("ما عاصمة امريكا؟", findings)
        repeat(10) { assertTrue(first == admission.admit("ما عاصمة امريكا؟", findings)) }
        assertTrue(first.admitted.isNotEmpty())
    }
}
