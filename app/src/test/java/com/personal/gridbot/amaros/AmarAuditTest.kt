package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.audit.AmarAuditLog
import com.personal.gridbot.amaros.audit.AmarAuditRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAuditTest {
    @Test fun audit_redactsSecretsAndVerifiesChain() {
        val log = AmarAuditLog(capacity = 3)
        log.append(AmarAuditRecord(epochMs = 1, category = "security", action = "login", outcome = "ok", details = mapOf("token" to "hidden", "safe" to "yes")))
        log.append(AmarAuditRecord(epochMs = 2, category = "runtime", action = "cycle", outcome = "ok"))
        val records = log.snapshot()
        assertEquals(2, records.size)
        assertFalse(records[0].details.containsKey("token"))
        assertEquals("yes", records[0].details["safe"])
        assertTrue(log.verifyIntegrity())
    }
}
