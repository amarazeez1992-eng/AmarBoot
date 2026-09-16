# AMAR AI — Internal Experience Specification

Status: APPROVED BY OWNER

## Objective

AMAR AI is an internal assistant room, not a decorative chatbot. The interface and engines must provide a real conversational path into the application while preserving the security boundary around broker execution.

## UI

- Provider-neutral mobile conversation workspace.
- Rounded composer at the bottom.
- Animated workspace perimeter and dynamic status treatment.
- Text input with normal Android copy/paste/delete behavior.
- Image attachment and multimodal input boundary; actual analysis is performed only by a verified Agent capability.
- Voice dictation and native speech-to-text.
- Voice-first live conversation mode: speech recognition -> Agent -> speech synthesis, with automatic turn continuation.
- Animated AMAR AI workspace identity with listening/thinking/speaking states when the verified voice capability is active.
- AI status and emergency-stop state remain visible.

## Application control

AI may issue governed local commands through `AmarAiAppCommandBus`:

- open application rooms such as Settings, Market, Chart and Bot Lab;
- enable/disable visual effects;
- queue bot configuration commands through the durable Bot Command Engine.

Commands do not bypass the application command boundary.

## Intelligence

Natural-language requests are routed first through deterministic local actions. Requests that are not local actions are sent to the real `AmarAiAgentEngine` and its research/analysis/testing tools.

Image input is retained as a provider-neutral evidence payload. No external AI provider is embedded in the UI layer.

Live voice uses `AmarAiLiveConversationEngine` and the provider-neutral Agent boundary.

## Trading safety

- AI authority remains governed by `AmarAiControlCenter`.
- Emergency stop disables AI authority.
- Broker execution is not claimed merely because an AI command was produced.
- Bot commands remain durable and explicitly marked for the MT5 lifecycle until a real bridge ACK/verify/reconcile path is available.
- No automatic strategy approval or adoption.

## Future direct-control extensions

The same command bus is the integration point for additional approved application actions: settings changes, chart navigation, market inspection, strategy operations and other UI-level actions. Each action must have a concrete handler and test; registry-only or prompt-only features are not considered implemented.
