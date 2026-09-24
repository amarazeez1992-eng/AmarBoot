package com.personal.gridbot.amaros.version

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarVersionTest {
    @Test
    fun versionConstantsMatchCurrentBuild() {
        assertEquals(BuildConfig.VERSION_NAME, AmarVersion.APP_VERSION)
        assertEquals("1.0", AmarVersion.APP_VERSION)
        assertEquals(1, AmarVersion.SCHEMA_VERSION)
        assertEquals(1, AmarVersion.CONTRACT_VERSION)
    }

    @Test
    fun mismatchIsRejected() {
        assertFalse(AmarVersion.isCompatible(AmarVersion.SCHEMA_VERSION + 1, AmarVersion.CONTRACT_VERSION))
        assertFalse(AmarVersion.isCompatible(AmarVersion.SCHEMA_VERSION, AmarVersion.CONTRACT_VERSION + 1))
        assertTrue(AmarVersion.isCompatible(AmarVersion.SCHEMA_VERSION, AmarVersion.CONTRACT_VERSION))
    }
}
