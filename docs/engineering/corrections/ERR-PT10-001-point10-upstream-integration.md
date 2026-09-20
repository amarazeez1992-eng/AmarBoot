# ERR-PT10-001 — Point 10 upstream integration gap

**Status:** Corrected — pending CI constitutional verification  
**Area:** Stage 11 / Item 3 / Point 10  
**Detected:** 2026-09-20

## Symptom

Point 10 had a deterministic certification engine and an explicit AmarEvidenceQualityUpstreamState, but the canonical assembler could not obtain the real Point 1 Evidence Intake and Point 8 Tampering states from the owning verification boundary.

The contract correctly remained open instead of fabricating those states.

## Root cause

The composition boundary was intentionally fail-closed, but the runtime had not yet connected the existing verification layer's usable-evidence counts and recorded provenance chain to the Point 10 upstream-state contract.

## Correction

Implemented the canonical runtime wiring:

- Point 1 is sourced from the owning verification report's usableEvidenceCount and invalidEvidenceCount.
- Point 8 is sourced from the owning verification report's recorded provenance chain and rechecked by AmarEvidenceTamperingDetector.
- Points 2–7 and 9 continue to use their existing owners.
- Point 10 remains deterministic and fail-closed.
- No new heuristic weights, percentages, keyword routing, or duplicate evidence methodology were introduced.
- Financial/trading requests now require canonical Point 10 certification before final approval.

## Regression protection

Added tests proving:

1. verified owner outputs produce 1.0;
2. a tampered provenance chain produces 0.0;
3. Point 10 receives all nine upstream states through the canonical assembler.

## Future developer rule

Do not replace owner outputs with inferred booleans or local heuristics. Any future Evidence Engine point must expose its authoritative verification state through its owning contract before Point 10 can consume it.
