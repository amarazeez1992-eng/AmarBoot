# Amar AI — Engineering Regression Exclusions

## Purpose

This document is a permanent implementation guard for all future stages. The following failure patterns are explicitly excluded from future implementation and must be checked before any Stage 7–10 work and every subsequent change.

## Mandatory pre-write checks

Before writing or modifying code:

1. Inspect existing definitions and contracts before introducing a new type, enum, constant, or data model.
2. Search the repository for every symbol being changed or introduced; do not duplicate an existing canonical contract.
3. Check coroutine/suspend boundaries before using suspend APIs inside collection, sequence, callback, or non-suspend lambdas.
4. Verify every enum/reference against its canonical declaration and all current call sites.
5. Check tests for semantic validity: tests must actually test the intended distinction, not accidentally use identical values or equivalent fixtures.
6. Check registry/catalog logic against its stated contract, including required channels, sources, and minimum coverage.
7. Check idempotency boundaries in tests and implementation: an idempotency key/decision ID represents one immutable decision outcome. Never reuse the same key in a test to simulate a new authorization attempt after revocation, expiry, or another state change; use a new key and separately verify that the original receipt remains stable.
8. Check generic type contracts end-to-end: constructor defaults, explicitly typed fields, method parameters, and call sites must agree on the same key/value types; never rely on accidental inference when a generic boundary is safety-relevant.

## Mandatory post-write checks

After every implementation change:

1. Re-read the complete changed file.
2. Search for duplicate declarations and unresolved references across the repository.
3. Run focused tests.
4. Run the full unit-test suite.
5. Inspect CI results; a change is not considered complete until CI is successful.
6. If CI fails, inspect the actual failure log and fix the root cause before proceeding.
7. Never declare a stage closed from design or from a partial/local-looking result alone.

## Explicit regression patterns from Stage 6

- Duplicate canonical types/enums in the same package.
- Calling suspend functions from non-suspend sequence/collection lambdas.
- Adding enum consumers without updating the single canonical enum contract.
- Tests whose supposedly different cases accidentally use the same value.
- Reusing an idempotency key/decision ID for a new logical authorization attempt after the original decision has already produced a receipt.
- Catalog/discovery implementations that silently omit required channels or contract dimensions.
- Generic cache/worker contracts where a key/value generic is inferred differently from the field or method call site, causing compile-time type mismatches.
- Pushing code before compile/test/CI verification.
- Reusing a previous failing implementation pattern merely by renaming files or symbols.

## Stage gate

For Stages 7, 8, 9, and 10, these exclusions are a hard gate. A stage cannot be marked CLOSED while any regression above remains unresolved or unverified.

**Rule:** inspect → design against existing contracts → implement → re-inspect → focused test → full test → CI → audit → only then approve/close.
