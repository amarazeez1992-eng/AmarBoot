package com.personal.gridbot.amaros.agent

import java.security.MessageDigest

/** Evidence ledger for provenance, conflict tracking and reproducible reasoning. */
class AmarEvidenceLedger {
    private val entries = mutableListOf<AmarEvidence>()

    fun record(evidence: AmarEvidence) { entries += evidence }
    fun all(): List<AmarEvidence> = entries.toList()
    fun clear() { entries.clear() }
}

data class AmarEvidence(
    val claim: String,
    val sourceTitle: String,
    val sourceUri: String,
    val authority: AmarEvidenceAuthority,
    val retrievedAtEpochMs: Long = System.currentTimeMillis(),
    val confidence: Double,
    val conflict: Boolean = false,
    val fingerprint: String = fingerprintOf("$sourceUri|$claim")
) {
    init { require(confidence in 0.0..1.0) }

    companion object {
        fun fingerprintOf(value: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}

enum class AmarEvidenceAuthority { PRIMARY, OFFICIAL, PEER_REVIEWED, REPUTABLE, COMMUNITY, UNKNOWN }
