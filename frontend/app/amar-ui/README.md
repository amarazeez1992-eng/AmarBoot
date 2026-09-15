# Amar AI UI — isolated boundary

This folder is the presentation/interaction layer for Item 10.

## Boundary
- UI code, UI styles, and the UI-only bridge contract live here.
- The protected Amar AI Agent implementation is outside this folder and must not be modified by UI work.
- The page entry imports only this folder entry point.
- Unsupported capabilities remain unavailable; the UI never reimplements Agent logic.
- Future UI changes should normally be confined to this folder.

## Contents
- `Item10Workspace.tsx` — UI workspace
- `amarAgentBridge.ts` — UI-only adapter contract
- `quantum.css` — UI base visuals used by this workspace
- `item10.css` — Item 10-specific styles
- `index.tsx` — isolated public entry point
