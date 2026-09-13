# AMAR Trading Platform — Approved Mobile Architecture

**Status:** APPROVED
**Scope:** Android/mobile trading platform only. Laptop/CMG/Bridge/MT5 write execution remains a later phase.
**Approved by:** Amar
**Principle:** modular, advanced, governed, evidence-driven, no random additions.

## 1. Product identity

AmarBoot is approved to evolve into:

**AMAR Trading Platform — Advanced Personal Trading Platform**

The mobile application is a complete trading workstation and intelligence platform, not merely a bot remote control.

## 2. Mandatory architecture rule

Every approved capability must have its own bounded module, package/folder, specification, implementation files, tests, and audit boundary where applicable.

No feature may be added as an unrelated screen, helper, registry entry, prompt-only capability, or cross-cutting shortcut.

Existing stable architecture must not be rewritten merely to add these modules.

## 3. Approved module map

```text
AMARBOOT
└── Trading Platform
    ├── Trading Terminal
    ├── Charts
    ├── Manual Trading
    ├── Grid Engine
    ├── Tracking Engine
    ├── Risk Manager
    ├── Portfolio
    ├── Strategy Lab
    ├── Trading Journal
    ├── Alert Center
    ├── Bot Vault
    ├── AI Copilot
    ├── Market Intelligence
    ├── Position Command Center
    ├── Trade Cockpit
    ├── Risk Simulator
    ├── Strategy Library
    ├── Replay Mode
    └── Security / Governance
```

## 4. Trading Terminal

Dedicated responsibility for the professional trading workspace:
- live market snapshot when a trusted data source exists;
- watchlists;
- instruments including XAUUSD, FX and supported markets;
- bid/ask/spread;
- timeframe selection;
- market-state presentation;
- price alerts;
- terminal-level status and data-source indicators.

No invented market values are permitted.

## 5. Charts

Dedicated chart subsystem:
- advanced candlestick presentation;
- timeframe navigation;
- support/resistance levels;
- trendlines;
- price levels;
- drawing objects;
- chart-linked alerts;
- real-data source labeling.

Chart calculations must use actual supplied market data.

## 6. Manual Trading

Dedicated governed manual-trading interface:
- Buy/Sell;
- Market/Pending;
- volume/lot;
- SL/TP;
- break-even;
- trailing stop;
- partial close;
- close all/profit/loss;
- batch modification;
- validation before command creation.

Android phase remains non-broker-writing until the final bridge phase.

## 7. Grid Engine

Dedicated grid engine package:
- Fixed Grid;
- Dynamic Grid;
- Buy/Sell/Both;
- order count;
- distance;
- base lot;
- manual lot progression;
- lot multiplier;
- basket TP/SL;
- individual TP/SL;
- rebuild;
- stop/resume;
- deterministic planning and validation.

The existing approved grid-planning foundation must be reused rather than duplicated.

## 8. Tracking Engine

Dedicated tracking subsystem:
- price tracking;
- position tracking;
- dynamic orders;
- entry/exit conditions;
- trailing;
- basket management;
- read-only runtime evidence during Android phase;
- fail-closed behavior when trusted runtime data is unavailable.

## 9. Risk Manager

Dedicated risk-control subsystem:
- daily loss limit;
- daily profit target;
- maximum exposure;
- maximum open positions;
- maximum lot;
- maximum drawdown;
- equity protection;
- floating P/L protection;
- emergency stop;
- risk-per-trade calculation;
- risk calculator;
- margin calculator;
- spread protection.

Risk Manager is a policy/control boundary, not an execution bypass.

## 10. Portfolio

Dedicated account/portfolio intelligence:
- balance;
- equity;
- margin;
- free margin;
- floating P/L;
- exposure;
- positions;
- pending orders;
- account history;
- daily/weekly/monthly performance.

All account data must be sourced from trusted runtime/account providers.

## 11. Strategy Lab

Dedicated strategy research environment:
- create/edit strategies;
- save versions;
- test;
- compare;
- backtest;
- forward test;
- walk-forward;
- stress test;
- optimization;
- performance analysis;
- version lineage.

Results must be measured or explicitly marked unavailable. No fabricated performance.

## 12. Trading Journal

Dedicated trade-journal subsystem:
- trade record;
- entry reason;
- strategy association;
- screenshot/reference;
- result;
- P/L;
- R:R;
- win rate;
- profit factor;
- drawdown;
- time-of-day analysis;
- strategy statistics;
- trader-error analysis.

## 13. Alert Center

Dedicated alert subsystem:
- price;
- profit/loss;
- drawdown;
- spread;
- margin;
- candle condition;
- strategy condition;
- grid condition;
- position condition;
- AI analysis result.

Alerts are separate from execution authority.

## 14. Bot Vault

Dedicated bot configuration and lifecycle storage:
- BOT 1..N configuration slots;
- strategy assignment;
- risk profile;
- grid/tracking settings;
- history;
- performance;
- start/stop state;
- configuration versions.

A Bot entry is not considered a real independent trading engine until its engine is implemented, tested and approved.

## 15. AI Copilot

Dedicated trading AI assistant:
- market analysis;
- candle analysis;
- trend analysis;
- volatility;
- risk;
- position analysis;
- grid analysis;
- strategy analysis;
- trade review;
- journal analysis;
- scenario explanation;
- recommendations.

The AI must consume evidence from real application engines. Registry/catalog/prompt text is not execution proof.

Autonomous execution remains governed by explicit authority boundaries.

## 16. Market Intelligence

Dedicated evidence-producing intelligence layer for:
- market state;
- volatility;
- anomaly detection;
- candle evidence;
- source quality;
- uncertainty;
- confidence;
- market context.

It must integrate with existing Amar AI evidence/memory architecture instead of creating a second competing intelligence registry.

## 17. Trade Cockpit

Dedicated high-density professional workspace combining:
- price;
- chart;
- positions;
- risk;
- Buy/Sell controls;
- Grid controls;
- AI Copilot.

It is a composition layer. Trading logic remains owned by the underlying modules.

## 18. Position Command Center

Dedicated multi-position command layer:
- multi-select positions;
- batch SL modification;
- batch TP modification;
- partial close;
- close all selected;
- break-even;
- trailing;
- command preview and validation.

It creates governed commands; it does not bypass execution security.

## 19. Risk Simulator

Dedicated what-if engine:
- pre-trade loss estimation;
- equity impact;
- margin impact;
- exposure impact;
- SL scenario;
- multiple-position scenario;
- grid scenario.

The simulator must clearly distinguish calculated scenarios from real account state.

## 20. Strategy Library

Dedicated internal strategy library:
- save;
- copy;
- edit;
- version;
- test;
- compare;
- approve;
- archive.

This is an internal governed library, not an external marketplace and not an execution authority.

## 21. Replay Mode

Dedicated historical replay/training environment:
- historical data playback;
- simulated live clock;
- manual decisions;
- strategy testing;
- journal capture;
- replay statistics.

Replay results must never be presented as live broker results.

## 22. Security / Governance

All modules remain subject to the existing governance chain:

`Authentication → Authorization → Command → Validation → Accept → Execute → ACK → Verify → Reconcile → Audit`

During Android-only development, the final broker-writing stages remain disabled.

## 23. Cross-module rules

1. No module may silently mutate another module's state.
2. Shared contracts must live in explicit contract packages.
3. UI is not an execution authority.
4. Registry/catalog is not evidence of runtime state.
5. AI cannot invent missing financial or market values.
6. Failed/unavailable evidence must be surfaced explicitly.
7. Every financial command must be validated before entering the durable command queue.
8. Every future broker execution must be idempotent, authenticated, scoped, TTL-bound and auditable.
9. MT5 protected files remain outside Android feature work.
10. No feature is considered complete until implementation, tests and audit evidence exist.

## 24. Implementation order

The approved implementation order is:

1. Architecture/contracts and module boundaries.
2. Trading Terminal + Charts.
3. Manual Trading command models and validation.
4. Grid Engine hardening.
5. Tracking Engine hardening.
6. Risk Manager.
7. Portfolio.
8. Trading Journal.
9. Alert Center.
10. Strategy Lab + Strategy Library.
11. Position Command Center + Risk Simulator.
12. Replay Mode.
13. Trade Cockpit.
14. Market Intelligence integration.
15. AI Copilot real-tool integration.
16. Application-wide tests and security audit.
17. Mobile Trading Ready gate.
18. Only then: Laptop → CMG → Bridge → MT5.

## 25. Approval status

The complete module list and the additional proposals A–F are **APPROVED PROJECT ARCHITECTURE**.

Approval means these capabilities are part of the AmarBoot mobile target and may be implemented under the modular rules above. It does not mean every capability is already implemented or broker-live.
