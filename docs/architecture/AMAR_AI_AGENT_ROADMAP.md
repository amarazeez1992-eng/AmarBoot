# AMAR AI Agent — Build Roadmap

## Phase 1 — Core (implemented foundation)
- Vendor-neutral Agent Core
- Replaceable reasoning interface
- Local deterministic fallback
- Tool registry boundary
- Knowledge registry with provenance
- Research contract
- Structured memory
- Deterministic risk gate
- Multi-gate strategy evaluation

## Phase 2 — Intelligence
- Local LLM runtime adapter
- GGUF/model manager
- Device benchmark and model recommendation
- Streaming generation
- Structured tool-call grammar
- RAG chunking + retrieval + reranking
- Trading ontology and terminology graph
- Citation/evidence ledger

## Phase 3 — Trading Research
- OHLCV normalization
- Technical-indicator library abstraction
- Strategy DSL
- Backtest engine
- Walk-forward analysis
- Monte Carlo / bootstrap robustness
- Slippage, spread, commission and latency stress
- Regime detection and drift monitoring
- Champion/challenger strategy evaluation

## Phase 4 — Agent Intelligence
- Planner
- Researcher
- Quant analyst
- Risk analyst
- Adversarial critic
- Verifier
- Synthesizer
- Consensus and disagreement scoring

## Phase 5 — Production Hardening
- Golden test suite
- Hallucination/evidence tests
- Prompt-injection defenses
- Tool sandbox
- Resource/time/token budgets
- Crash recovery
- Encrypted local storage where appropriate
- Full audit trail

## Phase 6 — Future MT5 integration
- Read-only MT5 bridge first
- Account/symbol/position reconciliation
- Paper trading
- Command approval gateway
- Kill switch
- Idempotency and replay protection
- Broker acknowledgement/read-back
- Only then optional live authorization

## Efficiency upgrades
1. Cache normalized market data and retrieval results.
2. Route simple calculations to deterministic tools instead of the LLM.
3. Use small local models for classification and routing; reserve larger models for synthesis.
4. Keep model context compact through retrieval and summarization.
5. Benchmark every model on the target phone before selecting it.
6. Maintain source fingerprints so stale evidence is detectable.
7. Never score AI confidence as a probability of trading profit.
