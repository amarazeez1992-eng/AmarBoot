# Accuracy-First Verification Implementation Record

## Constitutional decision
AMAR AI must prioritize verified accuracy over short response latency. Research, evidence validation, historical verification, conflict analysis, calculation, and final review must not be weakened merely to produce a faster answer.

## Implementation
- Agent budget default verification window aligned to 120 seconds.
- Android request boundary aligned to the same 120-second window.
- UI status explicitly tells the user that research/verification may take additional time.
- Timeout failure explicitly refuses to invent or bypass verification.
- Existing Evidence, Freshness, Verification, Confidence, Critic, Audit, and Failure Transparency layers remain intact.

## Semantic rules
Current data requires current verification.
Historical questions require attributable historical evidence/data when available.
Stored evidence must retain source time/status and remain traceable.
Future/forecast information is never presented as established fact.
Unavailable evidence is reported as insufficient; it is never filled by guessing.

## Important limitation
A longer timeout is an execution policy improvement, not proof that every market/historical source is already integrated. Runtime source coverage and historical-data persistence must be verified independently before claiming those capabilities.

## Closure gates
Build → unit tests → regression → APK verification → runtime phone test → latency/late-response test → evidence accuracy test → failure/timeout test → audit.
