# Stage 11 — Error Prevention Rules

These are mandatory regression-prevention rules for every future Stage 11 item.

**Binding project law:** `docs/governance/AMAR_PROJECT_CONSTITUTION.md` is the project-level authority. Stage 11 may be stricter, never weaker.

1. **Never close from implementation alone.** Closure requires the complete project closure loop: **تحقيق → تنفيذ → فحص → اختبار → تأكيد → إثبات → تدقيق نهائي → جاهز → إغلاق**.
2. **Never reuse stale proof as current closure evidence.** After any substantive correction, the affected item requires fresh verification on the current implementation/commit.
3. **Compile-test scope before CI.** Test helpers used by nested fakes/fixtures must be top-level or explicitly accessible; do not rely on enclosing test-class member scope.
4. **Reuse existing contracts.** Search the repository before creating enums, data classes, source types, evidence types, conflict types, or generic workforce contracts.
5. **Validate exact enum members and constructor signatures.** Never assume names such as source types or model fields; inspect the defining contract first.
6. **No unused imports or dead code in newly changed files.** Re-inspect changed files before CI.
7. **Conflicts must be semantically deduplicated.** Do not duplicate the same support/opposition pair when combining local and global verification results.
8. **Research verification must remain fail-closed.** Invalid/blank sources are filtered before verification; insufficient evidence cannot become VERIFIED.
9. **No execution authority in intelligence layers.** No broker calls, live-trading enablement, or governance mutation may enter Stage 11 intelligence packages.
10. **Every discovered defect becomes a regression guard.** A fixed defect must be represented by a test or architecture check that prevents recurrence.
11. **Do not repeat a previously discovered failure mode.** Before each implementation/repair, review this file, the Constitution, and the prior CI evidence for the current item.
12. **No advancement before proof.** A Stage 11 item remains **قيد التحقق النهائي** whenever any required closure evidence is missing; it cannot advance or be marked CLOSED by assumption.

<!-- Final verification trigger: current Stage 11 implementation must pass fresh CI before closure. -->
<!-- Verification retry: current head must receive fresh CI evidence. -->
