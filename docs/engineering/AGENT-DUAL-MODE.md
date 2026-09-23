# AMAR AI — Dual Mode Architectural Reference

## Purpose
This document records the Point 22 implementation boundary after Entity Identity Re-Assessment.

## Locked identity boundary
- Point 22 uses Trading Symbol as the stable comparison context.
- No new Entity ID is introduced.
- No Entity Identity Contract is introduced.
- No free-text symbol extraction is performed.
- AmarTradingSymbol.id may be consumed where an existing trading-symbol context is already available.
- brokerSymbol is not used as the comparison identity.

## Point 22 boundary
- Input is an explicit current snapshot and optional previous snapshot.
- A missing symbol fails closed with NO_BASELINE_AVAILABLE.
- Symbol mismatch fails closed.
- Change detection is action-flag only.
- Detection is stateless and deterministic.
- State is in-memory only; no persistence is introduced.
- Look-ahead protection rejects future timestamps.
- Point 6 Duplicate Evidence and Point 19 Historical Validation are not reused as change-detection authorities.

## Scope
This document is an architectural reference for Point 22 only. It does not create a new Entity Identity authority.
