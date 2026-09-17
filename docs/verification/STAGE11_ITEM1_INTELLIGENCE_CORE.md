# Stage 11 — Item 1: Intelligence Core

**Status: VERIFICATION IN PROGRESS**

## Scope

Establish a deterministic intelligence foundation before adding the later confidence, evidence, research, decision, and multimodal layers.

## Implemented

- Typed observation kinds and explicit input status classification.
- Normalized content handling.
- Deterministic perception snapshot with completeness measurement.
- Explicit reasoning scaffold with assumptions, alternatives, and `requiresMoreInput`.
- Explicit analysis state: `READY`, `DEGRADED`, or `BLOCKED`.
- Bounded quality/freshness inputs and validation of invalid numeric values.
- Deterministic bounded confidence calculation retained as a compatibility signal; dedicated Confidence Engine remains Item 2.
- No model, network, tool, broker, or mutable application-state access from the core.

## Tests Added/Updated

`AmarIntelligenceCoreTest` covers:

1. present/missing/malformed classification;
2. fail-closed behavior with no usable input;
3. degraded state for partial input;
4. bounded confidence and sensitivity to quality/freshness/completeness;
5. rejection of invalid quality/freshness values;
6. deterministic repeated analysis and input-order preservation.

## Verification Gate

The Stage 11 CI workflow must pass the focused Intelligence Core test, the full unit suite, debug build, architecture regression checks, and evidence upload before this item can be constitutionally closed.

**No Stage 11 Item 1 closure is declared from code changes alone.**
