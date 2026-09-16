# AMAR AI Agent — Architecture Map

## Mission
AMAR AI is a proprietary trading research and strategy agent. Its core is provider-neutral and currently uses the verified local reasoning fallback.

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
1. No vendor-specific external AI dependency in the core.
2. No API key is required for the core agent.
3. Research and analysis are read-only initially.
4. Strategy generation produces drafts, never automatic live trades.
5. Every external source has provenance and license metadata.
6. Broker/MT5 execution remains disabled until a separate authorization gate is deliberately implemented.
7. Reasoning is replaceable: the local provider is the current fail-closed implementation; future providers must be explicitly integrated and verified.

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
