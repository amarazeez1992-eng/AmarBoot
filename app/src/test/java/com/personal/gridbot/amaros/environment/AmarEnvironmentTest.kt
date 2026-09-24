package com.personal.gridbot.amaros.environment

import com.personal.gridbot.BuildConfig
import org.junit.Assert.assertEquals
import kotlin.test.assertFailsWith
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEnvironmentTest {
    @Test
    fun debugBuildResolvesToDebugEnvironment() {
        assertTrue(BuildConfig.DEBUG)
        assertEquals(AmarEnvironment.DEBUG, AmarEnvironment.current())
    }

    @Test
    fun productionEnvironmentIsRejectedInDebugToPreventCredentialMixing() {
        assertFailsWith<IllegalStateException> {
            AmarEnvironmentGuard.requireEnvironment(AmarEnvironment.PROD)
        }
    }

    @Test
    fun debugEnvironmentIsAcceptedInDebug() {
        AmarEnvironmentGuard.requireEnvironment(AmarEnvironment.DEBUG)
    }
}
