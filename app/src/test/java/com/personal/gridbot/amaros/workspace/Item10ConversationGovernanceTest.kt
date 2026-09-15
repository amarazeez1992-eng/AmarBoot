package com.personal.gridbot.amaros.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Item10ConversationGovernanceTest {
    @Test
    fun linksAreExplicitAndCannotSelfLink() {
        val governance = AmarConversationGovernance()
        assertTrue(governance.link("a", "b", "topic"))
        assertFalse(governance.link("a", "a", "topic"))
        assertEquals(1, governance.linked("a").size)
    }

    @Test
    fun revisionHistoryIsRetained() {
        val governance = AmarConversationGovernance()
        assertTrue(governance.recordRevision(AmarMemoryRevision("m1", null, 10L, "validated update")))
        assertEquals(1, governance.revisions("m1").size)
    }

    @Test
    fun retentionMustBePositiveAndUserControlled() {
        val governance = AmarConversationGovernance()
        assertFalse(governance.setRetention(AmarRetentionPolicy(0, true)))
        assertFalse(governance.setRetention(AmarRetentionPolicy(30, false)))
        assertTrue(governance.setRetention(AmarRetentionPolicy(30, true)))
    }
}
