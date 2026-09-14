# Stage 6 Regression Guard

Permanent engineering rules:

1. Never create duplicate canonical contracts in the same package. Search before adding any symbol; use one canonical contract plus explicitly named adapters.
2. Never admit a discovered source/capability on existence or score alone. Trust requires provenance, license/reuse basis, security/dependency review, deterministic tests, domain validation, benchmark, regression compatibility, and adversarial review as applicable.
3. Never fabricate source counts, benchmark results, accuracy, latency, confidence, or validation claims.
4. Never silently replace a trusted version with an unverified version. Keep version history and rollback/deprecation paths.
5. When a defect pattern can repeat across modules, fix the shared boundary and add a regression test instead of repeating local patches.
6. Stage 6 is not closed because scaffolding or a unit test exists. Closure requires implementation plus integration/regression evidence and an audit decision.

Recorded regressions:
- REG-001: MCB duplicated the canonical AmarCapabilityCandidate contract. Corrected by using the explicitly named AmarMCBCapabilityCandidate.
- REG-002: MCB admission used an incomplete gate set. Corrected to require the full trust/admission gate set.
