package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarGitHubIntentParserTest {
    @Test fun repositoryUrlBecomesInspectionIntent() {
        val intent = AmarAiGitHubIntentParser.parse("افتح https://github.com/amarazeez1992-eng/AmarBoot")
        assertNotNull(intent)
        assertEquals(AmarAiGitHubIntent.Operation.REPO_INSPECT, intent!!.operation)
        assertEquals("amarazeez1992-eng", intent.owner)
        assertEquals("AmarBoot", intent.repo)
    }

    @Test fun searchAndLicenseRequestsAreRecognized() {
        val search = AmarAiGitHubIntentParser.parse("ابحث داخل مستودع openai/codex عن tokenizer")
        assertNotNull(search)
        assertEquals(AmarAiGitHubIntent.Operation.CODE_SEARCH, search!!.operation)
        val license = AmarAiGitHubIntentParser.parse("افحص ترخيص https://github.com/openai/codex")
        assertNotNull(license)
        assertEquals(AmarAiGitHubIntent.Operation.LICENSE_INSPECT, license!!.operation)
    }

    @Test fun destructiveRequestsBecomeExplicitWriteIntents() {
        val delete = AmarAiGitHubIntentParser.parse("احذف ملف README.md من مستودع owner/repo")
        assertTrue(delete != null)
        assertEquals(AmarAiGitHubIntent.Operation.FILE_DELETE, delete!!.operation)
        assertEquals("README.md", delete.path)
    }

    @Test fun parserIsIndependentFromAndroidJson() {
        val intent = AmarAiGitHubIntentParser.parse("read file README.md from owner/repo")
        assertNotNull(intent)
        assertEquals(AmarAiGitHubIntent.Operation.FILE_READ, intent!!.operation)
        assertEquals("owner", intent.owner)
        assertEquals("repo", intent.repo)
        assertEquals("README.md", intent.path)
    }
}
