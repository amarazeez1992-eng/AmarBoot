# Quantitative Hardening Verification

This verification note records the quantitative hardening boundary currently enforced by `AmarQuantTradingMath`.

- ATR uses Wilder RMA smoothing.
- Historical VaR uses non-negative loss magnitudes and interpolated empirical quantiles.
- Risk-of-ruin is explicitly a first-passage diffusion approximation with documented assumptions.
- Quantitative unit tests use the project's JUnit runner so KAPT can resolve test annotations.
- This module is deterministic and has no broker execution authority.
