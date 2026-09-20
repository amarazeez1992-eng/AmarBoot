# ERR-INT-002 — Intent classification lost Arabic morphology and financial instruments

**Status:** Corrected  
**Area:** Intent Understanding / Query Policy boundary  
**Detected:** 2026-09-20

## Symptom

Three regression tests failed after introducing Query Policy:

1. `حلل الذهب XAUUSD الآن` did not reliably enter the financial/trading policy.
2. `ابحث وحلل XAUUSD` could be classified as general research because the research verb dominated and the instrument was not recognized as a trading entity.
3. `ما هي عاصمة فرنسا؟` lacked an explicit Arabic factual-question signal.

## Root cause

The intent scorer intentionally stopped using broad substring matching to prevent false positives, but the replacement token matching was too literal:

- Arabic definite articles such as `الذهب` did not match the canonical concept `ذهب`.
- Trading symbols such as `XAUUSD` were not represented as trading entities.
- The Arabic factual form `ما هي` was not represented in the research concept vocabulary.

This was an intent-understanding defect, not an Evidence Engine defect and not a reason to weaken the financial evidence gate.

## Correction

The correction is implemented at the intent-understanding layer:

- Arabic definite-article normalization is applied only when comparing canonical single-word concepts.
- Trading instruments are detected as structured entities/patterns and contribute to trade intent.
- `XAUUSD` and `XAGUSD` are exposed as entities.
- `ما هي` is recognized as a factual-research signal.
- Query Policy remains unchanged: only `TRADE_ANALYSIS` activates strict financial evidence handling.

## Regression protection

`AmarIntentClassificationRegressionTest.kt` covers the three failure classes.

## Architectural rule

Do not fix this class of failure by weakening Query Policy, adding raw keyword gates to the orchestrator, or bypassing Evidence Engine validation. Correct linguistic/entity understanding at the Intent Understanding layer and keep policy decisions downstream.
