package com.personal.gridbot.amaros.errors

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AmarErrorClassifierTest {
    @Test
    fun illegalArgumentException_maps_to_validation() {
        val error = AmarErrorClassifier.classify("test", IllegalArgumentException("bad"))
        assertEquals(AmarErrorCategory.VALIDATION, error.category)
    }

    @Test
    fun ioException_maps_to_network() {
        val error = AmarErrorClassifier.classify("test", IOException("network"))
        assertEquals(AmarErrorCategory.NETWORK, error.category)
    }

    @Test
    fun securityException_maps_to_security() {
        val error = AmarErrorClassifier.classify("test", SecurityException("denied"))
        assertEquals(AmarErrorCategory.SECURITY, error.category)
    }

    @Test
    fun runtimeException_maps_to_unexpected() {
        val error = AmarErrorClassifier.classify("test", RuntimeException("unexpected"))
        assertEquals(AmarErrorCategory.UNEXPECTED, error.category)
    }

    @Test
    fun source_and_cause_are_preserved() {
        val cause = IllegalArgumentException("bad")
        val error = AmarErrorClassifier.classify("source-module", cause)
        assertEquals("source-module", error.source)
        assertSame(cause, error.cause)
    }
}
