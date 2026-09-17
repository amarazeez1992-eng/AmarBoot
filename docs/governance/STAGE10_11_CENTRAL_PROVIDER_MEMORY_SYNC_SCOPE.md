# AMAR AI — Central Provider, GitHub Memory & Source Sync Scope

Status: APPROVED ROADMAP SCOPE — IMPLEMENTATION PENDING

This document defines approved requirements to be implemented and verified one at a time under the project constitution.

## Stage 10 — Security / Release Boundary

1. **GitHub authorization boundary**
   - Amar AI must support an explicit owner-controlled GitHub connection/authorization path.
   - The owner may grant, revoke, and inspect the GitHub capability used by the Agent.
   - Credentials/tokens/secrets must never be committed into the repository or exposed in logs/UI.
   - Permissions must be least-privilege and auditable.

2. **Central-provider security contract**
   - Define the future Amar AI central-provider API boundary and authentication model.
   - Define an owner-controlled provider credential family named **ABL**.
   - ABL is an architecture/API credential concept, not a secret to be hard-coded now.
   - External applications/agents may connect only through explicitly issued, scoped, revocable credentials.
   - No provider credential may bypass Amar AI governance, privacy, risk, or execution controls.

3. **Release/security audit**
   - Verify secret handling, authorization boundaries, revocation, provenance, logging, and release artifacts before Stage 10 closure.

## Stage 11 — Intelligence / Memory / Provider Implementation

4. **GitHub Workspace & explicit memory**
   - When the user explicitly says to save something, Amar AI stores it in a dedicated, human-readable GitHub-backed memory area.
   - Memory entries must be structured, searchable, timestamped, and source/provenance aware.
   - The user can inspect what has been saved, update it, or request deletion.
   - Saving must be explicit; Amar AI must not silently persist arbitrary content.

5. **Central ABL Provider**
   - Amar AI becomes the central provider layer for approved future applications/agents.
   - ABL credentials are owner-issued, scoped, revocable, auditable, and rotatable.
   - Each connected client receives only the capabilities explicitly granted to it.
   - Central provider remains optional for clients and must fail closed when authorization is absent or invalid.

6. **Source-aware automatic refresh**
   - Amar AI may periodically re-check important saved/source-backed information against its authoritative source.
   - Supported schedules: **every 6 hours** or **every 24 hours**.
   - Refresh must record source, timestamp, detected changes, previous value, and verification status.
   - A changed source must not silently overwrite user memory when the change is ambiguous or materially important; it must be flagged for review.
   - Scheduled research must respect source permissions, rate limits, privacy, and safety policies.

7. **Provider + memory + research integration**
   - The central Agent coordinates GitHub memory, source refresh, research modes, evidence chains, and future external clients.
   - No connected engine or client may bypass the central governance layer.

## Implementation law

Every item is implemented separately using:

**تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**

Approval of this scope does not mean implementation is complete. No item may be closed from documentation alone.
