package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AmarGitHubToolRegistryTest {
    @Test fun readAndWriteAuthoritiesAreSeparated() {
        assertEquals(AmarAiToolRegistry.Authority.READ_ONLY, AmarAiToolRegistry.resolve("github_code_search")?.authority)
        assertEquals(AmarAiToolRegistry.Authority.READ_ONLY, AmarAiToolRegistry.resolve("github_license_inspect")?.authority)
        assertEquals(AmarAiToolRegistry.Authority.DRAFT_ONLY, AmarAiToolRegistry.resolve("github_file_update")?.authority)
        assertEquals(AmarAiToolRegistry.Authority.DRAFT_ONLY, AmarAiToolRegistry.resolve("github_file_delete")?.authority)
        assertEquals(AmarAiToolRegistry.Authority.DRAFT_ONLY, AmarAiToolRegistry.resolve("github_pull_request_merge")?.authority)
        assertFalse(AmarAiToolRegistry.isExecutionCapable("github_file_delete"))
        assertFalse(AmarAiToolRegistry.isExecutionCapable("github_pull_request_merge"))
    }
}
