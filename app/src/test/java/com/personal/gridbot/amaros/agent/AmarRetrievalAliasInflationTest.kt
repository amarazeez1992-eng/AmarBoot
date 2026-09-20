package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarRetrievalAliasInflationTest {

    private val engine = AmarRetrievalRelevanceEngine()

    @Test
    fun america_aliases_count_as_one_query_term_not_four() {
        val result = engine.score(
            "اريد معرفة عاصمة امريكا",
            "Emma Goldman",
            "American activist and writer in the United States."
        )

        assertTrue(result.matchedTerms.contains("امريكا"))
        assertFalse(result.matchedTerms.contains("عاصمه"))
        assertFalse(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun matching_capital_and_country_remain_accepted() {
        val result = engine.score(
            "اريد معرفة عاصمة امريكا",
            "Washington, D.C. — Capital of the United States",
            "Washington, D.C. is the capital city of the United States."
        )

        assertTrue(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }
}
