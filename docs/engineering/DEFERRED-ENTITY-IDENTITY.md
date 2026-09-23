# AMAR AI — Deferred Entity Identity

## Status
Entity Identity Re-Assessment is closed for Point 22. Point 22 uses the existing Trading Symbol as its stable comparison context and does not introduce a new Entity Identity contract.

## Observed architecture
The repository contains multiple entity/symbol representations but no single constitutional Entity ID contract:
- AmarMarketState.symbol
- AmarMarketSnapshot.symbol
- ResearchFinding without an explicit Entity Identity field
- two QuestionProfile representations with different entity fields
- AmarIntentUnderstanding.entities
- AmarTradingSymbol.id
- broker-facing brokerSymbol

## Decision
For Point 22 only, do not introduce a new Entity ID or Entity Identity Contract. Use the existing Trading Symbol context as the stable comparison key.
- AmarMarketState.symbol is the accepted market-state source.
- AmarTradingSymbol.id may be consumed when that context is already available.
- brokerSymbol is not the identity key.
- No symbol is extracted from free text to compensate for missing context.

## Point 22 fail-closed rule
If the symbol is missing, Point 22 returns NO_BASELINE_AVAILABLE and does not perform change detection.

## Scope boundary
This closure does not establish that Entity and Trading Symbol are globally identical concepts. It establishes only the Point 22 comparison context.

## Next step
Proceed with Point 22 implementation under AGENT-DUAL-MODE.md. No Entity ID is created by this implementation.
