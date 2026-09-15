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

The invalidated implementation from PR #46 is not reused.
