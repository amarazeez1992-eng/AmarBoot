# AMAR AI UI ↔ Engine Integration

Status: IN PROGRESS

## Purpose
The Analysis room UI must call the provider-neutral AMAR engine boundary first. External providers such as Gemini are optional synthesis adapters and must never be required for the core UI path.

## Required runtime path
UI → AmarAiUiEngineBridge → AmarAiAgentEngine → AmarAiEngineMesh + deterministic AMAR engines → evidence/result → UI.

## Safety
- Empty external credentials must still produce a valid local AMAR response when the local engine can answer.
- External provider failure must not disable the AMAR local path.
- No broker execution authority is granted by the UI bridge.
- UI changes must preserve the UI developer's presentation scope.

## Verification
Focused integration test, full unit suite, debug build, and current CI evidence are required before closure.
