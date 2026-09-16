# Amar AI Stage Sequence Constitution

## Binding execution rule
From project start to final closure, every stage follows:
**Review → Inspect → Correct root cause → Test → Verify → Evidence → Final audit → Close → Re-check previous stages → Proceed**

No later stage is closed while an earlier stage has an unresolved defect, failed test, unverified integration, or missing evidence.

## Change protection
- Preserve the approved architecture and strategy.
- Do not delete requirements or features merely to satisfy CI.
- Replace implementations only when the approved requirement remains intact and correctness, safety, or maintainability improves.
- Sensitive strategic or architectural changes require explicit owner approval.

## Authority
- Amar AI Agent is the central application authority.
- UI is presentation/interaction and is not a competing authority.
- External AI providers are optional adapters, never the core authority.
- Gemini is permanently excluded.
- MT5 remains deferred until its assigned final stage.

## Evidence rule
Every capability must be **Implemented → Integrated → Tested → Verified → Evidenced → Audited → Closed**.
Anything lacking real evidence remains **NOT VERIFIED / FAIL-CLOSED**.

## Recovery rule
When a defect is found, return to the earliest affected stage, fix the root cause, rerun required verification, then continue forward. This applies to every branch, module, engine, UI boundary, permission boundary, memory boundary, provider boundary, multimodal capability, build artifact, release check, and integration.
