package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarGitHubIntentParserTest {
    @Test fun repositoryUrlBecomesInspectionIntent() {
        val intent = AmarAiGitHubIntentParser.parse("افتح https://github.com/amarazeez1992-eng/AmarBoot")
        assertNotNull(intent)
        assertEquals("repo_inspect", intent!!.operation)
        assertEquals("amarazeez1992-eng", intent.owner)
        assertEquals("AmarBoot", intent.repo)
    }

    @Test fun searchAndLicenseRequestsAreRecognized() {
        val search = AmarAiGitHubIntentParser.parse("ابحث داخل مستودع openai/codex عن tokenizer")
        assertNotNull(search)
        assertEquals("code_search", search!!.operation)
        val license = AmarAiGitHubIntentParser.parse("افحص ترخيص https://github.com/openai/codex")
        assertNotNull(license)
        assertEquals("license_inspect", license!!.operation)
    }

    @Test fun destructiveRequestsBecomeExplicitWriteIntents() {
        val delete = AmarAiGitHubIntentParser.parse("احذف ملف README.md من مستودع owner/repo")
        assertTrue(delete != null)
        assertEquals("file_delete", delete!!.operation)
        assertEquals("README.md", delete.path)
    }
}
