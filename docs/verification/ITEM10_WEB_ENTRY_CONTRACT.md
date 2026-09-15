# Item 10 Web Entry Contract

The Android WebView entry is intentionally stable at `amar_reference.html`. That entry must remain compatible with existing startup/recovery code while routing to the packaged current Quantum workspace.

Required properties:
- current Quantum UI is the target, not the legacy UI;
- legacy asset remains present as compatibility fallback;
- AMAR engine bridge remains exposed as `AmarEngine`;
- missing/invalid Quantum package must fail safely;
- no UI redesign is performed by the engine integration layer.

This is an integration contract, not a closure claim.
