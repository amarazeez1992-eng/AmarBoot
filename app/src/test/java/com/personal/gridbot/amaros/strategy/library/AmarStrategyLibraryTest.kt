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
        assertTrue(library.markTested("gold-grid", 1))
        assertTrue(library.approve("gold-grid", 1))
        assertEquals(AmarStrategyLibrary.Status.APPROVED, library.get("gold-grid", 1)?.status)
    }

    @Test
    fun approvedVersionCannotBeEdited() {
        val library = AmarStrategyLibrary()
        library.save(strategy())
        library.markTested("gold-grid", 1)
        library.approve("gold-grid", 1)
        assertFalse(library.update(strategy()))
    }

    @Test
    fun nextVersionIsCreatedAsDraft() {
        val library = AmarStrategyLibrary()
        library.save(strategy())
        val next = library.saveNextVersion("gold-grid", "Gold Grid v2", listOf("BUY_GRID", "TRAILING"))
        assertEquals(2, next.version)
        assertEquals(AmarStrategyLibrary.Status.DRAFT, next.status)
        assertEquals(2, library.list().size)
    }

    @Test
    fun copyCreatesIndependentDraft() {
        val library = AmarStrategyLibrary()
        library.save(strategy())
        val copy = library.copy("gold-grid", 1, "gold-grid-copy", "Gold Grid Copy")
        assertEquals(AmarStrategyLibrary.Status.DRAFT, copy.status)
        assertEquals(1, copy.version)
        assertEquals("Gold Grid Copy", library.get("gold-grid-copy", 1)?.name)
    }

    @Test
    fun archivedVersionCannotBeApproved() {
        val library = AmarStrategyLibrary()
        library.save(strategy())
        assertTrue(library.markTested("gold-grid", 1))
        assertTrue(library.archive("gold-grid", 1))
        assertFalse(library.approve("gold-grid", 1))
    }
}
