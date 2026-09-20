# DEFERRED EVIDENCE RANKING ISSUE

## Status
DEFERRED — PENDING POINT 12

## Problem
Runtime testing showed that the question "عدد دول العالم" could receive retrieved evidence about "الرياض". This indicates a retrieval/relevance/ranking failure: an unrelated finding reached downstream synthesis.

## Classification
Stage 11 → Item 3 → Point 12 — Evidence Ranking.

The issue is not classified as a Point 4 Freshness defect. Freshness can determine whether an evidence item is temporally acceptable; it does not by itself establish that the evidence is semantically relevant to the user's question.

## Impact
- It affects factual answer quality and evidence selection.
- It does not block constitutional closure of Point 4 after Point 4-specific runtime gates pass.
- The issue must remain visible as an explicit deferred engineering item.

## Future correction scope
When Point 12 is opened:
- strengthen Query → Retrieval Relevance;
- improve Evidence Ranking using question/evidence semantic alignment;
- strengthen Cross-Source Correlation;
- prevent unrelated evidence from being promoted into the final synthesis;
- add regression cases such as "عدد دول العالم" versus "الرياض";
- verify that rejection occurs when retrieved evidence is materially unrelated rather than allowing unrelated evidence to drive an answer.

## Guardrails
- Do not add hardcoded question-to-answer mappings.
- Do not weaken source verification to make an answer pass.
- Do not move Point 12 work into Point 4.
- Do not treat this deferred record as evidence that Point 4 is closed.
