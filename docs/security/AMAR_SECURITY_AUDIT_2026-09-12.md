# AMAR Security & Engineering Audit — 2026-09-12

## Scope

Android application, AI layer, research/source governance, repository security,
MT5 integration boundary, and protected BOT 1 EA baseline.

## Findings

### HIGH — Protected EA baseline is not live-ready

`mt5/Experts/Grid_Martingale_Basket_v2.mq5` has non-zero defaults, BUY/SELL enabled
by default, a hard-coded lot ceiling, and destructive rebuild behavior. The source
remains protected and unchanged. The correct mitigation is to keep live authorization
blocked until a separately reviewed hardened execution path is verified.

### HIGH — Documentation drift

The previous B35 contract described an EA version and safety semantics that did not
match the committed source. The contract has been corrected to reflect the real 2.01
baseline and its limitations.

### HIGH — Public repository security baseline

A public repository must assume source visibility is permanent. Secrets must not be
stored in source control. Dependabot and CodeQL workflows were added, together with
secret-file exclusions and a SECURITY.md disclosure policy.

### MEDIUM — Third-party license/provenance risk

TradingView, GitHub, LuxAlgo and open-source bot ecosystems are discovery channels,
not blanket reuse permissions. AMAR now documents a license/provenance gate and keeps
third-party material outside the AMAR-owned license unless its original terms permit
reuse.

### MEDIUM — AI evidence confidence must not become trade-success probability

Research convergence is evidence quality only. Profitability claims require reproducible
AMAR datasets, out-of-sample testing, stress testing, realistic costs, and explicit
validation fingerprints.

### MEDIUM — Execution-state reporting

The UI must never treat queue acceptance as broker execution. The authoritative path
requires ACK, runtime read-back, reconciliation, and audit evidence.

## Controls added

- MIT license for AMAR-owned source with an explicit third-party notice.
- SECURITY.md vulnerability disclosure policy.
- CONTRIBUTING.md engineering/trading contribution rules.
- CODE_OF_CONDUCT.md.
- Dependabot configuration for Gradle, npm, and pip.
- CodeQL workflow for Kotlin/Java and TypeScript/JavaScript.
- Secret-oriented `.gitignore` exclusions.
- Open-source provenance/license policy.
- Corrected B35 MT5 contract.

## Protected artifact

The original EA remains unchanged. Verified baseline SHA-1:
`1125cabef6b4a4b1d5eb2c5deaa4b5e7458479ad25`.

## Verification rule

This audit is a source-level audit. It is not a claim of live-trading safety.
The project remains demo/paper constrained until the complete MT5 ACK/read-back/
reconciliation path is demonstrated in a controlled terminal.
