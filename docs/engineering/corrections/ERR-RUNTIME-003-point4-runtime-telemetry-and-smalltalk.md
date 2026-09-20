# ERR-RUNTIME-003 — Point 4 Runtime Corrections: Small Talk, Telemetry, and Response Formatting

Status: IMPLEMENTED — CI VERIFICATION PENDING — RUNTIME RECHECK REQUIRED

## Scope
Stage 11 → Item 3 → Point 4 runtime evidence follow-up.

## Runtime defects confirmed
1. "كيف حالك" was included in the generic greeting set and therefore received the generic greeting response.
2. Final telemetry was not explicitly emitted from the canonical completed orchestrator run; the UI could therefore retain/default to zero metadata.
3. MainActivity injected "النتيجة الرسمية:" at the Android boundary, creating a second presentation layer instead of preserving the canonical Agent response.
4. The final UI status used "زمن البحث" even though the measured value covers the complete Agent processing/verification boundary.

## Corrections
- Added AgentIntent.SMALL_TALK and removed small-talk phrases from the generic greeting set.
- Routed SMALL_TALK through the existing canonical planner/query policy as local conversational processing.
- Added a dedicated local small-talk response.
- Published final elapsed/source telemetry from AmarAiAgentEngine using the completed AmarAgentRunResult.
- Discovered sources are counted from distinct non-blank source URIs returned by the research run.
- Accepted sources remain the canonical source-verifier count; no fabricated counts are introduced.
- MainActivity now displays final discovered/accepted counts directly and no longer injects "النتيجة الرسمية:".
- Final time label is now "زمن المعالجة والتحقق" to match the measured boundary.
- Local UI actions also publish truthful final elapsed metadata with zero research-source counts because no research run occurred.

## Guardrails
- No evidence thresholds were weakened.
- No financial/trading verification gate was bypassed.
- No source count is invented.
- No second Agent or alternate orchestration path was introduced.
- Point 5 remains blocked.
- Point 4 remains open until CI and a second phone runtime test confirm the fixes.