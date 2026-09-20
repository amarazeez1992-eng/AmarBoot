package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarQuestionAnswerabilityGateTest {
    private val gate = AmarQuestionAnswerabilityGate()

    @Test fun capital_question_rejects_emma_goldman() {
        val d = gate.decide(
            "ما عاصمة امريكا؟",
            "Emma Goldman",
            "American activist and writer in the United States."
        )
        assertFalse(d.admitted)
    }

    @Test fun capital_question_accepts_washington() {
        val d = gate.decide(
            "ما عاصمة امريكا؟",
            "Washington, D.C.",
            "Washington, D.C. is the capital of the United States."
        )
        assertTrue(d.admitted)
    }

    @Test fun age_question_requires_same_subject() {
        val unrelated = gate.decide(
            "كم عمر الفنانة شيرين؟",
            "Another singer",
            "A singer biography contains age information."
        )
        val matching = gate.decide(
            "كم عمر الفنانة شيرين؟",
            "Sherine biography",
            "Sherine was born in 1980 and is an Egyptian singer."
        )
        assertFalse(unrelated.admitted)
        assertTrue(matching.admitted)
    }

    @Test fun letters_question_rejects_estonia() {
        val d = gate.decide(
            "كم عدد الاحرف العربية والانكليزية؟",
            "Estonia",
            "Estonia is a country in Northern Europe."
        )
        assertFalse(d.admitted)
    }

    @Test fun missing_candidate_content_fails_closed() {
        assertFalse(gate.decide("ما عاصمة العراق؟", "", "Baghdad is the capital of Iraq.").admitted)
        assertFalse(gate.decide("ما عاصمة العراق؟", "Baghdad", "").admitted)
    }
}
