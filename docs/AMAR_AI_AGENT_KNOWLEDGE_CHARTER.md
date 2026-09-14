# AMAR AI Agent — Institutional Knowledge & Self-Expansion Charter

Status: ACTIVE DESIGN CONTRACT

## Mission
AMAR AI is the central director of a trading-intelligence system. It is not an indicator calculator and it must not be architected around a fixed list of indicators.

## Scope of intelligence
The Agent may discover, evaluate, index and use:
- technical indicators and indicator families;
- TradingView/Pine open-source material where reuse rights are actually established;
- quantitative/statistical methods;
- market-structure and price-action schools;
- systematic trading and portfolio methods;
- backtesting and simulation engines;
- market-data and alternative-data tools;
- academic papers and reproducible research;
- public institutional research and official market/regulatory publications;
- open-source libraries, repositories, datasets and analytical frameworks;
- new engines discovered by the research workforce.

TradingView is treated as a discovery/source domain, not as permission to copy arbitrary scripts. TradingView documents a large Community Scripts ecosystem and identifies open-source scripts by their source availability; Pine libraries and scripts have distinct reuse rules. The Agent therefore records the exact source, author/publisher, version, license/reuse basis, URL, retrieval time and fingerprint before admitting reusable code. citeturn0search10turn0search13turn0search3

## MCB — Market/Capability Brain
MCB is the Agent's capability-discovery layer. It continuously maintains a catalog of:
1. sources;
2. libraries;
3. indicator engines;
4. strategy families;
5. research methods;
6. datasets;
7. parsers/connectors;
8. simulation/backtest engines;
9. validation methods;
10. internal specialists.

MCB never silently promotes an unverified discovery into trusted knowledge.

## Self-expansion loop
DISCOVER → FETCH METADATA → PROVENANCE CHECK → LICENSE/REUSE CHECK → SECURITY/DEPENDENCY CHECK → STATIC/UNIT VALIDATION → DOMAIN VALIDATION → BENCHMARK → ADVERSARIAL REVIEW → ADMISSION → VERSIONED KNOWLEDGE RECORD → SCHEDULED RECHECK.

A new source may be automatically indexed without becoming executable/trusted. Promotion to trusted/executable capability requires all applicable gates to pass.

## Internet research
The research layer is designed for broad multi-source search rather than a hard-coded source list. GitHub's public repository APIs support discovery of public repositories, and code-scanning infrastructure exists for security analysis. These capabilities are used as evidence sources/connectors, not as blanket trust. citeturn0search2turn0search14

## Internal workforce
DIRECTOR/ORCHESTRATOR
→ RESEARCH SCOUTS
→ SOURCE & PROVENANCE GUARD
→ QUANT RESEARCHER
→ MARKET STRUCTURE RESEARCHER
→ INDICATOR/LIBRARY ENGINEER
→ STRATEGY ENGINEER
→ DATA ENGINEER
→ BACKTEST/EXPERIMENT ENGINE
→ RISK GUARD
→ CRISIS GUARD
→ ADVERSARIAL REVIEWER
→ DECISION CONFIRMATION
→ AUDITOR
→ EXECUTION GOVERNOR (disabled until explicit delegation).

The Director delegates tasks and aggregates independent evidence. No specialist may bypass provenance, security, risk, audit, or execution gates.

## Institutional reporting
Every research task can produce a versioned report containing:
- question and interpretation;
- sources searched;
- source quality and provenance;
- conflicting evidence;
- methods/indicators/strategies found;
- reproduced calculations or experiments;
- assumptions;
- validation results;
- failures and rejected candidates;
- Agent opinion and confidence;
- dissenting internal opinions;
- recommended next experiments;
- knowledge updates;
- immutable audit references.

## Self-update policy
The Agent may automatically refresh metadata and search for newer versions. It may automatically store verified metadata and research records. It must not silently replace a trusted executable engine with newly discovered code. Such upgrades are staged, benchmarked, security-checked, regression-tested and canary-validated before promotion.

## Reliability principle
"Open source" does not mean "trusted", "accurate", "profitable", or "safe". A license grants reuse rights; it does not validate trading claims. Every admitted capability retains provenance and validation status.

## Performance principle
The system uses parallel workers where safe, deterministic aggregation, bounded resource budgets, caching, fingerprints and incremental refresh. Resource budgets limit compute/network consumption; they do not limit the conceptual scope of Agent intelligence.

## Execution boundary
Research and simulation can be broad. Live broker/device execution remains a separate capability and is locked until explicit user delegation and the execution governance stage is approved.
