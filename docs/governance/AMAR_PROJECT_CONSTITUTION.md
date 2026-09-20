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

## Article 14 — Independent Re-Verification and Closure Confidence

A closure decision MUST NOT be based solely on the latest successful addition, latest green CI result, latest test run, or latest implementation change.

For every stage, numbered item, feature, subsystem, or audit gate, the responsible engineer MUST evaluate the complete in-scope result as a whole against its authoritative requirements, contracts, architecture, tests, integration points, and acceptance criteria. The verification cycle MUST include independent re-checks of the completed result after the implementation evidence has been collected. Where technically applicable, this means repeating the relevant inspection/test/confirmation cycle at least twice, with the second pass explicitly checking that the first pass did not overlook a missing requirement, regression, integration defect, contradiction, or unverified capability.

A repeated check MUST be substantive and evidence-based; merely rerunning the same command without examining the resulting evidence does not satisfy this Article.

The final status MUST be determined from the complete evidence set, not from the newest successful component. If any required part is missing, contradictory, unverified, failing, stale, or outside the current commit, the official decision remains **قيد العمل / قيد التحقق النهائي** and the project MUST NOT advance or close that scope.

Only when all applicable requirements are individually accounted for, verified on the current implementation, cross-checked as an integrated whole, and supported by reproducible evidence may the responsible engineer issue the final **نجاح / جاهز / إغلاق** decision.

The final report MUST distinguish explicitly between:
- **تم** — the requirement is implemented and independently verified;
- **قيد العمل / قيد التحقق** — implementation or evidence is incomplete;
- **فشل** — a required verification or acceptance criterion failed;
- **محجوز للمرحلة اللاحقة** — intentionally deferred by the approved roadmap, without falsely treating it as completed in the current stage.

No percentage, success score, readiness statement, or stage-transition decision may conceal an incomplete required item. A 100% decision is permitted only when the entire applicable scope has passed the complete closure loop and the independent re-verification requirements of this Article.

## Article 15 — Binding Evidence Pipeline Steps

The following four steps are permanent project-law architecture for the evidence pipeline. They are mandatory, ordered, and may not be skipped, collapsed into one opaque operation, or implemented by a competing parallel subsystem.

### Step 1 — Rules
Define the authoritative admission rules and contracts that determine what an evidence item is allowed to be. Step 1 establishes the state model and invariants; it does not by itself claim runtime integration or completion.

### Step 2 — Evidence Intake
Receive and normalize the evidence candidates produced by retrieval. Step 2 separates raw/provider retrieval output from the canonical evidence pipeline so that candidates are not treated as accepted evidence merely because a provider returned them.

### Step 3 — Question Relevance
Determine whether each candidate is actually related to and answerable for the user's question. Relevance is a semantic evidence-boundary responsibility, not a source-authority, freshness, or trust substitute. Unrelated trusted sources MUST remain outside admitted evidence.

### Step 4 — Evidence Selection
Select the evidence that survives the relevance boundary for downstream quality, authority, freshness, independence, conflict, claim verification, confidence, reasoning, and final-answer processing.

The constitutional flow is therefore:

**User Question → Step 1 Rules → Step 2 Evidence Intake → Step 3 Question Relevance → Step 4 Evidence Selection → Downstream Verification/Reasoning → Final Answer**

No later step may silently perform the responsibility of an earlier step. Existing authoritative components MUST be integrated rather than duplicated, and each step MUST receive its own implementation, tests, CI evidence, and audit before that step is considered complete.

These definitions are binding even when individual implementation details evolve. Any change to their meaning requires an explicit constitutional amendment under Article 11.
