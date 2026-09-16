# Amar AI Stage Sequence Constitution

## Binding execution rule
From the beginning of the project through final closure, every stage MUST follow this cycle:

**Review → Inspect → Correct root cause → Test → Verify → Evidence → Final audit → Close → Re-check previous stages → Proceed**

No later stage may be declared closed while an earlier stage has an unresolved defect, failed test, unverified integration, or missing evidence.

## Change protection
- The architecture and approved strategy are preserved.
- No requirement may be deleted merely to satisfy CI.
- No feature may be silently removed.
- Any genuinely obsolete implementation must be replaced only when the replacement preserves the approved requirement and improves correctness, safety, or maintainability.
- Sensitive architectural or strategic changes require explicit owner approval.

## Authority
- Amar AI Agent is the central application authority.
- UI is presentation/interaction and must not become a competing authority.
- External AI providers are optional adapters and never the core authority.
- Gemini is permanently excluded from the architecture.
- MT5 remains deferred until its explicitly assigned final stage.

## Evidence rule
A capability is considered complete only when it is:

**Implemented → Integrated → Tested → Verified → Evidenced → Audited → Closed**

Anything lacking real backend/test evidence remains **NOT VERIFIED / FAIL-CLOSED**.

## Stage progression
Stages are evaluated in order. When a defect is found, execution returns to the earliest affected stage, repairs the root cause, reruns the complete required verification, and only then continues forward.

This rule applies to every branch, module, engine, UI boundary, permission boundary, memory boundary, provider boundary, multimodal capability, build artifact, and release check.
