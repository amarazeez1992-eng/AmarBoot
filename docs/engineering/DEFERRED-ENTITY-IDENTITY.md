# AMAR AI — Deferred Entity Identity

## Status
Entity Identity Design is inspected and deferred. No Entity Identity contract is introduced by Point 22 at this stage.

## Problem
The current architecture contains multiple entity/symbol representations but no single constitutional Entity ID contract:

- AmarMarketState contains `symbol`.
- AmarMarketSnapshot contains `symbol`.
- ResearchFinding has no explicit Entity Identity field.
- A QuestionProfile representation contains `entity: String?`, while another QuestionProfile representation does not.
- Evidence identity is represented by `evidenceFingerprint`, which is not an Entity ID.

## Impact
Point 22 (Evidence Change Detection) remains deferred because its generic entity identity prerequisite is not yet established as a canonical contract.

## Decision
Do not invent a new Entity Identity contract in Point 22. Do not designate `symbol` as the Entity ID. Do not modify ResearchFinding or QuestionProfile for this purpose.

## Recommendation
Complete the Migration Design and implementation first. After the actual evidence path is established, reassess whether a canonical Entity Identity is required and, if required, define it from the observed architecture rather than introducing a parallel authority.

## Constraints
- No new Entity ID.
- No use of `symbol` as Entity ID.
- No modification of ResearchFinding.
- No modification of QuestionProfile.
- No Point 22 implementation before Migration and Entity Identity Re-Assessment.

## Scope
Documentation only. No production-code changes are made by this document.
