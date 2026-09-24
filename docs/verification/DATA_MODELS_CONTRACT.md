# Data Models Contract

## Official Core Data Models

The Stage 1 core Data Models are:

1. `AmarAgentRequest`
2. `AmarAgentResponse`
3. `AmarAgentContext`
4. `AmarAgentPolicy`
5. `AmarBotRuntimeRecord`
6. `AmarRuntimeOrderRecord`
7. `AmarRuntimePositionRecord`
8. `AmarRuntimeCommandRecord`
9. `Bot`
10. `BotLog`
11. `AmarAgentTool`
12. `AmarQueryPolicyDecision`

## Validation Boundary

Validation occurs at system boundaries (constructors for Core/API/Storage), rather than inside Data Classes.

Critical invariants are checked at the following boundaries:

- `AmarAgentCore`: source-count and policy-related constraints.
- `AmarConfigurationValidator`: configuration/policy constraints.
- `AmarEnvironmentGuard`: environment constraints.

No `require` blocks are to be added inside Data Classes at this time. This is reviewed only when an actual need arises.
