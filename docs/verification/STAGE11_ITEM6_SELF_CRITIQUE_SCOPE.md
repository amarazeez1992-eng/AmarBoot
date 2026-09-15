# Stage 11 Item 6 — Self-Critique Scope

Status: CLOSED

## Design authority

- Reused the existing `AmarAgentCritic`; no second critique engine was introduced.
- Critique remains advisory and fail-closed: it can reject/revise a draft but cannot execute trades or mutate governance.
- Existing evidence contracts (`ResearchFinding`, `EvidenceStance`) remain authoritative.

## Verified behavior

1. Rejects blank drafts.
2. When evidence is required, rejects missing or invalid evidence.
3. Counts independent source URIs rather than duplicate references.
4. Surfaces support/opposition conflicts instead of hiding them.
5. Detects unsupported certainty language.
6. Detects unsupported numeric claims when evidence is required.
7. Produces a bounded critique score in `[0,1]`.
8. Preserves the existing `review(...)` contract used by the orchestrator.
9. Preserves the existing guarantee-language guard.
10. Invalid evidence is never treated as supporting evidence.

## Closure evidence

- Current-main Stage 11 verification passed on the corrected implementation commit before this documentation closure.
- Focused Item 6 regression coverage passed, including invalid-evidence and guarantee-language regression guards.
- Full unit suite passed.
- Debug build passed.
- Architecture/execution-safety checks passed.
- No duplicate critique engine and no execution authority were introduced.

Closure loop completed:

**تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**

Final status: **CLOSED**
