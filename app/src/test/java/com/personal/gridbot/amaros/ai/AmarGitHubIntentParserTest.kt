package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarGitHubIntentParserTest {
    @Test fun repositoryUrlBecomesInspectionIntent() {
        val intent = AmarAiGitHubIntentParser.parse("افتح https://github.com/amarazeez1992-eng/AmarBoot")
        assertNotNull(intent)
        assertEquals("repo_inspect", intent!!.getString("operation"))
        assertEquals("amarazeez1992-eng", intent.getString("owner"))
        assertEquals("AmarBoot", intent.getString("repo"))
    }

    @Test fun searchAndLicenseRequestsAreRecognized() {
        val search = AmarAiGitHubIntentParser.parse("ابحث داخل مستودع openai/codex عن tokenizer")
        assertNotNull(search)
        assertEquals("code_search", search!!.getString("operation"))
        val license = AmarAiGitHubIntentParser.parse("افحص ترخيص https://github.com/openai/codex")
        assertNotNull(license)
        assertEquals("license_inspect", license!!.getString("operation"))
    }

    @Test fun destructiveRequestsBecomeExplicitWriteIntents() {
        val delete = AmarAiGitHubIntentParser.parse("احذف ملف README.md من مستودع owner/repo")
        assertTrue(delete != null)
        assertEquals("file_delete", delete!!.getString("operation"))
        assertEquals("README.md", delete.getString("path"))
    }
}
