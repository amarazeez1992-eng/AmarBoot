# Stage 11 — Item 3 — Point 7: Fingerprint Integrity

## Constitutional audit record

Status: VERIFICATION PENDING — NOT CLOSED

### Scope
Verify that each supplied evidence fingerprint matches the exact source URI + evidence payload that produced it.

### Boundary
- A fingerprint is valid only when present and exactly matches the canonical SHA-256 of `sourceUri|evidence`.
- Missing fingerprints are invalid.
- Modified fingerprints are invalid.
- Changing the source URI or evidence invalidates the prior fingerprint.
- Duplicate detection remains a separate boundary.
- Source independence remains a separate boundary.
- No evidence collection, ranking, routing, claim verification, decision logic, or trading/execution authority is added.

### Regression coverage
- Matching fingerprint is accepted.
- Tampered fingerprint is rejected.
- Missing fingerprint is rejected.
- Source/evidence mutation breaks integrity.
- Repeated verification is deterministic.

### CI gate
The Stage Two workflow contains the dedicated Point 7 boundary test, the existing Point 6 gate, the full unit suite, and a Debug build.

### Constitutional gate
This record remains NOT CLOSED until the exact Point 7 state passes the Stage Two workflow and the successful run is directly verified. No success is inferred from workflow configuration alone.
