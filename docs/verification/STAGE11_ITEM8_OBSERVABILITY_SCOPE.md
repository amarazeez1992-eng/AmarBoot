# Stage 11 — Item 8: Observability

Status: IN PROGRESS

## Objective

Provide a bounded, structured, auditable observability contract for critical Amar Intelligence behavior without creating execution authority or hidden state.

## Required capabilities

1. Structured events with timestamp, trace ID, component, event, outcome, severity, and sanitized details.
2. Bounded in-memory event retention with deterministic counters that remain correct when older events are evicted.
3. Metrics for total events, error events, critical events, and per-event counts.
4. Health signal that fails closed after a critical observed condition and exposes the last critical/error timestamps and reason.
5. Traceability into the existing authoritative `AmarAuditLog`; no parallel audit authority.
6. Secret-key filtering for passwords, tokens, secrets, and credentials before event/audit persistence.
7. Deterministic testability through an injected clock and repeatable contract tests.
8. Explicit proof that observability remains provider-agnostic and does not grant broker/live execution authority.

## Non-goals

- No broker access.
- No order execution.
- No credentials storage.
- No replacement of the existing audit authority.
- No removal or weakening of existing safety contracts.

## Verification evidence required before closure

- Focused Item 8 unit tests.
- Full relevant unit suite.
- Debug build.
- Architecture regression checks.
- Fresh CI on the current Item 8 commit.
- Fresh evidence artifact.
- Final audit against this scope and the project constitution.

## Closure chain

**تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**

Item 8 remains open until every required proof is current and traceable.
