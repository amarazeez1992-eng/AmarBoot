# AMARBOOT — COMPLETE PROJECT HANDOFF

**Date:** 2026-09-12
**Repository:** `amarazeez1992-eng/AmarBoot`
**Default branch:** `main`
**Project:** AMAR BOT / Grid Trade Bot + AMAR AI Supervisor
**Purpose of this document:** carry the complete project context into a new ChatGPT conversation without restarting the project.

---

## 1. THE CORE IDEA

AmarBoot is not intended to remain a simple grid-bot UI. It is being prepared as a modular personal trading platform in which:

`AMAR AI → real application engines → governed execution intent → Android command record → final laptop/CMG/Bridge/MT5 stage`

The AI must reason from actual application/runtime evidence. It must not claim that a strategy, engine, market state, trade, or execution exists merely because a Registry, catalog, prompt, or text description says it exists.

The final live-execution stage is deliberately postponed until the Android application and its internal engines are complete.

---

## 2. NON-NEGOTIABLE ARCHITECTURE RULES

1. Add modules/files instead of destroying or rewriting existing architecture unless genuinely necessary.
2. Do not invent market values, candle percentages, lot values, positions, broker ACKs, or execution results.
3. Registry/catalog = discovery/context only; it is never proof of execution.
4. AI authority is separate from broker execution authority.
5. Emergency stop blocks AI authority; it does not close broker positions.
6. Security is fail-closed.
7. Demo/live broker execution remains disabled until the final laptop/CMG/Bridge/MT5 phase.
8. Queue acceptance is never treated as broker execution success.
9. Real execution must eventually follow:
   `UI → Command → Validate → Accept → Execute → ACK → Verify → Reconcile → Audit`.
10. The protected MT5 EA must not be modified by Android/AI hardening work.

---

## 3. PROTECTED MT5 BASELINE

Protected EA:
`mt5/Experts/Grid_Martingale_Basket_v2.mq5`

Version: `2.01`
Protected SHA-1:
`1125cabef6b4a4b1d5eb2c5deaa4b5e7458479ad25`

It is not to be rewritten as part of the Android application work.

Final future stage only:
`Android → Laptop → CMG Command Panel → Secure Bridge → MT5 → ACK → Read-back → Verification → Reconciliation → Audit`.

---

## 4. CURRENT APPLICATION STATE

The repository README identifies the current platform as Kotlin/Jetpack Compose with BOT 1 runtime, MT5 bridge architecture, security boundaries and Bot Vault.

### Bot state
- BOT 1 is the real operational bot/runtime baseline.
- BOT 2–4 are currently configuration vaults; they do not yet have independent trading engines.
- Bot Vault provides persistent bot/strategy storage and editing/deletion/clear operations.
- Bot Lab keeps BOT 1 as the active operational interface.

### Security
- Fail-closed execution model.
- TTL.
- Replay/idempotency controls.
- Scope validation.
- Receiver-side HMAC verification.
- Live trading remains gated.

### MT5
- Live execution is not yet the current Android-phase target.
- Current Android AI engine bindings are read-only when MT5 runtime data is available.

---

## 5. AI EXPERIENCE / UI

Implemented direction:
- ChatGPT/Gemini-style AI conversation experience.
- White bottom composer.
- Animated yellow/red/orange perimeter.
- Text input.
- Copy / paste / delete.
- Image attachment and Gemini Vision analysis.
- Native Android voice input.
- Live voice conversation engine.
- Animated AI avatar states: LISTENING / THINKING / SPEAKING.
- AI status and emergency-stop controls.
- Gemini API key storage through Android Keystore/AES-GCM.
- AI actions are routed deterministically first where possible, then through the real AI agent.

Important UI principle:
The AI UI is not a fake trading screen. Its control commands must eventually connect to real governed application engines and, only in the final stage, the real MT5 bridge.

---

## 6. AI CORE / AGENT

Important existing components include:

- `AmarAiKeyStore.kt`
- `AmarGeminiClient.kt`
- `AmarAiAgentEngine.kt`
- `AmarAiControlCenter.kt`
- `AmarAiWorkspace`
- `AmarAiMt5Office`
- `AmarTradingSourceMesh`
- `AmarTradingIntelligenceRegistry`
- `AmarAiResearchAuthority`
- `AmarStrategyEvolutionEngine`
- `AmarAiApprovalLedger`
- `AmarDecisionMemoryRepository`
- `AmarKnowledgeGraphRepository`
- `AmarTradeLevelStressEngine`
- `AmarDriftAndUncertaintyEngine`
- `AmarMarketAnomalyEngine`
- `AmarStrategyLineageStore`
- `AmarAiExternalResearch`
- `AmarAiVoiceInput`
- `AmarAiImageInput`

The Agent already has a governed tool architecture. The next goal is to ensure important tools call the real application engines instead of returning registry/prompt-only descriptions.

---

## 7. DIRECT AI → REAL ENGINE BINDING — APPROVED

Approved requirement:

> AMAR AI commands must be produced from real application engines and measured runtime state, not from an intelligence Registry, catalog text, or prompt-only claims.

Approved direct bindings:

### Market
`AmarMarketStateStore` and, when installed, read-only MT5 runtime market data.

### Grid
`AmarGridPlanningEngine` calculates deterministic grid levels and lot progression.

### Tracking
`AmarMt5RuntimeRegistry` provides read-only MT5 positions when a runtime is installed. Otherwise the system must fail closed with:
`MT5_RUNTIME_NOT_INSTALLED`.

### Risk
`AmarTradingPrecisionEngine` provides the current research/risk gate.

### Strategy / Validation
`AmarStrategyValidationEngine` evaluates supplied measured R results and does not fabricate data.

### Candle analysis
Candle direction/body percentage must be calculated from actual refreshed runtime candle data. If the runtime cannot supply candle data, report data unavailable; never invent a percentage.

Approved architecture document:
`docs/architecture/AMAR_AI_DIRECT_ENGINE_BINDING.md`

Approved commit:
`6110fc84548ea0e403d55f2a09dce29a025f47db`

---

## 8. IMPORTANT COMPILE ERROR AND CORRECTION

Latest failed workflow inspected:
`34685779381`

Its checked-out commit was:
`deeb3ef9c5b57cc13ce27157f9fcdec8a435b084`

The build failed at:
`AmarAiEngineBinding.kt:8:29`

Error:
`Unresolved reference: runtime`

Root cause:
The import pointed to the wrong package:
`com.personal.gridbot.runtime.AmarStrategyValidationEngine`

Correct package is:
`com.personal.gridbot.amaros.ai.AmarStrategyValidationEngine`

The current `main` version has the corrected import and the validation call uses the real `AmarStrategyValidationEngine` in the AI package.

Do NOT go back to the old `deeb3ef...` version.

Important verification status:
The corrected `main` content is confirmed in the repository, but a new CI run for the corrected commit must still be observed before calling CI green.

---

## 9. EXECUTION ORCHESTRATOR

Created:
`AmarAiExecutionOrchestrator.kt`

Purpose:
Convert explicit natural-language user commands into validated canonical execution intents without calling MT5 directly during the Android phase.

Supported intent types include:
- `OPEN_MARKET`
- `CLOSE_ALL`
- `SET_STOP_LOSS`
- `SET_PROFIT_TRIGGER`
- `START_GRID`
- `STOP_GRID`
- `START_TRACKING`
- `STOP_TRACKING`

Rules:
- BOT number must be valid.
- OPEN_MARKET requires explicit symbol, BUY/SELL and positive volume.
- Dollar stop/profit triggers require explicit positive values.
- Missing financial values are never guessed.
- Canonical command schema:
  `AMAR_EXECUTION_INTENT_V1`
- Android execution status remains:
  `PENDING_MT5`

---

## 10. AI APP COMMAND BUS

`AmarAiAppCommandBus` carries governed application commands such as:
- OpenRoom
- SetVisualEffects
- QueueBotCommand

Execution intents are validated and routed into the existing durable command queue through `QueueBotCommand`.

This keeps MainActivity stable and avoids unnecessary command-bus duplication.

---

## 11. NATURAL-LANGUAGE COMMANDS ALREADY PLANNED / WIRED

Examples:

- `افتح شراء XAUUSD لوت 0.01`
- `افتح بيع XAUUSD لوت 0.01`
- `أغلق الكل`
- `عند الخسارة 30 دولار أغلق`
- `عند الربح 2 دولار نفذ الأمر التالي`
- `شغل الشبكة`
- `أوقف الشبكة`
- `شغل التتبع`
- `أوقف التتبع`

Commands that require missing financial values must ask for the value instead of inventing one.

---

## 12. APP INTROSPECTION

`AmarAiAppInspector.kt` was introduced to give the AI deterministic application introspection.

It is intended to expose:
- available application rooms;
- current market snapshot;
- execution stage;
- application readiness information.

The next step is to ensure the Agent has an explicit `inspect_app` tool and uses its returned evidence.

---

## 13. MARKET / CANDLE ANALYSIS REQUIREMENT

For commands such as:

`اذهب إلى الشمعة الربع ساعة واكتشف هل هي شرائية/بيعية وكم نسبتها`

The AI must:
1. obtain actual candle data;
2. identify the timeframe;
3. calculate direction from open/close;
4. calculate body percentage from real high/low range;
5. report the actual source;
6. refuse to invent data if the runtime is unavailable.

Current direct binding uses MT5 runtime refresh for this path.

---

## 14. GRID ENGINE BINDING

The AI direct engine layer calls `AmarGridPlanningEngine` for deterministic grid calculations.

Inputs include:
- reference price;
- grid step;
- order count;
- base lot;
- lot multiplier;
- buy/sell direction.

This is calculation/validation only during Android phase. It does not submit broker orders.

---

## 15. TRACKING ENGINE BINDING

The AI direct engine layer uses `AmarMt5RuntimeRegistry` when installed.

Current Android-phase behavior is read-only:
- retrieve positions;
- expose ticket/symbol/volume/current price/profit/magic;
- fail closed if runtime is unavailable;
- require successful runtime response before reporting tracking data.

No direct broker write is enabled here.

---

## 16. RISK ENGINE BINDING

The AI direct layer calls `AmarTradingPrecisionEngine` for a research/risk gate.

The output includes score, confidence, uncertainty, gate and reasons.

This is a policy/evidence gate, not a license for autonomous live trading.

---

## 17. STRATEGY / VALIDATION

`AmarStrategyValidationEngine` evaluates measured R-value sequences.

Reported metrics include:
- sample size;
- win rate;
- profit factor;
- expectancy in R;
- maximum drawdown in R;
- SQN;
- verification status.

No fabricated market trades are allowed.

Future work:
Connect walk-forward/stress/evolution tools to actual measured data paths rather than merely aggregating descriptive scalars.

---

## 18. SECURITY / AUTHORITY

The AI control boundary remains governed by `AmarAiControlCenter`.

Emergency stop and authority checks must remain active.

The Registry is not execution authority.

Live execution must remain disabled until the final bridge phase.

---

## 19. FINAL LAPTOP / CMG / BRIDGE / MT5 PHASE

This is intentionally the LAST phase.

Do not start it before the Android application and its internal engines are complete.

Final target:

`AMAR AI
→ Android canonical execution intent
→ durable command record
→ Laptop
→ CMG command panel
→ authenticated secure bridge
→ MT5
→ terminal ACK
→ fresh runtime state
→ verification
→ reconciliation
→ audit`

The bridge must preserve:
- authentication;
- HMAC/signature verification;
- scope/allow-list checks;
- TTL;
- nonce/idempotency;
- replay protection;
- broker symbol/volume/price validation;
- ACK;
- fresh read-back;
- verification;
- reconciliation;
- fail-closed behavior.

Accepted final states:
`PENDING_MT5`, `EXECUTED`, `REJECTED`, `VERIFIED`, `FAILED`, `STALE`.

`QUEUED` or `accepted=true` alone is never execution success.

---

## 20. WHAT IS COMPLETE / WHAT IS NOT

### Implemented / established
- Android Kotlin/Compose platform.
- BOT 1 runtime baseline.
- Bot Vault architecture.
- Security boundaries.
- AI Supervisor foundation.
- AI conversation UI direction.
- Voice/image capabilities.
- AI control center and emergency stop.
- Deterministic AI action routing.
- Canonical execution intent schema.
- Durable pending execution commands.
- Direct AI → market/grid/tracking/risk/validation bindings.
- App introspection foundation.
- MT5 read-only runtime path.
- Architecture documentation for direct engine binding.
- Final laptop/CMG/Bridge/MT5 phase specification.

### Still required before final bridge
- Verify a fresh CI run on the corrected current `main` commit.
- Complete Agent tool integration so direct engine evidence is actually used by the AI decision loop.
- Add/verify `inspect_app` tool in `AmarAiAgentEngine`.
- Complete actual market anomaly / trade stress / decision memory / knowledge graph / lineage integrations where they are currently descriptive rather than executable/evidence-producing.
- Verify walk-forward/stress/strategy evolution against measured data.
- Finish application-wide tests and audit.
- Keep live broker execution OFF.
- Only after all of the above: implement laptop + CMG + Bridge + MT5 write/ACK/verify/reconcile.

---

## 21. KNOWN CI HISTORY

Important workflow references:
- `34683680216` — earlier validation had one drift test failure; the drift normalization issue was subsequently patched.
- `34684295171`
- `34684310409`
- `34684344221`
- `34684372849`
- `34684377728`
- `34684391468`
- `34684391469`
- `34685105677`
- `34685215967`
- `34685779381`

Latest specifically inspected compile failure:
`34685779381` → old commit `deeb3ef...` → wrong `runtime` import.

Current main contains the corrected package import.

Do not claim green CI until a fresh run on current main completes successfully.

---

## 22. HOW TO CONTINUE IN A NEW CHAT

Start the new conversation with:

> هذا ملف تسليم مشروع AmarBoot. لا تبدأ من الصفر. اقرأه كمرجع أساسي واستمر من آخر حالة موثقة. المستودع هو amarazeez1992-eng/AmarBoot. آخر مرحلة صحيحة هي Android + direct engine binding، والمرحلة الأخيرة مستقبلًا فقط هي Laptop + CMG + Bridge + MT5. لا تعدّل EA المحمي. لا تعتبر Registry أو prompt تنفيذًا حقيقيًا. أول مهمة: تحقق من آخر CI على main، ثم أكمل ربط AmarAiAgentEngine بالمحركات الفعلية واختبر كل مسار قبل الانتقال إلى مرحلة اللابتوب.

Then continue from the current repository `main`, not from an old snapshot.

---

## 23. SINGLE SOURCE OF TRUTH FOR THE NEXT CHAT

Repository:
`https://github.com/amarazeez1992-eng/AmarBoot`

Protected EA:
`mt5/Experts/Grid_Martingale_Basket_v2.mq5`

Direct engine architecture:
`docs/architecture/AMAR_AI_DIRECT_ENGINE_BINDING.md`

Execution phases:
`docs/architecture/AMAR_AI_EXECUTION_PHASES.md`

AI experience:
`docs/architecture/AMAR_AI_INTERNAL_EXPERIENCE_SPEC.md`

This handoff:
`docs/HANDOFF/AMARBOOT_PROJECT_HANDOFF_2026-09-12.md`

---

## FINAL RULE

**Do not restart. Do not redesign the project from zero. Continue from the repository state and this handoff.**

The project is in the Android/internal-engine preparation phase. The laptop/CMG/Bridge/MT5 integration is the final stage after the Android environment is fully complete and verified.
