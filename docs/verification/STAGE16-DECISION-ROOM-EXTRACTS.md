# STAGE 16 — DECISION ROOM EXTRACTS

> Note: Extracted ideas from Decision Room v1.
> Status: Parking Lot — for later discussion.
> Not part of Stage 16 implementation yet.

## Purpose

This file records selected useful ideas extracted from Decision Room v1 for later architectural discussion. These are not approved Stage 16 requirements, are not implementation instructions, and do not override the Master Constitution or current Stage boundaries.

## Extracted Ideas

1. **Decision Snapshot Input/Output Split** — Keep the reproducible input state separate from the original decision output so a historical decision can be replayed without contaminating its inputs with its result.

2. **Deterministic Decision Replay Contract** — Reload Snapshot Input, rerun the same Decision Engine version/configuration, and compare the reproduced output with the recorded original output.

3. **Point-in-Time Evidence Guard** — Bind every decision input to the information that was actually available at the decision timestamp.

4. **Look-Ahead Contamination Detection** — Explicitly detect evidence, features, outcomes, or metadata that became available only after the decision timestamp.

5. **Canonical Ledger Serialization** — Define deterministic serialization rules before hashing ledger entries: sorted keys, normalized text, deterministic numeric representation, and UTF-8.

6. **Hash-Chain Integrity Verification** — Compute each ledger hash from canonical payload plus the previous hash to support tamper detection and chain verification.

7. **Ledger Integrity Is Not a Digital Signature** — Keep hash-chain integrity terminology distinct from cryptographic signing/non-repudiation claims.

8. **Separate Source Health from Source Lifecycle Status** — Operational health and governance status are different dimensions and should not be collapsed into one state.

9. **Source Failure Counter Policy** — Define explicit consecutive-failure thresholds, reset behavior, recovery criteria, and manual review for suspended sources.

10. **Evidence Lifecycle State Machine** — Track evidence from observation through normalization, sanitization, validation, uniqueness, classification, scoring, decision use, and archival.

11. **Explicit Evidence Rejection Reasons** — Preserve machine-readable rejection reasons such as duplicate, stale, low quality, non-independent, invalid, out-of-time, untrusted, conflicted, or insufficient sample.

12. **Independence Before Aggregation** — Check source/content/dataset dependency before allowing multiple observations to increase apparent evidence strength.

13. **Source-Family Detection** — Identify sources that reproduce the same upstream information so copied reports are not counted as independent evidence.

14. **Claim-Level Deduplication** — Deduplicate claims rather than relying only on URL or document-level deduplication.

15. **Dataset Dependency Modeling** — Represent shared datasets and upstream dependencies explicitly when calculating evidence independence.

16. **Evidence Graph as a First-Class Audit Object** — Preserve relationships among sources, claims, datasets, transformations, and decisions for later reconstruction.

17. **Separate ManagerOutput from DecisionOutput** — Make aggregation output structurally distinct from the final decision so the coordinator cannot silently become the decision authority.

18. **Manager as Coordinator/Aggregator Only** — Restrict Manager to aggregation, policy coordination, and production of ManagerOutput; prohibit direct BUY/SELL/WAIT/NO_TRADE authority.

19. **Explicit Decision Authority Boundary** — Keep the final decision authority singular and mapped to Stage 13 rather than introducing a second Decision Engine.

20. **Verification as a Boundary Guard** — Use verification to block invalid, unsafe, contaminated, or unauthorized outputs without creating a second Evidence/Confidence authority.

21. **AI Evidence-Only Path** — Route optional AI output through schema/policy/evidence/verification before it can influence the analytical aggregation path.

22. **AI Failure Isolation** — Ensure core analytical operation remains valid when optional AI services fail, time out, or become unavailable.

23. **Decision Lifecycle State Machine** — Track decision states from draft through composition, evaluation, freeze, persistence, publication, expiry, and outcome recording.

24. **Short Freeze Window** — Freeze a decision snapshot for a defined period so its published state cannot silently mutate.

25. **Outcome Recording Separation** — Keep later outcome/learning records separate from the original decision-generation event.

26. **Explicit NOT_APPLICABLE Test State** — Allow a test to be marked not applicable only with an explicit reason and approval metadata.

27. **Test-State Evidence Discipline** — Do not treat an untriggered or not-applicable test as successful; preserve the actual state and supporting evidence.

28. **Determinism Test Type** — Test repeated execution with identical inputs/configuration for equivalent outputs.

29. **Failure-Severity Protocol** — Separate minor, moderate, and critical failures and define escalation for integrity, authority, determinism, look-ahead, or execution-boundary violations.

30. **Critical Stop-and-Protect Sequence** — For critical violations, stop, log, protect affected artifacts, identify root cause, fix, test, audit, obtain owner approval, then resume.

31. **Contract-First Interfaces** — Define data, module, engine, manager, division, discovery, and library contracts before implementation of dependent components.

32. **Schema Test Gate** — Test JSON/data contracts independently before relying on them in higher-level integrations.

33. **Versioned Decision Context** — Bind decisions to engine, manager, policy, weight, and other relevant version identifiers.

34. **Weight-Version Provenance** — Record which weight/policy version produced a decision so historical replay does not silently use current configuration.

35. **Conflict-Visible Aggregation** — Preserve conflicting evidence/division outputs rather than silently resolving disagreement.

36. **WAIT/NO-TRADE Reachability** — Ensure insufficient agreement/confidence/conflict can produce a non-trading outcome rather than forcing a directional decision.

37. **Scenario Set A/B/C** — Preserve alternative scenarios with their assumptions instead of storing only one directional conclusion.

38. **Reconstructable Decision Record** — A historical decision should contain enough provenance to explain what inputs, versions, policies, and scores produced it.

39. **No-Execution Analytical Boundary** — Keep Decision Room outputs advisory and prohibit direct broker/order/position control.

40. **Read-Only External Integration** — Where future broker integration is useful, prefer read-only data access or explicit export rather than embedding execution authority.

41. **Protected Artifact Guard** — Maintain explicit protection rules for critical external artifacts and verify they remain unchanged.

42. **Namespace-First Creation Rule** — Inspect repository structure and existing ownership before creating new files, packages, services, or authorities.

43. **Single-Authority Architecture Check** — Before accepting a component, verify that it does not create a parallel authority already owned by another Stage.

44. **Current-Evidence Closure Gate** — Closure requires current evidence; historical CI or prior success cannot substitute for current verification.

45. **Design-vs-Implementation Separation** — A design document, contract, or approved architecture must never be represented as implemented merely because it has been reviewed or marked final.

## Status

All 45 entries are a parking lot for later discussion. No entry is approved for Stage 16 implementation by this file alone.
