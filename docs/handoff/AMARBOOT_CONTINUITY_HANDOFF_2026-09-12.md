# AMARBOOT — Continuity Handoff

Date: 2026-09-12
Repository: `amarazeez1992-eng/AmarBoot`
Current main after latest correction commits: `ac4fb24749d2063fbe7257612623a2f92bfc8c45`

## 1. Non-negotiable project rules
- Continue from the existing repository state; do not restart the project.
- Add functionality as independent modules/files where practical; do not rewrite existing architecture without real necessity.
- AI is advisory and governed. Never claim broker execution without real MT5 ACK + verification.
- Security is fail-closed.
- Emergency stop disables AI authority; it does not close broker positions.
- `AMAR_LIVE_EXECUTION=0` remains disabled until the final laptop/CMG/Bridge/MT5 phase.
- The sacred MT5 EA `mt5/Experts/Grid_Martingale_Basket_v2.mq5` must not be modified.
- Final phase only: Android app -> laptop -> CMG command channel -> secure bridge -> MT5 -> ACK -> fresh-state verification -> reconciliation.

## 2. Architecture already implemented
### AI
- `AmarAiAgentEngine`
- `AmarAiControlCenter`
- `AmarAiApprovalLedger`
- `AmarAiExternalResearch`
- `AmarAiExperienceScreen`
- `AmarAiLiveConversationEngine`
- `AmarAiVoiceInput`
- `AmarAiImageInput`
- `AmarGeminiClient`
- `AmarGeminiVisionClient`

### Direct engine boundary
- `AmarAiEngineBinding`
- Market -> `AmarMarketStateStore`
- Grid -> `AmarGridPlanningEngine`
- Tracking -> `AmarMt5RuntimeRegistry` (read-only)
- Risk -> `AmarTradingPrecisionEngine`
- Strategy validation -> `AmarStrategyValidationEngine`
- Candle analysis -> MT5 runtime candle refresh, fail-closed if runtime is absent

### Execution governance
- `AmarAiExecutionOrchestrator`
- `AmarAiAppCommandBus`
- Canonical schema: `AMAR_EXECUTION_INTENT_V1`
- Pending state: `PENDING_MT5`
- Explicit intent types: OPEN_MARKET, CLOSE_ALL, SET_STOP_LOSS, SET_PROFIT_TRIGGER, START_GRID, STOP_GRID, START_TRACKING, STOP_TRACKING.

### Existing bridge/security
- Python bridge under `bridge/`
- 39 bridge tests passed in the last verified bridge stage.
- HMAC/authentication, nonce/TTL/replay/idempotency, allow-lists and fail-closed validation exist.
- MT5 execution remains disabled until final phase.

## 3. Recent CI errors and corrections
### Run 34685215967
- Python bridge: 39/39 passed.
- Android unit tests: 111 total, 3 failed.
- Failures:
  1. `AmarAiActionEngineTest.botLotCommandIsGoverned`
  2. `AmarAiExecutionOrchestratorTest.canonicalCommandIsBridgeReadyAndNeverClaimsExecution`
  3. `AmarAiExecutionOrchestratorTest.naturalLanguageTradingCommandsBecomeGovernedIntents`
- Root issue for canonical command was Android `org.json` use in JVM unit tests. The current `AmarAiExecutionOrchestrator` on main was already converted to JVM-safe deterministic JSON string construction.
- Bot-lot parsing was hardened with an explicit deterministic regex for commands like `ارفع لوت البوت 2 إلى 0.03`.

### Run 34685779381
- Build reached Kotlin compilation.
- Failure was exactly:
  `AmarAiEngineBinding.kt:8:29 Unresolved reference: runtime`
- Root cause: wrong package import for `AmarStrategyValidationEngine`.
- Correct package is `com.personal.gridbot.amaros.ai.AmarStrategyValidationEngine`.
- Corrected in commit `09f67ccf6412ec7c8a9c9daa7ba37a74c97ec330`.

## 4. Latest commits
- `09f67ccf6412ec7c8a9c9daa7ba37a74c97ec330` — correct direct engine validation import.
- `ac4fb24749d2063fbe7257612623a2f92bfc8c45` — deterministic bot-lot command parsing.
- Previous implementation baseline included `b2e0cae917e13ebed4cf6d6227b013926207db61` and earlier AI/UI/direct-engine commits.

## 5. Important CI status rule
The latest correction commits do NOT yet have a visible completed workflow run in `fetch_commit_workflow_runs` at handoff time. Therefore this handoff must NOT claim CI is green. The next conversation must verify the new `Build APK` / Android workflow result before calling the build fixed.

## 6. Direct AI-to-real-engines target
The approved next implementation target is:
`AI -> real market engine + analysis engine + Grid engine + Tracking runtime + Risk engine + Strategy/Validation engines -> governed intent -> durable app command -> final laptop/CMG/Bridge/MT5 phase.`

Do not replace this with registry-only descriptions or prompt-only claims.

Required direct capabilities:
- Market analysis must read measured market state.
- Grid analysis must call the deterministic grid planner.
- Tracking must read real runtime positions when runtime exists and fail closed otherwise.
- Risk must call the real precision/risk gate.
- Strategy validation must evaluate supplied measured R-results and never invent data.
- Candle analysis must use actual runtime candles and must never invent bullish/bearish percentages.
- Execution commands remain governed and PENDING_MT5 until the final bridge stage.

## 7. Final laptop stage — intentionally last
Only after Android application readiness and verification:
1. Laptop service.
2. CMG command panel/channel.
3. Secure bridge.
4. MT5 runtime.
5. Authentication/signature validation.
6. Nonce/TTL/replay/idempotency.
7. Command allow-list and broker-volume validation.
8. MT5 execution.
9. Terminal ACK.
10. Fresh-state verification.
11. Reconciliation.
12. Final status: PENDING_MT5 / EXECUTED / REJECTED / VERIFIED / FAILED / STALE.

Never report `EXECUTED` merely because a command was queued or accepted.

## 8. New-chat continuation instruction
Start from the current repository state and this handoff. Do not recreate the architecture, repeat old discussions, or start from zero. First action in the new conversation should be:
`تحقق من آخر commit على main ونتيجة CI بعد 09f67cc و ac4fb24، ثم أكمل ربط AmarAiAgentEngine مباشرة بالمحركات الفعلية.`

The user wants implementation, testing and verification, not a theoretical plan.
