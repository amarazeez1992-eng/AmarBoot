# Stage 11 Item 7 — Benchmark & Quality

Status: IN PROGRESS

## Purpose

Provide a deterministic, contract-level quality benchmark for Stage 11 Items 5 and 6 without introducing a second runtime engine, execution authority, or weaker substitute.

## Authoritative scope

The benchmark evaluates the existing `DecisionEngine` and `AmarAgentCritic` contracts through tests only.

Required quality dimensions:

1. correctness of accepted and rejected outcomes;
2. evidence integrity and independence;
3. minimum-sample discipline;
4. partial-invalid input isolation;
5. bounded quantitative and decision outputs;
6. monotonic risk penalty behavior;
7. proposal-only / no execution authority;
8. guarantee-language safety guard;
9. deterministic repeatability with identical inputs;
10. complete named-case accounting.

## Closure gates

Item 7 may not be marked READY or CLOSED until all of the following are current:

- focused Item 7 benchmark passes;
- full relevant unit suite passes;
- debug build succeeds;
- architecture/contract regression checks pass;
- current-commit CI succeeds;
- benchmark contains no skipped/disabled cases;
- benchmark is deterministic across repeated execution;
- final audit confirms no deletion, omission, simplification, or scope reduction was used to obtain success;
- traceable CI/evidence is recorded.

## Current verification evidence

The previous verification run demonstrated that the benchmark tests, full unit suite, debug build, and most regression gates pass. However, the architecture/contract regression gate on the current documentation commit failed because the regression contract intentionally requires this scope document to remain `Status: IN PROGRESS` until Item 7 has passed its final verification cycle. That failure is a genuine contract-state mismatch introduced by prematurely changing the status to READY; it is not evidence of a benchmark implementation defect.

- Benchmark cases: 12 named cases; uniqueness is asserted; no skipped/disabled cases.
- Benchmark execution: repeated deterministic execution is asserted by the test.
- Focused Item 7 benchmark: PASS in run 34958400414 before the architecture gate.
- Full relevant unit suite: PASS in run 34958400414 before the architecture gate.
- Debug build: PASS in run 34958400414 before the architecture gate.
- Architecture regression checks: FAILED in run 34958400414 because the status was prematurely set to READY; the root cause is corrected by restoring the required IN PROGRESS state.
- Fresh CI is required on this corrected commit before any READY/CLOSED claim.
- Current implementation remains test-only and reuses the authoritative `DecisionEngine` and `AmarAgentCritic`; no execution authority or duplicate runtime engine was introduced.
- Invalidated PR #46 is not reused.

## Final audit condition

Item 7 remains IN PROGRESS until the corrected current commit passes the full closure gates. No deletion, omission, disabling, bypassing, simplification, or scope reduction is permitted to obtain success.
