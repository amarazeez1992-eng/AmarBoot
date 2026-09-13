package com.personal.gridbot.amaros.strategy.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStrategyLibraryTest {
    private fun strategy(status: AmarStrategyLibrary.Status = AmarStrategyLibrary.Status.DRAFT) =
        AmarStrategyLibrary.StrategyVersion("gold-grid", 1, "Gold Grid", listOf("BUY_GRID"), status)

    @Test
    fun saveAndRetrieveVersion() {
        val library = AmarStrategyLibrary()
        assertTrue(library.save(strategy()))
        assertFalse(library.save(strategy()))
        assertEquals(strategy(), library.get("gold-grid", 1))
    }

    @Test
    fun approvalRequiresTestedState() {
        val library = AmarStrategyLibrary()
        library.save(strategy())
        assertFalse(library.approve("gold-grid", 1))
        assertTrue(library.update(strategy(AmarStrategyLibrary.Status.TESTED)))
        assertTrue(library.approve("gold-grid", 1))
        assertEquals(AmarStrategyLibrary.Status.APPROVED, library.get("gold-grid", 1)?.status)
    }

    @Test
    fun copyCreatesIndependentDraft() {
        val library = AmarStrategyLibrary()
        library.save(strategy())
        val copy = library.copy("gold-grid", 1, "gold-grid-v2", "Gold Grid Copy")
        assertEquals(AmarStrategyLibrary.Status.DRAFT, copy.status)
        assertEquals(1, copy.version)
        assertEquals("Gold Grid Copy", library.get("gold-grid-v2", 1)?.name)
    }

    @Test
    fun archivedVersionCannotBeApproved() {
        val library = AmarStrategyLibrary()
        library.save(strategy(AmarStrategyLibrary.Status.TESTED))
        assertTrue(library.archive("gold-grid", 1))
        assertFalse(library.approve("gold-grid", 1))
    }
}
