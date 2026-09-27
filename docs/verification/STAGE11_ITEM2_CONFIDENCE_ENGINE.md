# Stage 11 — Item 2: Confidence Engine

**Status:** CLOSED

## 11 Additions

1. Dedicated Engine
2. Five Dimensions
3. Finite [0,1] Validation
4. Deterministic Weighted Score
5. Stable Labels — REJECTED, LOW, MODERATE, HIGH, VERY_HIGH; unreachable VERY_LOW removed
6. Explainable Reasons
7. No Execution Authority
8. Critical Boundary Guards
9. Rejection Threshold (<0.30 → REJECTED)
10. Staleness Ceiling
11. Production Consumer Contract

## Production Evidence

- Correction parent: da80466165de7c614018894038110c3ca0e03f63
- AmarConfidenceEngine.kt corrected Blob: e9247713136fcf93b62371622a82212ee4617d79
- AmarConfidenceEngineTest.kt Blob: 14c88be17b7743b4ed65233589ee33a0708b0981
- Tests: 25 / 0 / 0
- Commit: 98accd2706259a5efc31c8d8edff0c34d91de821

## Item 1 ↔ Item 2 Resolution

**Decision: REMOVE the duplicate confidence algorithm from Item 1.**

AmarIntelligenceCore.confidence() remains the Item 1 compatibility entry point. It prepares the five Item 2 dimensions, calls AmarConfidenceEngine.evaluate(), returns AmarConfidenceEngine.Result, and contains no independent weighting or label algorithm.

- AmarIntelligenceCore.kt corrected Blob: bce754f92f777f2d00a13622aa3e4b03e4a1baee
- AmarIntelligenceCoreTest.kt corrected Blob: e651f40e29f1be8fe04332199cf1b9e4b042cc3e
- Direct delegation test added.

## Contract

interface AmarConfidenceConsumer {
    fun requestConfidence(evidence: AmarConfidenceEngine.Evidence): AmarConfidenceEngine.Result
}

**Consumers:** Item 13 + Stage 16.

No Item 13 or Stage 16 wiring is performed in this correction.

## CI Evidence

- Required workflow: Amar Stage Eleven
- Required head SHA: 98accd2706259a5efc31c8d8edff0c34d91de821
- Required conclusion: success
- Artifact: amar-stage-eleven-evidence
- Run ID: 36309921454 — success

## Authority

AmarConfidenceEngine is the single Confidence Authority.
