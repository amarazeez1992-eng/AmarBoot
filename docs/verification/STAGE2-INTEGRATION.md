# AMAR AI — STAGE 2 INTEGRATION

**Status:** Integration Document — Living — Editable  
**Layer 1:** Stage 2 Engine (Closed)  
**Layer 2:** Cognitive Intelligence (Designed)  
**Code:** None added  
**Quality:** 100% — No regression  

## 1. Definition of Complete Stage 2

Stage 2 is represented as two integrated layers. The integration is additive and preserves the existing Stage 2 implementation.

### Layer 1 — AmarStageTwoEngine — Closed

**AmarStageTwoEngine** is the existing Stage 2 Multi-Role Deliberation layer. It operates over the shared evidence snapshot and coordinates the existing Stage 2 deliberation roles and boundaries.

**AmarStageTwoHardening** is part of the preserved Layer 1 hardening boundary.

This layer remains unchanged by this integration document.

### Layer 2 — Cognitive Intelligence — Designed

Layer 2 is the Cognitive Intelligence design defined by `STAGE2-COGNITIVE-INTELLIGENCE.md`:

- 72 Capabilities
- 14 Engines
- 3 Gates
- 397 internal additions in the draft capability breakdown

Layer 2 is design-only at this point. It does not replace Layer 1 and adds no implementation code through this document.

## 2. Integration Matrix

| Layer | Element | Status | Reference |
|---|---|---|---|
| 1 | `AmarStageTwoEngine` | ✅ Closed | `AmarStageTwoEngine.kt` |
| 1 | `AmarStageTwoHardening` | ✅ Closed | `AmarStageTwoHardening.kt` |
| 1 | 10 Stage 2 tests | ✅ Closed | `AmarAgentStageTwoTest.kt` |
| 2 | 72 Capabilities | 📄 Design | `STAGE2-COGNITIVE-INTELLIGENCE.md` |
| 2 | 14 Engines | 📄 Design | `STAGE2-COGNITIVE-INTELLIGENCE.md` |
| 2 | 3 Gates | 📄 Design | `STAGE2-COGNITIVE-INTELLIGENCE.md` |
| 2 | 397 additions | 📄 Design | `STAGE2-CAPABILITY-BREAKDOWN.md` |

The 397 additions are a **draft implementation breakdown**, not 397 independently constitutionally approved requirements.

## 3. Difference Between the Two Layers

| Layer 1 | Layer 2 |
|---|---|
| Executes the existing Stage 2 deliberation flow | Provides the designed Cognitive Intelligence layer |
| Multi-Role | Cognitive Intelligence |
| Shared Evidence Snapshot / State | Cognitive State + Awareness |
| Existing Stage 2 implementation | Design reference |
| Closed | Designed / Editable |

Layer 2 may reason about cognition, context, intent, uncertainty, hypotheses, simulation, self-review, and cognitive state according to its design. It must not turn inference into observed fact, simulation into evidence, or cognitive analysis into execution authority.

## 4. Integration Boundaries

1. **Additive only:** Layer 2 is added above Layer 1.
2. **Preservation:** Layer 1 remains intact.
3. **No replacement:** `AmarStageTwoEngine.kt` is not replaced.
4. **No deletion:** Existing Stage 2 code and tests are preserved.
5. **No regression:** Existing behavior and tests remain protected.
6. **No duplication:** Layer 2 does not recreate responsibilities already owned by the existing Stage 2 implementation or by other constitutional stages.
7. **No authority escalation:** Layer 2 does not acquire Evidence Validation, External Research Authority, Final Decision Authority, Authorization, Execution, Broker, or Market Signal Authority.
8. **Editable design:** Layer 2 remains a living, editable design until implementation and verification establish its final state.

## 5. Source Documents

- `docs/verification/STAGE2-COGNITIVE-INTELLIGENCE.md` — Layer 2 Cognitive Intelligence design.
- `docs/verification/STAGE2-CAPABILITY-BREAKDOWN.md` — 72-capability draft breakdown and 397 internal additions.
- `app/src/main/java/com/personal/gridbot/amaros/agent/AmarStageTwoEngine.kt` — preserved Layer 1 engine.
- `app/src/main/java/com/personal/gridbot/amaros/agent/AmarStageTwoHardening.kt` — preserved Layer 1 hardening.
- `app/src/test/java/com/personal/gridbot/amaros/agent/AmarAgentStageTwoTest.kt` — preserved Stage 2 tests.

## 6. Governance

This document records the integration model. It does not replace the Master Constitution and does not independently close Layer 2. Any future implementation of Layer 2 must remain additive, preserve Layer 1, and be verified before any closure claim.

**Layer 1:** Closed  
**Layer 2:** Designed — Living — Editable  
**Integration:** Documented  
