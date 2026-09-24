package com.personal.gridbot.amaros.logging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarLoggerTest {
    private class Sink : AmarLogSink {
        val events = mutableListOf<AmarLogEvent>()
        override fun write(event: AmarLogEvent) { events += event }
    }

    @Test fun records_all_levels_and_context() {
        val sink = Sink()
        val logger = AmarLogger(sink) { 1234L }

        logger.debug("TEST", "debug")
        logger.info("TEST", "info")
        logger.warn("TEST", "warn")
        logger.error("TEST", "error", IllegalStateException("boom"))

        assertEquals(4, sink.events.size)
        assertEquals(AmarLogLevel.DEBUG, sink.events[0].level)
        assertEquals(AmarLogLevel.INFO, sink.events[1].level)
        assertEquals(AmarLogLevel.WARN, sink.events[2].level)
        assertEquals(AmarLogLevel.ERROR, sink.events[3].level)
        assertEquals(1234L, sink.events[3].epochMs)
        assertEquals(IllegalStateException::class.java.name, sink.events[3].throwableType)
    }

    @Test fun bounds_android_tag_length() {
        val sink = Sink()
        AmarLogger(sink).info("123456789012345678901234567890", "x")
        assertEquals(23, sink.events.single().tag.length)
    }

    @Test fun redacts_sensitive_values() {
        val sink = Sink()
        AmarLogger(sink).info("SECURITY", "token=abc123 password=hunter2 credential=secret authorization=BearerXYZ")
        val message = sink.events.single().message
        assertFalse(message.contains("abc123"))
        assertFalse(message.contains("hunter2"))
        assertFalse(message.contains("secret"))
        assertFalse(message.contains("BearerXYZ"))
        assertTrue(message.contains("[REDACTED]"))
    }

    @Test fun logger_does_not_require_android_backend_for_unit_tests() {
        val sink = Sink()
        AmarLogger(sink).info("TEST", "ok")
        assertNotNull(sink.events.single())
    }
}
