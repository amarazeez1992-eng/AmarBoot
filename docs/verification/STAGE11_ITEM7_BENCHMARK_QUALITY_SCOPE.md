# Stage 11 Item 7 — Benchmark & Quality

Status: CLOSED

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

## Final closure evidence

The final READY verification commit `82605a5b896bb19b0e9d7e466255f5c34fc855b4` passed the complete fresh verification cycle. Stage Eleven run `34961376037` completed successfully, including the focused Item 7 benchmark, full unit suite, debug build, architecture regression checks, and evidence upload. Stage One `34961375995`, Stage Two `34961376006`, CodeQL `34961375939`, and Stage 10 Final Audit `34961376083` also completed successfully on the same final READY verification commit.

- Benchmark cases: 12 named cases; uniqueness is asserted; no skipped/disabled cases.
- Benchmark execution: repeated deterministic execution is asserted by the test.
- Focused Item 7 benchmark: PASS in Stage Eleven run `34961376037`.
- Full relevant unit suite: PASS in Stage Eleven run `34961376037`.
- Debug build: PASS in Stage Eleven run `34961376037`.
- Architecture regression checks: PASS in Stage Eleven run `34961376037`.
- Fresh current-commit CI: PASS across Stage Eleven, Stage One, Stage Two, CodeQL, and Stage 10 Final Audit runs listed above.
- Fresh evidence artifact: `amar-stage-eleven-evidence`, artifact `10392999941`, digest `sha256:f4c1a36423d23011fdfafa61466e6f9eef6553c28f5eb7a957d0a7b2a1597c09`.
- Current implementation remains test-only and reuses the authoritative `DecisionEngine` and `AmarAgentCritic`; no execution authority or duplicate runtime engine was introduced.
- Invalidated PR #46 is not reused.

## Final audit

Item 7 is CLOSED after completion of the mandatory closure loop: investigation → implementation → inspection → testing → confirmation → proof → final audit → ready → closure. The final CLOSED commit itself must undergo a fresh complete verification cycle; no deletion, omission, disabling, bypassing, simplification, or scope reduction is permitted to obtain success.
