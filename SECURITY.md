# Security Policy

## Scope

Security issues in AmarBoot, the Android application, AI boundary, bridge, frontend,
and repository automation are in scope.

The MT5 EA files are protected artifacts. Security reports concerning them should
identify the exact file/version without modifying the source as part of the report.

## Report a vulnerability

Please do not publish exploitable details in a public issue.

Report privately through GitHub's private vulnerability reporting when enabled for
this repository, or contact the repository maintainer through the GitHub profile.
Include:

- affected component and version/commit
- reproduction steps
- expected vs actual behavior
- security impact
- logs or minimal proof of concept when safe
- whether credentials, tokens, broker access, or personal data are involved

## Security invariants

- Live trading remains fail-closed unless the documented execution authority is enabled.
- Emergency stop outranks AI authority.
- Queue acceptance is not execution success.
- Broker execution requires ACK, read-back verification, and reconciliation.
- Idempotency, TTL, replay protection, scope validation, and authentication are required
  at the command boundary.
- API keys and credentials must never be committed to source control.
- Third-party code is reusable only after provenance and license review.
- No protected, invite-only, paid proprietary, or credentialed content is copied into AMAR.

## Supported security baseline

Public repository security should use Dependabot, secret scanning/push protection,
and code scanning/CodeQL where available. GitHub recommends these controls for
public repositories. See the repository security documentation for current availability.
