# ERR-RETRIEVAL-003 — Query Relevance Gate

## Status

IMPLEMENTED — CI/RUNTIME VERIFICATION PENDING

## Root Cause

Runtime evidence showed that public retrieval could return documents unrelated to the user's question. The previous verifier validated source usability, authority, and independence, but did not require query relevance before evidence entered those gates.

This allowed a trustworthy publisher to contribute irrelevant evidence.

## Scope

Stage 11 retrieval/evidence boundary.

The defect is retrieval relevance and evidence ranking/selection, not Freshness Engine Point 4.

The deferred P1 is therefore promoted as active remediation work under the existing Point 12 / evidence-ranking scope. No new constitutional Stage 11 item was created.

## Implemented

1. Added AmarRetrievalRelevanceEngine.
2. Scores results against the actual user query using normalized lexical/entity-aware matching, title weighting, body coverage, exact phrase matching, and conservative query expansion.
3. Rejects results below the minimum relevance threshold before evidence verification.
4. Carries relevanceScore through ResearchFinding.
5. AmarSourceVerifier now rejects evidence below the relevance threshold even when the source is otherwise authoritative.
6. GitHub repository discovery is restricted to code/repository-oriented queries instead of being treated as a general factual source.
7. Internal verification failure codes are no longer shown to the user; the UI-facing response is a plain-language insufficient-evidence message.

## Safety Boundary

This is a retrieval gate, not a hardcoded answer system and not a replacement for later semantic ranking.

No answer is generated from a result merely because keywords match. Relevance is now a prerequisite for evidence admission; subsequent authority, freshness, independence, claim verification, and confidence gates remain active.

## Required Verification

- Unit tests for relevance acceptance/rejection.
- CI green on all required workflows.
- Runtime regression:
  - capital of America must not produce unrelated entities;
  - artist age query must not accept unrelated date evidence;
  - Arabic/English letter-count query must not accept unrelated country evidence;
  - financial/trading queries must reject irrelevant market evidence;
  - user-facing insufficient-evidence responses must not expose internal gate codes.
- Point 4 remains open until its own constitutional closure evidence is complete.
