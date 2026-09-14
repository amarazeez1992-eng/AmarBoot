package com.personal.gridbot.amaros.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarSecurityContractsTest {
    @Test
    fun allowsHttpsSyncEndpoints() {
        assertTrue(AmarSecurityContracts.isAllowedSyncEndpoint("https://sync.example.com/api"))
    }

    @Test
    fun allowsExplicitPrivateLanHttpOnly() {
        assertTrue(AmarSecurityContracts.isAllowedSyncEndpoint("http://10.0.0.25:8080"))
        assertTrue(AmarSecurityContracts.isAllowedSyncEndpoint("http://192.168.1.20:8080"))
    }

    @Test
    fun rejectsPublicHttpAndBlankEndpoints() {
        assertFalse(AmarSecurityContracts.isAllowedSyncEndpoint("http://sync.example.com"))
        assertFalse(AmarSecurityContracts.isAllowedSyncEndpoint(""))
    }

    @Test
    fun rejectsBlankAndOversizedTokens() {
        runCatching { AmarSecurityContracts.requireSyncConfiguration("https://sync.example.com", "") }
            .onSuccess { error("blank token must be rejected") }

        runCatching {
            AmarSecurityContracts.requireSyncConfiguration(
                "https://sync.example.com",
                "x".repeat(4097),
            )
        }.onSuccess { error("oversized token must be rejected") }
    }

    @Test
    fun acceptsValidSyncConfiguration() {
        AmarSecurityContracts.requireSyncConfiguration("https://sync.example.com", "test-token")
    }
}
