# Stage 9 — Performance, Parallel Workforce and Reliability

STATUS: OPEN — implementation landed; verification gate pending

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

## Safety and architecture

- The workforce executes only supplied worker functions; it does not own broker/device permissions.
- It is transport-neutral and can serve research, analysis, evidence, or other independent non-execution tasks.
- A failed worker does not cancel unrelated siblings; caller cancellation does cancel the workforce.
- Timeout is classified separately from caller cancellation.
- Cache keys must be unique within one batch to prevent ambiguous aggregation.
- Result order is deterministic regardless of completion order.

## Verification gate

Required before closure:
1. focused Stage 9 unit tests
2. full unit-test suite
3. debug build verification
4. CI success
5. re-inspection of changed files
6. architecture/regression audit

The Stage 9 CI workflow is also enabled for pull requests so the gate can be independently observed before closure.

Verification branch is intentionally documentation-only; production Stage 9 code is unchanged.

Do not mark Stage 9 CLOSED before all gates above are verified.
