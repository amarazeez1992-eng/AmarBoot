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
