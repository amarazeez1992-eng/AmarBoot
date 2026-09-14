# AMAR AI Agent — Automatic Discovery & Update Policy

## Automatic operations
The Agent may continuously:
- search registered public/open-source source domains;
- discover new repositories, releases, papers, datasets and indicator/strategy engines;
- compare versions and fingerprints;
- refresh provenance/license metadata;
- record new findings, conflicts, errors and opinions;
- run regression/benchmark suites against candidate engines;
- mark stale or deprecated capabilities;
- generate update reports.

## Promotion is gated
Discovery is automatic. Trust is not.

A candidate can move to `ADMITTED` only after applicable evidence supports:
1. source provenance;
2. explicit license/reuse basis;
3. dependency/security review;
4. deterministic tests;
5. domain correctness tests;
6. performance benchmark;
7. regression compatibility;
8. adversarial review.

If any mandatory gate fails, the candidate remains indexed but untrusted or is rejected.

## No silent destructive updates
A newer version never overwrites a trusted version immediately. AMAR keeps versioned records and supports rollback/deprecation. The old trusted capability remains available until the new candidate passes promotion gates.

## Research reports
Each refresh produces a machine-readable and human-readable record of discoveries, accepted capabilities, rejected candidates, conflicts, errors, benchmarks and Agent opinions.

## Network failure
If external research is unavailable, AMAR continues using its last verified knowledge snapshot and clearly reports that freshness could not be confirmed. It does not fabricate new evidence.
