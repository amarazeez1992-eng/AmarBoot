# Stage 11 Item 7 — Benchmark & Quality

Status: READY

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

The corrected implementation commit `b1b3b7c413a4396e30c80f6086d2cfcd0dd1920e` passed the complete fresh verification cycle. Stage Eleven run `34959983968` completed successfully, including the focused Item 7 benchmark, full unit suite, debug build, architecture regression checks, and evidence upload. Stage One `34959983999`, Stage Two `34959983997`, CodeQL `34959983973`, and Stage 10 Final Audit `34959983960` also completed successfully on the same corrected commit.

- Benchmark cases: 12 named cases; uniqueness is asserted; no skipped/disabled cases.
- Benchmark execution: repeated deterministic execution is asserted by the test.
- Focused Item 7 benchmark: PASS in Stage Eleven run `34959983968`.
- Full relevant unit suite: PASS in Stage Eleven run `34959983968`.
- Debug build: PASS in Stage Eleven run `34959983968`.
- Architecture regression checks: PASS in Stage Eleven run `34959983968`.
- Fresh current-commit CI: PASS across Stage Eleven, Stage One, Stage Two, CodeQL, and Stage 10 Final Audit runs listed above.
- Fresh evidence artifact: `amar-stage-eleven-evidence`, artifact `10392618536`, digest `sha256:f595391d23bad2689d33ef811b826d3def15313f33bf9ee8f38d83209be06389`.
- Current implementation remains test-only and reuses the authoritative `DecisionEngine` and `AmarAgentCritic`; no execution authority or duplicate runtime engine was introduced.
- Invalidated PR #46 is not reused.

## Final audit condition

Item 7 is READY after the completed fresh verification cycle. Final CLOSED status requires a final closure commit followed by another fresh complete verification cycle. No deletion, omission, disabling, bypassing, simplification, or scope reduction is permitted to obtain success.
