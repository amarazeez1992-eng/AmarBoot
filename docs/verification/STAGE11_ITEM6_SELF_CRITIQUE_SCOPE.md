# Stage 11 Item 6 — Self-Critique Scope

Status: IN PROGRESS

## Design authority

- Reuse the existing `AmarAgentCritic`; do not introduce a second critique engine.
- Critique remains advisory and fail-closed: it can reject/revise a draft but cannot execute trades or mutate governance.
- Existing evidence contracts (`ResearchFinding`, `EvidenceStance`) remain authoritative.

## Required behavior

1. Reject blank drafts.
2. When evidence is required, reject missing or invalid evidence.
3. Count independent source URIs rather than duplicate references.
4. Surface support/opposition conflicts instead of hiding them.
5. Detect unsupported certainty language.
6. Detect unsupported numeric claims when evidence is required.
7. Produce a bounded critique score in `[0,1]`.
8. Preserve the existing `review(...)` contract used by the orchestrator.

## Closure evidence

1. Focused Item 6 regression suite passes.
2. Full unit suite passes.
3. Debug build passes.
4. Architecture/execution-safety checks pass.
5. Fresh CI runs on the exact current commit.
6. Final audit confirms no duplicate critic engine and no execution authority.

Closure loop is mandatory:

**تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**
