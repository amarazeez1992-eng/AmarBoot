# AMAR AI Agent — Architecture Map

## Mission
AMAR AI is a proprietary trading research and strategy agent. Its core does not depend on Gemini or ChatGPT.

## Layers

```text
                    ┌─────────────────────────┐
                    │       AMAR AI UI        │
                    │ Chat / Research / Lab   │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │    AGENT ORCHESTRATOR   │
                    │ plan → tools → verify   │
                    └────────────┬────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          ▼                      ▼                      ▼
 ┌────────────────┐     ┌────────────────┐     ┌────────────────┐
 │ Reasoning      │     │ Trading Tools  │     │ Agent Memory   │
 │ Local / Plug-in│     │ Research       │     │ Facts / prefs  │
 └───────┬────────┘     │ Indicators     │     │ decisions      │
         │              │ Strategies     │     └────────────────┘
         │              │ Simulation     │
         │              │ Risk Audit     │
         │              └───────┬────────┘
         │                      │
         └──────────────┬───────┘
                        ▼
               ┌─────────────────┐
               │ Knowledge Mesh  │
               │ sources/catalog │
               │ provenance      │
               │ license gates   │
               └────────┬────────┘
                        ▼
               ┌─────────────────┐
               │ Policy Gateway  │
               │ permissions     │
               │ audit / safety  │
               └────────┬────────┘
                        │
                 CURRENT: BLOCKED
                        │
               ┌────────▼────────┐
               │ Future MT5 Gate │
               │ approval + risk │
               └─────────────────┘
```

## Core principles
1. No Gemini dependency in the core.
2. No ChatGPT dependency in the core.
3. API keys are never required for the core agent.
4. Research and analysis are read-only initially.
5. Strategy generation produces drafts, never automatic live trades.
6. Every external source has provenance and license metadata.
7. Broker/MT5 execution remains disabled until a separate authorization gate is deliberately implemented.
8. A reasoning provider is replaceable: local model first, optional external adapters later.

## Planned modules
- `agent/` — orchestration and provider-neutral contracts
- `knowledge/` — trading knowledge, source catalog, provenance
- `research/` — multi-source research and evidence ranking
- `strategy/` — strategy specifications and validation
- `simulation/` — backtesting and stress testing interfaces
- `memory/` — approved persistent agent memory
- `policy/` — permissions, safety and approval gates
- `execution/` — reserved for the future MT5 command gateway

## Current milestone
The first independent core is now present under `app/.../amaros/agent/` with a local no-key reasoning fallback and a read-only trading tool registry.
