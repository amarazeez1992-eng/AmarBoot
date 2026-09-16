# AMAR AI — Mandatory Stage Closure Protocol

Status: BINDING PROJECT RULE

## Purpose
This document establishes the mandatory verification loop for every stage from Stage 1 through final closure.

## Non-negotiable rule
No stage may be considered complete, closed, or passed merely because code exists, a historical run passed, or a workflow is green on an older commit.

For every stage and every dependent capability, the required loop is:

**Review → Implement → Inspect → Audit → Test → Confirm → Evidence → Experience/Run when applicable → Re-test → Final Audit → Close**

If any gate fails, the stage remains OPEN/RED and work returns to the failed gate until the root cause is corrected.

## Continuity rule
Each stage must be revalidated against the current HEAD and against the requirements and contracts inherited from all earlier stages. Historical success is evidence of history only; it is not current closure evidence.

## Engineering quality rule
Corrections must address the root cause. Do not weaken tests, delete requirements, add duplicate authorities/routers/bridges merely to make CI green, or use a temporary workaround as a final implementation.

## Strategy preservation
The agreed architecture, product strategy, required capabilities, and existing valid functionality must not be changed or removed without explicit owner approval. Technical refactoring is allowed only when it preserves those requirements and improves correctness, safety, maintainability, or testability.

## Authority rule
The Amar AI Agent remains the central authority. UI is presentation/interaction. Adapters transport requests/results; they do not become competing policy authorities.

## Provider rule
Gemini and other external AI providers are not the core authority. The project must remain provider-neutral and must not require an external AI provider for core operation.

## Safety rule
Sensitive operations require explicit permission and confirmation. Unverified capabilities remain **NOT VERIFIED / FAIL-CLOSED**. No fabricated observations, hidden execution, covert camera/screen access, Android/security bypass, or unauthorized device control.

## Evidence rule
A closure record must identify the exact current commit, relevant tests, build results, security/regression checks, and any remaining limitations. Unsupported claims are not closure evidence.

## Final gate
Stages 1–10 and Stage 11 integration must be rechecked as one connected system before final release. The final artifact must be independently verified. MT5 remains a final-stage scope item and is not to be introduced early.
