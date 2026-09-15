# AMAR Final Quantum + Agent Baseline

This marker records the engineering baseline after the explicit provider-isolation and new-UI integration directive.

- Primary UI: `frontend/app/page.tsx` -> `HomeWorkspace`
- Internal agent UI: `HomeWorkspace` -> `Item10Workspace`
- Runtime bridge: `AmarEngine` -> `AmarAiWebEngineBridge` -> `AmarAiAgentEngine`
- Gemini: disconnected from the runtime authority path; Gemini client/key-store source removed.
- Legacy agent UI sources removed.
- Quantum export is packaged into Android assets by the build path.
- Final gate: `.github/workflows/amar-final-quantum-agent-gate.yml`

This document is evidence metadata only; final closure requires fresh CI evidence on this exact main commit.
