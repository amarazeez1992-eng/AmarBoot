# Contributing to AmarBoot

Thank you for contributing to AmarBoot.

## Engineering rules

1. Add rather than destroy existing architecture.
2. Keep new capabilities isolated in focused modules/files.
3. Do not silently change approved trading semantics.
4. Do not modify `mt5/Experts/Grid_Martingale_Basket_v2.mq5` without explicit architectural approval.
5. Separate UI, transport, command, execution, risk, AI, and persistence boundaries.
6. Never claim broker execution from queue acceptance alone.
7. Every material trading change needs tests and a reproducible verification path.
8. Third-party code requires provenance and license review before reuse.
9. Never commit API keys, broker credentials, tokens, private certificates, or local secrets.

## Trading changes

Trading logic must document:

- inputs and defaults
- state transitions
- risk limits
- failure behavior
- idempotency/replay behavior where commands are involved
- validation and reconciliation requirements
- paper/demo/live scope

Numerical performance claims must come from reproducible data. Source popularity,
number of search results, or AI confidence is not a profitability probability.

## Pull requests

A PR should explain what changed, why it changed, affected modules, tests executed,
and any remaining limitations. CI must pass before a change is considered verified.
