# AMAR AI Agent — Master Architecture

## Mission
AMAR AI is a proprietary, trading-focused agent. It is vendor-neutral: Gemini and ChatGPT are optional adapters, never the core dependency.

## Layers

```text
AMAR AI UI
   |
   v
Agent Orchestrator
   |
   +--> Reasoning Backend (local-first / replaceable)
   +--> Tool Registry + Permission Scopes
   +--> Trading Knowledge / RAG
   +--> Multi-Source Research
   +--> Market Data Normalization
   +--> Strategy Lab + Backtesting
   +--> Validation / OOS / Stress / Robustness
   +--> Deterministic Risk Engine
   +--> Memory + Audit Ledger
   +--> Policy / Approval Gateway
   |
   +--> FUTURE: MT5 Command Gateway
```

## Non-negotiable boundaries
1. Language models never decide broker execution by themselves.
2. Risk checks are deterministic and outside the model.
3. Research claims carry provenance and confidence.
4. Strategy quality requires out-of-sample and cost-stress validation.
5. MT5 remains disabled until a later, separately verified milestone.
6. Build on existing AmarBoot modules; do not delete working functionality without migration and tests.

## Local AI direction
The production target is an on-device model runtime with a provider abstraction. Android local inference can be implemented through a native runtime such as llama.cpp, while keeping the application independent from a hosted API. The model is replaceable; the Agent Core is not.

## Research source policy
Sources are catalogued with title, URI, provenance, license/terms, authority class, retrieval time and evidence fingerprint. Public availability does not imply permission to copy proprietary code or data.

## Agent lifecycle
`Question -> Plan -> Retrieve -> Verify -> Reason -> Challenge -> Simulate -> Validate -> Risk Gate -> Explain -> Human Approval -> (future execution)`

## Quality target
A 9.9/10 target is a design goal, not a claim of profitability or guaranteed correctness. Readiness must be demonstrated by reproducible tests, benchmark suites, source verification, failure-injection tests and real-device performance measurements.
