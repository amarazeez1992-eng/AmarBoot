## ADR-0003 — B13 Memory Foundation / B14 Knowledge Foundation

### Status
Accepted — Demo foundation only

### Decision
Introduce storage-agnostic memory and knowledge contracts as independent modules. Runtime and UI must access them through services/repositories rather than direct storage access.

### B13 Memory
- `AmarMemoryRecord` is immutable and namespaced.
- `AmarMemoryRepository` is the persistence boundary.
- `InMemoryAmarMemoryRepository` provides deterministic Demo/test behavior.
- `AmarMemoryService` validates writes and normalizes tags/importance.

### B14 Knowledge
- `AmarKnowledgeItem` is an immutable, source-attributed knowledge record.
- `AmarKnowledgeRepository` is the persistence/search boundary.
- `InMemoryAmarKnowledgeRepository` provides deterministic Demo/test search.
- `AmarKnowledgeService` validates publication and exposes topic/search access.

### Safety
These modules do not execute trades, alter decisions, bypass risk controls, or enable LIVE mode. They are foundation contracts for later persistent storage, indexing, retrieval, explainability, and Supervisor capabilities.

### Rationale
The repository boundaries allow Room, encrypted storage, remote knowledge, or indexed retrieval to be added later without changing consumers. This preserves Add-Don't-Destroy and keeps UI independent from storage and trading execution.
