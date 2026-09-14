# Stage 9 — Performance, Parallel Workforce and Reliability

STATUS: CLOSED — verified 2026-09-14

## Scope

Stage 9 introduces a reusable bounded workforce for independent research/analysis tasks without granting execution authority.

Implemented boundary:
- `AmarParallelWorkforce<K, V>`
- bounded worker concurrency (1–16)
- hard item bound (1–512)
- per-item timeout/latency budget
- cooperative coroutine cancellation
- deterministic result aggregation by input index
- bounded thread-safe LRU cache
- configurable retry/recovery
- explicit ONLINE/PARTIAL/OFFLINE connectivity state
- network-required work is fail-closed when offline
- partial results remain observable rather than silently discarded
- explicit cache generic boundary prevents key/value type drift

## Safety and architecture

- The workforce executes only supplied worker functions; it does not own broker/device permissions.
- It is transport-neutral and can serve research, analysis, evidence, or other independent non-execution tasks.
- A failed worker does not cancel unrelated siblings; caller cancellation does cancel the workforce.
- Timeout is classified separately from caller cancellation.
- Cache keys must be unique within one batch to prevent ambiguous aggregation.
- Result order is deterministic regardless of completion order.
- Execution authority remains outside this workforce and behind the existing governance boundary.

## Verification evidence

All closure gates were verified:
1. focused Stage 9 unit tests — SUCCESS
2. full unit-test suite — SUCCESS
3. debug build verification — SUCCESS
4. Stage 9 CI — SUCCESS
5. changed production file re-inspected — COMPLETE
6. architecture/regression audit — COMPLETE

Verified CI run: `34866641241`
Verified job: `104052015157`
Verified production correction commit: `397a25ae9f24d242cd314d312328439e000965cb`

The permanent engineering regression guard includes the Stage 9 generic-type contract failure pattern and requires the inspect → implement → re-inspect → focused test → full test → CI → audit gate for Stages 7–10.

## Closure

Stage 9 is officially CLOSED after successful focused tests, full unit tests, debug build verification, CI verification, re-inspection, and architecture/regression audit.
