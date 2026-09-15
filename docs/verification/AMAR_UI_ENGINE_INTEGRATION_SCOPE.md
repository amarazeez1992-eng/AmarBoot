# AMAR AI UI ↔ Engine Integration

Status: IN PROGRESS

## Purpose
The Analysis room UI must call the provider-neutral AMAR agent boundary. External providers such as Gemini are optional synthesis adapters and must never be required for the core UI path.

## Runtime path
UI → AmarAiAgentEngine → AmarAiEngineMesh → deterministic AMAR engines → evidence/result → UI.

`AmarAiUiEngineBridge` is the stable reusable boundary for future UI surfaces and does not own presentation.

## Safety
- Empty external credentials must still produce a valid local AMAR response when the local engine can answer.
- External provider failure must not disable the AMAR local path.
- No broker execution authority is granted by the UI-to-engine boundary.
- Existing UI presentation is preserved; integration work must not delete, simplify, or redesign UI scope.

## Verification
Focused integration test, full unit suite, debug build, and current CI evidence are required before closure.
