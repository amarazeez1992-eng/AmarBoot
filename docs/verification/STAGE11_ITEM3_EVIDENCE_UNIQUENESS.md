# Stage 11 — Item 3 — Point 9: Evidence Uniqueness

## Constitutional audit record

Status: VERIFICATION PENDING — NOT CLOSED

### Scope
Verify uniqueness of already-collected evidence records without duplicating Point 6, Point 7, or Point 8 responsibilities.

### Canonical boundary
A record identity is derived deterministically from:
- source URI
- evidence payload
- retrieval timestamp

The supplied fingerprint field is deliberately not used by this point.

### Separation of responsibilities
- Point 6 detects semantic duplicate evidence content.
- Point 7 verifies fingerprint integrity.
- Point 8 detects mutation against a recorded provenance snapshot.
- Point 9 verifies whether already-collected evidence records occupy unique canonical record identities.

### Regression coverage
- Same content from different sources remains unique records.
- Same source + same content + same retrieval timestamp collides.
- A changed retrieval timestamp creates a distinct record identity.
- Missing source/evidence is not accepted as a unique record.
- The result is deterministic and independent of the supplied fingerprint field.

### CI gate
The Stage Two workflow contains the dedicated Point 9 test, existing evidence gates, Stage Two regression, full unit suite, and Debug build.

### Constitutional gate
This record remains NOT CLOSED until the exact Point 9 state passes the Stage Two workflow and the successful run is directly verified.
