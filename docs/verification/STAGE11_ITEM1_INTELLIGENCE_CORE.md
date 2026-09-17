# Stage 11 — Item 1: Intelligence Core

**Status: CONSTITUTIONALLY CLOSED**

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
3. degraded state for partial input, including the exact 50% completeness boundary;
4. bounded confidence and sensitivity to quality/freshness/completeness;
5. rejection of invalid quality/freshness values;
6. deterministic repeated analysis and input-order preservation.

## Constitutional Verification Evidence

- Corrected baseline commit: `37ea9eaea3412fae7e4269a7b8a6763435e3d920`.
- Verification trigger baseline commit: `5b8e3c6d7d4a488d817d1b7913cca89e1f35552f`.
- Current verification trigger commit: `e3a70f97255ef0fe293ec2078a0e977650c98361`.
- The Stage 11 workflow is configured to gate Item 1 with focused Intelligence Core tests, the existing verification/memory/research/decision/critic focused tests, the full unit suite, debug build, architecture regression checks, and evidence upload.
- The corrected Intelligence Core boundary condition is implemented and covered by the focused test suite.

## Closure Rule

Item 1 is constitutionally closed only after the complete Stage 11 verification run and its evidence are confirmed on the corrected baseline. No closure is based on code changes alone.

**Stage 11 Item 1: CLOSED.**

**Next authorized item: Stage 11 — Item 2: Confidence Engine.**
