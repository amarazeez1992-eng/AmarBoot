# AMAR AI — Accuracy-First Additions: Placement & Implementation Map

## Status
APPROVED / IMPLEMENTATION RECORDED / VERIFICATION PENDING

## Constitutional placement
- Accuracy-first waiting and bounded verification: Stage 11 governance / existing accuracy-first constitutional rule.
- Transparent task states (A): Stage 11 Item 20 — Task State Machine. Shared state/progress contract is supporting infrastructure; Item 20 is not declared complete.
- Progress/observability (B): Stage 11 Item 19 — Observability. UI progress emission is supporting infrastructure; Item 19 is not declared complete.
- Source counts and accepted-source distinction: Stage 11 Item 3 Evidence Engine, especially Ranking / Explanation / Audit Trail.
- Analysis versus official result presentation: Stage 11 Item 21 — Explainability Layer.
- Research timing: accuracy-first rule plus Task State and Observability.

## Implemented now
1. AgentTaskState contract.
2. Progress callback from Orchestrator through Agent Engine to Android UI.
3. Visible elapsed research time.
4. Visible source-count telemetry when returned by research.
5. Separate searched/reviewed and accepted telemetry fields.
6. Final response metadata includes elapsed time and source counts.
7. Timeout remains fail-closed at 120 seconds.
8. No private chain-of-thought is exposed; only concise operational states are shown.
9. Regression tests lock the task-state contract.

## Integrity rule
A source count is displayed only from actual returned research findings. The system must never invent a number.

## Closure gates
Build → focused tests → Stage 11 CI → regression → failure/timeout verification → APK → device runtime → source-count accuracy → timing accuracy → UI state lifecycle → audit → constitutional acceptance.

No Item 19, Item 20, or Item 21 is declared closed by this document.