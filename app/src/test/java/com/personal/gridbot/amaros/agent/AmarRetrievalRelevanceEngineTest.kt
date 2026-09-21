package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarRetrievalRelevanceEngineTest {

    private val engine = AmarRetrievalRelevanceEngine()

    @Test
    fun unrelated_result_is_rejected() {
        val result = engine.score(
            "اريد معرفة عاصمة امريكا",
            "Emma Goldman",
            "Biography and historical activism in the United States."
        )
        assertFalse(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun relevant_capital_result_is_accepted() {
        val result = engine.score(
            "اريد معرفة عاصمة امريكا",
            "Washington, D.C. — Capital of the United States",
            "Washington, D.C. is the capital city of the United States."
        )
        assertTrue(engine.accept(
            "اريد معرفة عاصمة امريكا",
            "Washington, D.C. — Capital of the United States",
            "Washington, D.C. is the capital city of the United States."
        ).accepted)
        assertTrue(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun unrelated_country_result_is_rejected_for_letter_question() {
        val result = engine.score(
            "كم عدد الاحرف العربية والانكليزية",
            "Estonia",
            "Estonia is a country in Northern Europe."
        )
        assertFalse(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun relevant_age_result_is_accepted() {
        val result = engine.score(
            "كم عمر الفنانة شيرين",
            "Sherine — age and biography",
            "Sherine was born in 1980 and is an Egyptian singer."
        )
        assertTrue(engine.accept(
            "كم عمر الفنانة شيرين",
            "Sherine — age and biography",
            "Sherine was born in 1980 and is an Egyptian singer."
        ).accepted)
        assertTrue(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun score_below_threshold_rejects() {
        val decision = engine.accept(
            "ما عاصمة امريكا",
            "America",
            "capital of"
        )
        assertFalse(decision.accepted)
        assertEquals(
            AmarRetrievalRelevanceEngine.RejectionReason.SCORE_BELOW_THRESHOLD,
            decision.reason
        )
    }

    @Test
    fun score_above_threshold_accepts() {
        val decision = engine.accept(
            "ما عاصمة امريكا",
            "Washington capital of the United States",
            "Washington is the capital of the United States."
        )
        assertTrue(decision.accepted)
        assertEquals(null, decision.reason)
        assertTrue(decision.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun facet_missing_rejects_with_required_facet_missing() {
        val decision = engine.accept(
            "السعر الحالي للذهب",
            "السعر الحالي الذهب",
            "Gold information without a value term."
        )
        assertFalse(decision.accepted)
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.REQUIRED_FACET_MISSING, decision.reason)
    }

    @Test
    fun entity_anchor_mismatch_rejects() {
        val decision = engine.accept(
            "كم عمر شيرين",
            "Adele age how",
            "Adele was born in 1988."
        )
        assertFalse(decision.accepted)
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.ENTITY_ANCHOR_MISMATCH, decision.reason)
    }

    @Test
    fun current_value_requires_current_facet() {
        val decision = engine.accept(
            "السعر الحالي للذهب",
            "السعر الذهب price",
            "Gold price information."
        )
        assertFalse(decision.accepted)
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.REQUIRED_FACET_MISSING, decision.reason)
    }

    @Test
    fun age_question_requires_age_facet() {
        val decision = engine.accept(
            "كم عمر شيرين",
            "Sherine number",
            "Sherine how many details."
        )
        assertFalse(decision.accepted)
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.REQUIRED_FACET_MISSING, decision.reason)
    }

    @Test
    fun capital_question_requires_capital_facet() {
        val decision = engine.accept(
            "ما عاصمة امريكا",
            "America USA",
            "Information about the United States."
        )
        assertFalse(decision.accepted)
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.REQUIRED_FACET_MISSING, decision.reason)
    }

    @Test
    fun accept_is_deterministic() {
        val q = "ما عاصمة امريكا"
        val t = "Washington capital of the United States"
        val e = "Washington is the capital of the United States."
        assertEquals(engine.accept(q, t, e), engine.accept(q, t, e))
    }

    @Test
    fun accept_returns_score_alongside_decision() {
        val q = "ما عاصمة امريكا"
        val t = "Washington capital of the United States"
        val e = "Washington is the capital of the United States."
        val decision = engine.accept(q, t, e)
        assertEquals(engine.score(q, t, e).score, decision.score, 0.0)
    }

    @Test
    fun score_is_independent_of_gates() {
        val q = "السعر الحالي للذهب"
        val t = "السعر الذهب price"
        val e = "Gold price information."
        val scored = engine.score(q, t, e)
        val decision = engine.accept(q, t, e)
        assertEquals(scored.score, decision.score, 0.0)
        assertFalse(decision.accepted)
        assertEquals(
            AmarRetrievalRelevanceEngine.RejectionReason.REQUIRED_FACET_MISSING,
            decision.reason
        )
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.REQUIRED_FACET_MISSING, decision.reason)
    }
}
