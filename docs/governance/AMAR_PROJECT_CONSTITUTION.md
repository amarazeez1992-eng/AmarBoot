# AMAR PROJECT CONSTITUTION

**Status: BINDING PROJECT LAW**

This document is the mandatory engineering and governance agreement for Amar AI / AmarBoot. It is binding for all future project work, stages, numbered items, fixes, audits, and closure decisions.

## Article 1 — No Closure Without Proof

No stage, numbered item, feature, subsystem, audit gate, or milestone may be marked **CLOSED**, **READY**, **GREEN**, or equivalent unless the complete mandatory closure loop has been executed and evidenced.

## Article 2 — Mandatory Closure Loop

Every numbered project item MUST pass, in this exact order:

**تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**

English equivalent:

**investigate → implement → inspect → test → confirm → prove → final audit → ready → close**

Skipping, merging away, or assuming any step is prohibited when the step is required to establish evidence.

## Article 3 — Evidence Is the Authority

Previous successful CI runs, previous approvals, previous green commits, or earlier audits MUST NOT be treated as proof for a newly modified implementation. After a substantive correction, the affected item must be re-verified against the current code and current commit.

A historical success remains historical evidence only; it cannot substitute for fresh verification of changed code.

## Article 4 — No Premature Closure

Implementation alone never equals completion.

A test file alone never equals completion.

A successful historical CI run never equals completion after later changes.

A design document never equals completion.

A human assumption never equals completion.

Closure requires current, traceable evidence for the applicable gates.

## Article 5 — Defect Discipline

Every discovered defect must be corrected at its root cause and converted into a permanent regression guard whenever technically applicable.

The same failure mode must not be knowingly repeated.

## Article 6 — Re-Inspection Is Mandatory

After every repair or substantive modification, the changed production files, tests, contracts, and affected workflow must be re-inspected before closure is considered.

## Article 7 — Verification Gates

Unless a stricter stage-specific gate exists, closure requires at minimum:

1. focused tests for the changed item;
2. full relevant unit/regression tests;
3. build/compile verification where applicable;
4. CI verification on the current implementation/commit;
5. architecture and contract consistency review;
6. final audit confirming that no known critical/high blocker remains in scope;
7. recorded evidence sufficient to reproduce the closure decision.

## Article 8 — Stage Progression Lock

The project may not advance to the next numbered item merely because the current implementation exists. The current item must first reach **جاهز** through the complete closure loop and then be explicitly **إغلاق** with evidence.

If evidence is missing, the official state is **قيد التحقق النهائي** (final verification pending), not CLOSED.

## Article 9 — Truthful Status

Project status must reflect the verified state of the current repository, not optimism, intention, or an earlier state.

When evidence is incomplete, the status must remain open/pending verification.

## Article 10 — Governance Hierarchy

This Constitution is the binding project-level rule for engineering closure discipline. Stage-specific governance documents may impose stricter requirements, but may not weaken these rules.

The roadmap is the status tracker; it must not override evidence or this Constitution.

## Article 11 — Change Control

Any change to this Constitution must be explicit, documented, and committed to the repository. A conversational promise alone does not amend project law.

Until formally changed, these rules remain in force for the project.

## Article 12 — Mandatory Audit Phrase

Before any closure decision, the responsible engineer must be able to demonstrate:

**تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**

If any required proof is absent, closure is forbidden.

## Article 13 — Architectural Integrity and Professional Standard

All Amar AI / AmarBoot engineering work MUST remain within the approved project context, architecture, contracts, governance documents, and AI-agent design authority. No implementation may be simplified, weakened, generalized, or altered merely for convenience when doing so changes the intended architectural meaning or contract.

Every modification, correction, extension, refactor, test, workflow change, or governance change MUST be executed to the highest professional engineering standard reasonably applicable to the scope. The responsible engineer MUST preserve architectural coherence, explicit contracts, deterministic behavior where required, safety boundaries, traceability, maintainability, and regression protection.

The AI Agent MUST NOT invent a parallel subsystem, duplicate an existing authority, bypass an established contract, silently change semantics, remove a required guard, or introduce execution authority merely to satisfy a local test or short-term objective. When an existing authoritative component already owns a responsibility, the default is to extend or correctly integrate with that authority rather than create a competing implementation.

Any ambiguity, missing requirement, or conflict MUST be resolved by inspecting the authoritative repository contracts and governance documents before implementation. Assumptions MUST NOT be used to lower engineering quality or bypass required verification.

This Article is a permanent project-law requirement and applies to all future stages, items, fixes, audits, AI-agent changes, and closure decisions unless the Constitution is explicitly amended under Article 11.

## Article 14 — Absolute Completeness, No Deletion, No Simplification

A stage, item, feature, audit, or milestone MUST NOT be closed if there is any known missing requirement, incomplete implementation, unverified dependency, untested path, unresolved defect, or missing evidence within its approved scope.

**Deletion, omission, reduction of scope, or simplification is prohibited as a method of obtaining closure.** Required functionality, contracts, guards, tests, evidence, architecture, and governance controls MUST NOT be removed, weakened, hidden, bypassed, or redefined merely to make a test pass, eliminate an error, shorten implementation, or obtain a READY/CLOSED status.

If a defect cannot be safely corrected without changing an approved requirement or architectural contract, closure MUST remain blocked until the change is explicitly reviewed, documented, tested, and approved under the project governance hierarchy.

After every substantive change, a completeness audit MUST confirm that the correction introduced no deletion, omission, simplification, regression, contract weakening, or loss of previously required behavior. The audit MUST compare the current implementation against the authoritative scope, contracts, tests, workflow gates, and prior accepted requirements.

No stage may be declared successful merely because its failing component was removed, disabled, skipped, downgraded, excluded from CI, or replaced by a weaker substitute.

This Article is constitutional, permanent, and mandatory for every future stage, item, addition, correction, audit, AI-agent action, and closure decision unless the Constitution is explicitly amended under Article 11.

## Article 15 — Supreme Engineering Quality Gates

The following numbered quality gates are constitutional additions and MUST be applied to the corresponding project stage or to any later stage that performs the same responsibility. They raise the minimum engineering bar; they never authorize deletion, simplification, or scope reduction.

1. **Contract & Architecture Gate** — Every stage must verify explicit contracts, architectural ownership, integration boundaries, and absence of conflicting authorities before READY.
2. **Memory Integrity Gate** — Any memory/state subsystem must prove persistence integrity, retrieval correctness, consistency, corruption handling, and regression protection.
3. **Research Integrity Gate** — Research must verify source quality, source diversity/independence, contradiction handling, deduplication, provenance, and resistance to unsupported conclusions.
4. **Decision Safety Gate** — Decision Intelligence must remain bounded and proposal-only unless separately authorized by an explicit execution architecture; risk limits and unavailable evidence must fail safely.
5. **Adversarial Self-Critique Gate** — Self-critique must be tested against deliberate invalid, conflicting, unsupported, overconfident, numeric, and guarantee-style claims and must reject unsafe conclusions.
6. **Benchmark Certification Gate** — Benchmarks must measure current contracts, determinism/repeatability where required, regression resistance, boundary behavior, and architecture compliance; benchmark success cannot be achieved by weakening the implementation.
7. **Observability Gate** — Critical behavior must expose sufficient structured logs, metrics, health signals, diagnostics, and traceability to detect, localize, and audit failures without relying on hidden state.
8. **99% Certification Gate** — The final quality gate must independently verify the complete approved scope, all mandatory evidence, regression status, architecture, safety, and closure chain before the project may claim the 99% target.
9. **Agent Safety Boundary Gate** — Agent reasoning, memory, tools, storage, external access, and execution authority must remain explicitly separated by contracts and permissions; no implicit execution authority is allowed.
10. **Full-System Chaos & Failure Testing Gate** — Before final closure, the system must be tested against relevant failures including invalid/corrupt input, missing evidence, dependency failure, timeout, interruption, duplication, partial failure, and recovery paths, without disabling the affected capability.
11. **Independent Final Audit Gate** — Final audit must reassess the complete project from the authoritative baseline rather than assuming earlier CLOSED labels are sufficient; every material claim must be traceable to current evidence.

These 11 gates are permanent constitutional requirements unless explicitly amended under Article 11.

## Article 16 — Supreme Engineering Laws

The following seven laws apply globally to every stage, item, subsystem, workflow, agent action, test, audit, and closure decision:

1. **Zero Silent Failure** — No critical failure may disappear silently. Failures must be surfaced, classified, and traceable.
2. **Fail-Closed by Default** — When required evidence, integrity, authorization, or safety conditions are missing or uncertain, the system must stop the unsafe path rather than guess or proceed with hidden risk.
3. **No Evidence = No Trust** — Claims of correctness, quality, safety, completion, or authorization require appropriate current evidence.
4. **Deterministic Where Required** — Behavior that is required to be reproducible must not depend on uncontrolled randomness, hidden mutable state, or nondeterministic ordering.
5. **No Single Point of Truth Drift** — A responsibility must have one authoritative source of truth unless an explicit architecture defines synchronization and conflict resolution.
6. **Every Change Has a Proof Obligation** — Every material change must establish why it was made, what changed, what could regress, what was tested, and what current evidence proves the result.
7. **Final Audit Starts From Zero Assumptions** — The final audit must not inherit trust merely from prior approvals; it must independently verify the current repository against the authoritative requirements.

These seven laws are permanent constitutional requirements unless explicitly amended under Article 11.

## Article 17 — Long-Term Project Agreement

The constitutional additions in Articles 15 and 16 are part of the permanent Amar AI / AmarBoot engineering agreement and must be treated as long-term project baseline requirements in all future work. The project may not claim the target engineering level while knowingly bypassing any applicable numbered gate or supreme law.
