# Stage 11 — Error Prevention Rules

These are mandatory regression-prevention rules for every future Stage 11 item.

1. **Never close from implementation alone.** Closure requires focused tests, full unit suite, build verification, CI success, re-inspection, and final audit.
2. **Compile-test scope before CI.** Test helpers used by nested fakes/fixtures must be top-level or explicitly accessible; do not rely on enclosing test-class member scope.
3. **Reuse existing contracts.** Search the repository before creating enums, data classes, source types, evidence types, conflict types, or generic workforce contracts.
4. **Validate exact enum members and constructor signatures.** Never assume names such as source types or model fields; inspect the defining contract first.
5. **No unused imports or dead code in newly changed files.** Re-inspect changed files before CI.
6. **Conflicts must be semantically deduplicated.** Do not duplicate the same support/opposition pair when combining local and global verification results.
7. **Research verification must remain fail-closed.** Invalid/blank sources are filtered before verification; insufficient evidence cannot become VERIFIED.
8. **No execution authority in intelligence layers.** No broker calls, live-trading enablement, or governance mutation may enter Stage 11 intelligence packages.
9. **Every discovered defect becomes a regression guard.** A fixed defect must be represented by a test or architecture check that prevents recurrence.
10. **Do not repeat a previously discovered failure mode.** Before each implementation/repair, review this file and the prior CI evidence for the current item.
