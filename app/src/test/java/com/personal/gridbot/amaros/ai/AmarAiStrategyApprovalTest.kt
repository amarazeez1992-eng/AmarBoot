package com.personal.gridbot.amaros.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmarAiStrategyApprovalTest {
    @Test fun saveCannotSilentlyCreateApprovedStrategy() {
        val repo = AmarAiStrategyNotesRepository(ApplicationProvider.getApplicationContext<Context>())
        val name = "TEST-${System.currentTimeMillis()}"
        assertNull(repo.save(name, "test strategy", "APPROVED"))
        val draft = repo.save(name, "test strategy", "DRAFT")
        assertEquals("DRAFT", draft?.status)
        val approved = repo.approve(name)
        assertEquals("APPROVED", approved?.status)
        repo.delete(name)
    }
}
