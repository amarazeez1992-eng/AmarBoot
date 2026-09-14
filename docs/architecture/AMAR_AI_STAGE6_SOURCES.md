# AMAR AI Stage 6 — Source Provenance

Stage 6 only admits market-intelligence components through explicit provenance and license metadata.

## Verified candidate

| Component | Repository | License | Status |
|---|---|---|---|
| TA-Lib | https://github.com/TA-Lib/ta-lib | BSD-3-Clause | Candidate adapter; not bundled yet |

TA-Lib's repository LICENSE grants redistribution/use under BSD-style conditions. The Agent records the license as `BSD-3-Clause` and requires an HTTPS repository for external indicator sources.

## Current built-in engine

`AmarBuiltInIndicatorAdapter` is provider-neutral and read-only. It currently implements deterministic SMA, EMA, RSI and ATR calculations without requiring a network service or API key.

## Admission rules

1. Every external source must have source ID, name, version, homepage, repository, source type and SPDX license metadata.
2. Default admission allows MIT, Apache-2.0, BSD-2-Clause and BSD-3-Clause.
3. Unknown or incompatible licenses fail closed.
4. Conflicting provenance for an existing source ID fails closed.
5. Indicator calculations must be deterministic and finite.
6. No Stage 6 component may access broker execution.
7. Adding a third-party library requires a separate dependency review; provenance documentation alone does not authorize bundling it.
