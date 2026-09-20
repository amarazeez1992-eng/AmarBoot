# AMAR AI AGENT — MASTER CONSTITUTION

Document ID: AMAR-AI-MASTER-CONSTITUTION
Status: MASTER SOURCE OF TRUTH

> This file is the canonical project constitution supplied by the project owner. It is intentionally maintained as an editable Markdown document so future developers and AI systems can read, review, propose controlled changes, and extend it without replacing or bypassing the governing architecture.

## Governance

- Single Source of Truth.
- No real additions outside the constitution.
- Any new idea must be understood, analyzed, assigned to a Stage, added through a new constitution version, impact reviewed, tested, and not added randomly.
- AMAR AI = Central AI Agent + Research System + Intelligence Platform + Knowledge System + Trading Intelligence + Engineering Platform.
- Hierarchy: Generation → Stage → Section → Item → Point → Engine → Service → Input → Process → Output → Validation → Test → Evidence → Audit → Lifecycle.

## Non-Negotiables

- No hallucinated source, search, test, result, code, data, confidence, execution, or access.
- Important results must be traceable Source → Evidence → Claim → Analysis → Validation → Result.
- No blind majority vote.
- No forced BUY/SELL.
- Forecasts are not facts.
- No market guarantees.
- Preserve architecture and contracts.
- Do not simplify, delete, or replace existing capability merely to make implementation easier.

## Stage 7 — Evidence Engine

Official ordering:

Evidence Intake → Source Quality Analysis → Authority Scoring → Freshness Engine → Source Independence → Duplicate Evidence Detection → Fingerprint Integrity → Evidence Tampering Detection → Evidence Uniqueness → Evidence Quality Score → Evidence Status Classification → Evidence Ranking → Evidence Explanation → Evidence Conflict Awareness → Claim ↔ Evidence Verification → Confidence Calibration Input → Invalid/Future Evidence Protection → Deterministic Evidence Handling → Historical Validation → Cross-Source Correlation → Evidence Chain → Evidence Change Detection → Evidence Lifecycle → Evidence Audit Trail.

## Stage 11 — Advanced Evidence Intelligence

Stage 11 is governed by the same evidence integrity rules. Current project work is at:

Stage 11 → Item 3 → Point 10

This point is not considered complete until its required implementation, tests, evidence, audit, and acceptance gates are actually satisfied.

## Testing and Definition of Done

Definition of Done:

Implemented, Integrated, Tested, Failure Tested, Security Checked, Regression Tested, Documented, Audited, Accepted, Stable.

No component is considered complete merely because code exists or a single test passes.

## Governance Stages and Cross-Cutting Rules

- Stage 1: Foundation.
- Stage 7: Evidence Engine.
- Stage 11: Advanced Evidence Intelligence.
- Stage 25: Testing.
- Stage 46: Security.
- Stage 48: Code Management.
- Stage 64: Mobile-First.
- Stage 67: Constitution Change Control.
- Stage 75: Trading UI.
- Stage 80+: Lifecycle and higher-order governance.
- Stage 97: Final rules.

Later implementation plans, stage execution documents, test/closure documents, and technical specifications are implementation artifacts; they do not override this constitution.

## Query Policy Architecture

The Agent uses one Agent Engine with a Query Policy layer rather than multiple competing Agents.

User Input
→ Intent Understanding
→ Query Policy Classifier
→ one of:
- GENERAL / LOCAL
- GENERAL FACTUAL
- FINANCIAL / TRADING
→ existing Agent Engine, Orchestrator, and Evidence Engine
→ Final Response.

Rules:

- General Conversational requests may be handled locally.
- General Factual requests may use light research and must disclose uncertainty when verification is insufficient.
- Financial/Trading requests require strict evidence handling and must not guess.
- Identity, time, date, and greeting requests are deterministic local intents.
- Do not implement this policy as a raw financial-keyword gate.
- Do not introduce arbitrary confidence thresholds.
- Do not create a second Agent or duplicate the research/verification pipeline.
- Keep strict financial validation tied to the existing constitutional Evidence Engine.

## Runtime Architecture

Canonical path:

User → amar_reference.html → JavaScript ask() → Android bridge → MainActivity → AmarAiAgentEngine → AmarAgentOrchestrator → planner/research/evidence/reasoning/verification → callback → WebView receiveAgent() → visible response.

The Android/WebView bridge must preserve request delivery, result delivery, timeout/error handling, and renderer-death recovery without inventing responses.

## Project Identity and Product Scope

- Product name: Amar AI.
- The project includes a central AI Agent, research and evidence processing, knowledge/intelligence capabilities, trading intelligence, engineering capabilities, and a mobile-first UI.
- The trading interface is primarily intended for gold and forex/scalping use cases.
- Trading answers must remain evidence-driven and must never fabricate live prices, sources, executions, or confidence.
- The UI is the new Amar AI interface; rejected legacy visual concepts must not be silently restored.

## Change Control

Every material addition or correction must:
1. Identify the problem or requirement.
2. Identify the responsible Stage/Section/Item/Point.
3. Implement the change without destroying existing architecture or contracts.
4. Add or update regression tests where applicable.
5. Record the correction/addition in the project documentation.
6. Update the constitution version/change history when the governing rules themselves change.
7. Verify integration, failure behavior, security impact, regression behavior, and auditability.

## Editable Extension Area

Future amendments must be appended as versioned amendments. Do not silently rewrite historical rules. Each amendment should state:
- Amendment ID
- Date
- Reason
- Affected Stage/Section/Item/Point
- Exact change
- Impact analysis
- Tests
- Approval/acceptance state

## Version / Change History

- v1 — Master constitution established as the project governing source.
- Subsequent amendments must be recorded here; historical entries must remain intact.

---
END OF MASTER CONSTITUTION
