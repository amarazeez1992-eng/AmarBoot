package com.personal.gridbot.amaros.agent.lifecycle

import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceNode

/** Stateless, deterministic Point 23 lifecycle boundary. */
class AmarEvidenceLifecycleManager : AmarEvidenceLifecycleContract {
    override fun evaluate(input: EvidenceLifecycleInput): EvidenceLifecycleResult {
        if (input.currentTimeMs < 0L ||
            input.policy.agedAfterMs < 0L ||
            input.policy.expiredAfterMs <= input.policy.agedAfterMs
        ) {
            return failed(LifecycleReason.INVALID_INPUT)
        }

        if (input.currentEvidence.chainIntegrity.name != "INTACT" ||
            !input.currentEvidence.isDownstreamReady
        ) {
            return failed(LifecycleReason.TRANSITION_FAILED)
        }

        val fingerprints = input.currentEvidence.chainLinks
            .map { it.toFingerprint }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        if (fingerprints.isEmpty()) {
            return EvidenceLifecycleResult(
                currentStates = emptyList(),
                transitions = emptyList(),
                isDownstreamReady = true,
                reason = LifecycleReason.NO_BASELINE_AVAILABLE
            )
        }

        val provenanceByFingerprint = input.provenanceNodes
            .filter { it.evidenceFingerprint.isNotBlank() }
            .associateBy { it.evidenceFingerprint }

        if (fingerprints.any { it !in provenanceByFingerprint }) {
            return failed(LifecycleReason.MISSING_PROVENANCE)
        }

        val states = mutableListOf<EvidenceLifecycleSnapshot>()
        val transitions = mutableListOf<LifecycleTransition>()
        var baselineMissing = false

        for (fingerprint in fingerprints) {
            val node = provenanceByFingerprint.getValue(fingerprint)
            if (input.currentTimeMs < node.retrievedAtEpochMs) {
                return failed(LifecycleReason.TRANSITION_FAILED)
            }

            val state = stateFor(
                input.currentTimeMs - node.retrievedAtEpochMs,
                input.policy
            )
            val previous = input.previousSnapshots[fingerprint]

            if (previous == null) {
                baselineMissing = true
                states += EvidenceLifecycleSnapshot(
                    evidenceFingerprint = fingerprint,
                    state = state,
                    createdAt = node.retrievedAtEpochMs,
                    lastTransitionAt = node.retrievedAtEpochMs,
                    transitions = emptyList()
                )
                continue
            }

            if (previous.evidenceFingerprint != fingerprint ||
                previous.createdAt > input.currentTimeMs ||
                previous.lastTransitionAt > input.currentTimeMs
            ) {
                return failed(LifecycleReason.INVALID_INPUT)
            }

            val updatedTransitions = previous.transitions.toMutableList()
            var lastTransitionAt = previous.lastTransitionAt

            if (state != previous.state) {
                val transition = LifecycleTransition(
                    fromState = previous.state,
                    toState = state,
                    timestamp = input.currentTimeMs,
                    reason = "Time-based lifecycle threshold reached"
                )
                updatedTransitions += transition
                transitions += transition
                lastTransitionAt = input.currentTimeMs
            }

            states += EvidenceLifecycleSnapshot(
                evidenceFingerprint = fingerprint,
                state = state,
                createdAt = previous.createdAt,
                lastTransitionAt = lastTransitionAt,
                transitions = updatedTransitions
            )
        }

        return EvidenceLifecycleResult(
            currentStates = states.sortedBy { it.evidenceFingerprint },
            transitions = transitions.sortedWith(compareBy<LifecycleTransition> { it.timestamp }.thenBy { it.toState.ordinal }),
            isDownstreamReady = true,
            reason = if (baselineMissing) LifecycleReason.NO_BASELINE_AVAILABLE else LifecycleReason.VALID_LIFECYCLE
        )
    }

    private fun stateFor(ageMs: Long, policy: LifecyclePolicy): EvidenceLifecycleState =
        when {
            ageMs >= policy.expiredAfterMs -> EvidenceLifecycleState.EXPIRED
            ageMs >= policy.agedAfterMs -> EvidenceLifecycleState.AGED
            else -> EvidenceLifecycleState.ACTIVE
        }

    private fun failed(reason: LifecycleReason) = EvidenceLifecycleResult(
        currentStates = emptyList(),
        transitions = emptyList(),
        isDownstreamReady = false,
        reason = reason
    )
}
