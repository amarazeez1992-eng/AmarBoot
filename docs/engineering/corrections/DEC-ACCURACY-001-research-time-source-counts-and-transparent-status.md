# AMAR AI — Accuracy-First Research UX Decision

## Status
CONSTITUTIONAL/ENGINEERING DECISION — RECORDED

## Requirements
1. Research time must be measurable and user-visible (for example 10s, 20s, 35s, 40s, 100s), without promising a fixed duration.
2. The Agent may spend additional time when required for verification; accuracy takes priority over short latency.
3. Final answers, especially current financial/market answers, must be grounded in known, attributable, reputable sources.
4. The UI should report the number of sources searched/reviewed when that telemetry is actually available. It must never invent a source count.
5. The UI should distinguish sources searched/reviewed from sources accepted/used in the final evidence chain.
6. During processing, show concise transparent work states (researching, comparing sources, verifying, calculating, etc.), not private chain-of-thought.
7. Before the final answer, processing/status information may be visible; once the final result is rendered, the transient processing panel can be hidden.
8. Final response structure:
   - Analysis: concise evidence-based explanation of what was found and why.
   - Official Result: concise final answer.
   - Source/verification metadata where available.
9. If reputable evidence is insufficient, the Agent must say so rather than guess.

## Example
For a request such as historical gold price:
- show elapsed research time;
- show actual source count when known;
- identify reputable sources used;
- separate historical evidence from current data and forecasts;
- present Analysis then Official Result.

## Closure
This decision is recorded now. Implementation remains subject to the active Stage/Item/Point governance and must be tested before being marked complete.
