# Stage 11 — Item 7 Benchmark & Quality

**Status: IN PROGRESS**

## Purpose

Establish a deterministic, repeatable quality gate for the Stage 11 intelligence layer without introducing a second decision, research, or critique engine.

## Scope

The benchmark evaluates existing authoritative components:

- `DecisionEngine` quantitative-risk contract.
- `AmarAgentCritic` evidence and safety-review contract.

It measures:

- correctness of evidence availability handling;
- monotonic risk ordering;
- rejection of invalid/non-finite inputs as synthetic risk;
- preservation of independent valid metrics under partial invalidity;
- bounded finite risk scores;
- acceptance of supported evidence;
- rejection of evidence conflicts;
- rejection of guarantee language;
- deterministic calibration drift across repeated runs;
- benchmark execution time as an observed performance metric.

## Quality thresholds

- Minimum deterministic case accuracy: **90%**.
- Maximum deterministic calibration drift: **0%**.
- Benchmark elapsed time is recorded as evidence; no machine-specific hard latency threshold is imposed.
- All benchmark cases must be repeatable.
- Any benchmark exception is a failed case.

## Architecture constraints

- No broker/live execution authority.
- No second canonical intelligence engine.
- No mutation of governance state from benchmark execution.
- Existing production contracts remain authoritative.
- Fail closed on malformed benchmark execution.

## Closure evidence

Closure requires the full project loop:

**تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**

Required evidence:

1. production benchmark implementation;
2. focused regression tests;
3. full unit suite;
4. debug build;
5. architecture/contract regression;
6. fresh CI on the exact current commit;
7. final audit confirming this document is `CLOSED`.
