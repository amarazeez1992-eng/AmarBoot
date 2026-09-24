# Storage Migration Strategy

## Current Schema

- Schema v1 is the current schema for the Room databases.
- Current schemas are exported under `app/schemas/`.

## Required Procedure for Any Schema Change

When a schema changes:

1. Increase the Room database version.
2. Write an explicit `Migration(N, N+1)` and register it with `Room.databaseBuilder.addMigrations(...)`.
3. Add the new schema JSON under `app/schemas/`.
4. Add a migration test that opens v(N), applies the migration, and verifies the resulting schema/data.

## Data Preservation

- `fallbackToDestructiveMigration` is prohibited.
- Critical data — Bots, Orders, and Positions — must not be lost during migration.

## Governance

Migration/version changes must follow Stage 59 (Version Control).
