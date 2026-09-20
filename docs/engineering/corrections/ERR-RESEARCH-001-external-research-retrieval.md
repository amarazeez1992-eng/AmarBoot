# ERR-RESEARCH-001 — External research retrieval ceiling and provider fragility

**Status:** Corrected / CI verification required  
**Detected:** 2026-09-20  
**Area:** External Research  
**Stage alignment:** Research foundation following Stage 0 → 10 closure

## Symptom

The Agent requested 40 sources while the constitutional research contract and budget already support a target of 80 independent sources. The external retrieval layer also queried providers sequentially with long default network timeouts.

## Root cause

The research adapter was not using the available 80-source retrieval target, and provider calls were serialized. A slow or unavailable provider could consume most of the Agent request timeout.

## Correction

1. Agent retrieval target raised from 40 to 80.
2. External research result limit is bounded at 80.
3. DuckDuckGo, Wikipedia and GitHub retrieval are executed concurrently.
4. Network calls use bounded connect/read/call timeouts.
5. Results are normalized and deduplicated by canonical URL.
6. Provider failure remains fail-soft; no provider is treated as truth by itself.
7. Existing Evidence Engine, verification and strict financial gate remain authoritative.

## Important limitation

This correction establishes the 80-result retrieval budget and a stronger multi-provider collection layer. It does **not** claim that 80 independent high-authority financial sources are guaranteed in every query. Independence and authority remain evidence/verification responsibilities.

## Non-negotiables

- No hardcoded question → answer mapping.
- No hosted LLM as primary brain.
- No weakening of financial evidence gates to compensate for missing sources.
- No fabricated source, evidence, confidence or result.
