# ERR-RESEARCH-002 — Wikipedia factual retrieval and direct-answer synthesis

**Status:** Corrected — fresh CI required on current main  
**Detected:** 2026-09-20  
**Area:** External Research / Local Reasoning  
**Purpose:** Close the current runtime story for simple factual questions without introducing a hardcoded question→answer path.

## Problem

The Agent had an external research layer, but a simple factual request such as:

من هيه عاصمة فرنسا؟

could retrieve evidence and still return only a generic evidence summary. Arabic requests were also queried against English Wikipedia.

## Root cause

Two integration gaps existed:

1. Wikipedia retrieval was fixed to the English API.
2. The local reasoning kernel ranked evidence records but did not extract a directly matching factual sentence for the final answer.

## Correction

- Arabic requests now query Arabic Wikipedia; non-Arabic requests use English Wikipedia.
- Wikipedia results use page extracts as evidence when available.
- The local reasoning kernel performs generic sentence-level evidence matching using normalized query/evidence token overlap.
- When a directly matching sentence is available, the Agent returns that sentence and identifies the source title/URI.
- No France-specific mapping, answer dictionary, external LLM, or keyword-only answer table was introduced.
- Existing verification/evidence gates remain unchanged.

## Acceptance test

The regression test supplies the sentence “باريس هي عاصمة فرنسا وأكبر مدنها.” as Wikipedia-style evidence for the Arabic question and verifies that the direct factual sentence and Wikipedia source are returned.

## Required closure evidence

1. Unit tests pass.
2. Build APK passes.
3. Stage 8 / Stage 10 / CodeQL gates pass on the final current commit.
4. The generated APK is installed and the owner verifies:
   - من انت
   - مرحبا
   - من هيه عاصمة فرنسا
5. The factual response must contain the verified answer and identify Wikipedia/source evidence.
6. No Stage 12 transition is performed as part of this correction.

## Non-negotiables

- No hardcoded question→answer mapping.
- No fabricated evidence.
- No claim of runtime closure before the owner verifies the APK behavior.
- No changes to Stage 12 or later work.
