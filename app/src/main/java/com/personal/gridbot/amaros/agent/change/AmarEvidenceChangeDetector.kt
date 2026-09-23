package com.personal.gridbot.amaros.agent.change

/**
 * Stateless, deterministic Point 22 boundary.
 *
 * The symbol is the stable comparison context. No entity ID is created and no
 * free-text symbol extraction is performed.
 */
class AmarEvidenceChangeDetector : AmarEvidenceChangeDetectionContract {

    override fun detect(input: EvidenceChangeDetectionInput): EvidenceChangeDetectionResult {
        if (input.currentTimeEpochMs < 0L) {
            return failed(ChangeDetectionReason.INVALID_INPUT)
        }

        val current = input.currentSnapshot
        if (current.symbol.isBlank()) {
            return failed(ChangeDetectionReason.NO_BASELINE_AVAILABLE)
        }

        if (current.capturedAtEpochMs < 0L || current.capturedAtEpochMs > input.currentTimeEpochMs) {
            return failed(ChangeDetectionReason.INVALID_INPUT)
        }

        if (current.entries.any { invalidEntry(it, input.currentTimeEpochMs) }) {
            return failed(ChangeDetectionReason.INVALID_INPUT)
        }

        val previous = input.previousSnapshot
            ?: return failed(ChangeDetectionReason.NO_BASELINE_AVAILABLE)

        if (previous.symbol.isBlank()) {
            return failed(ChangeDetectionReason.NO_BASELINE_AVAILABLE)
        }

        if (previous.symbol != current.symbol) {
            return failed(ChangeDetectionReason.SYMBOL_MISMATCH)
        }

        if (previous.capturedAtEpochMs < 0L || previous.capturedAtEpochMs > input.currentTimeEpochMs) {
            return failed(ChangeDetectionReason.INVALID_INPUT)
        }

        if (previous.entries.any { invalidEntry(it, input.currentTimeEpochMs) }) {
            return failed(ChangeDetectionReason.INVALID_INPUT)
        }

        return runCatching {
            val previousBySource = previous.entries.associateBy { it.sourceUri }
            val currentBySource = current.entries.associateBy { it.sourceUri }

            if (previousBySource.size != previous.entries.size ||
                currentBySource.size != current.entries.size
            ) {
                return@runCatching failed(ChangeDetectionReason.INVALID_INPUT)
            }

            val changes = mutableListOf<EvidenceChange>()

            currentBySource.forEach { (sourceUri, currentEntry) ->
                val previousEntry = previousBySource[sourceUri]
                when {
                    previousEntry == null -> {
                        val sourceMatch = previous.entries.firstOrNull {
                            it.evidenceFingerprint == currentEntry.evidenceFingerprint
                        }
                        if (sourceMatch != null) {
                            changes += EvidenceChange(
                                symbol = current.symbol,
                                changeType = ChangeType.SOURCE_CHANGED,
                                evidenceFingerprint = currentEntry.evidenceFingerprint,
                                previousSourceUri = sourceMatch.sourceUri,
                                currentSourceUri = sourceUri,
                                previousContent = sourceMatch.content,
                                currentContent = currentEntry.content
                            )
                        } else {
                            changes += EvidenceChange(
                                symbol = current.symbol,
                                changeType = ChangeType.CONTENT_ADDED,
                                evidenceFingerprint = currentEntry.evidenceFingerprint,
                                previousSourceUri = null,
                                currentSourceUri = sourceUri,
                                previousContent = null,
                                currentContent = currentEntry.content
                            )
                        }
                    }
                    previousEntry.content != currentEntry.content -> {
                        changes += EvidenceChange(
                            symbol = current.symbol,
                            changeType = ChangeType.CONTENT_MODIFIED,
                            evidenceFingerprint = currentEntry.evidenceFingerprint,
                            previousSourceUri = sourceUri,
                            currentSourceUri = sourceUri,
                            previousContent = previousEntry.content,
                            currentContent = currentEntry.content
                        )
                    }
                }
            }

            previousBySource.forEach { (sourceUri, previousEntry) ->
                if (sourceUri !in currentBySource &&
                    current.entries.none { it.evidenceFingerprint == previousEntry.evidenceFingerprint }
                ) {
                    changes += EvidenceChange(
                        symbol = current.symbol,
                        changeType = ChangeType.CONTENT_REMOVED,
                        evidenceFingerprint = previousEntry.evidenceFingerprint,
                        previousSourceUri = sourceUri,
                        currentSourceUri = null,
                        previousContent = previousEntry.content,
                        currentContent = null
                    )
                }
            }

            EvidenceChangeDetectionResult.immutable(
                changes = changes.sortedWith(
                    compareBy<EvidenceChange> { it.changeType.ordinal }
                        .thenBy { it.evidenceFingerprint }
                        .thenBy { it.previousSourceUri.orEmpty() }
                        .thenBy { it.currentSourceUri.orEmpty() }
                ),
                actionFlag = changes.isNotEmpty(),
                isDownstreamReady = true,
                reason = ChangeDetectionReason.VALID_CHANGE_DETECTION
            )
        }.getOrElse {
            failed(ChangeDetectionReason.CHANGE_DETECTION_FAILED)
        }
    }

    private fun invalidEntry(entry: EvidenceSnapshotEntry, currentTimeEpochMs: Long): Boolean =
        entry.evidenceFingerprint.isBlank() ||
            entry.sourceUri.isBlank() ||
            entry.content.isBlank() ||
            entry.retrievedAtEpochMs < 0L ||
            entry.retrievedAtEpochMs > currentTimeEpochMs

    private fun failed(reason: ChangeDetectionReason): EvidenceChangeDetectionResult =
        EvidenceChangeDetectionResult.immutable(
            changes = emptyList(),
            actionFlag = false,
            isDownstreamReady = false,
            reason = reason
        )
}
