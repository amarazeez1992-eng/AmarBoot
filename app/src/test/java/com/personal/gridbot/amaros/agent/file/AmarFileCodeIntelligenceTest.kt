package com.personal.gridbot.amaros.agent.file

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarFileCodeIntelligenceTest {
    @Test fun detects_language_symbols_dependencies_and_hash() {
        val result = AmarFileCodeIntelligence.analyze(
            AmarFileCodeIntelligence.FileInput(
                "src/Main.kt",
                "import kotlin.math.abs\nclass Main {\n    // TODO: test\n    fun run() = abs(1)\n}"
            )
        )
        assertEquals(AmarFileCodeIntelligence.Language.KOTLIN, result.language)
        assertTrue(result.symbols.any { it.contains("class Main") })
        assertTrue(result.dependencies.any { it.contains("kotlin.math.abs") })
        assertTrue(result.findings.any { it.category == "maintenance" && it.line == 3 })
        assertEquals(64, result.sha256.length)
    }

    @Test fun secret_detection_is_possible_not_confirmed() {
        val result = AmarFileCodeIntelligence.analyze(
            AmarFileCodeIntelligence.FileInput("config.py", "api_key = 'example-value'")
        )
        val finding = result.findings.first { it.category == "security" }
        assertEquals(AmarFileCodeIntelligence.Confidence.POSSIBLE, finding.confidence)
        assertTrue(AmarFileCodeIntelligence.safeRepairProposal(result).isNotEmpty())
    }

    @Test fun comparison_pinpoints_changed_lines_and_does_not_write() {
        val before = AmarFileCodeIntelligence.FileInput("a.txt", "one\ntwo\nthree")
        val after = AmarFileCodeIntelligence.FileInput("a.txt", "one\nTWO\nthree\nfour")
        val diff = AmarFileCodeIntelligence.compare(before, after)
        assertTrue(diff.any { it.category == "change" })
        assertTrue(diff.any { it.category == "diff" && it.line == 2 })
        assertTrue(diff.any { it.category == "diff" && it.line == 4 })
    }

    @Test fun malformed_json_is_reported_without_claiming_parser_success() {
        val result = AmarFileCodeIntelligence.analyze(
            AmarFileCodeIntelligence.FileInput("data.json", "not-json")
        )
        assertTrue(result.findings.any { it.category == "syntax" })
    }
}
