---
title: 'Story 1.2 Code Review Fixes'
type: 'bugfix'
created: '2026-10-16'
status: 'done'
route: 'oneshot'
review_loop_iteration: 0
context:
  - '{project-root}/_bmad-output/implementation-artifacts/spec-1-2-edit-task-details-and-change-progress.md'
  - '{project-root}/_bmad-output/implementation-artifacts/epic-1-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Review of Story 1.2 identified concurrency and error-mapping gaps, plus missing regression coverage around History protection and dashboard-order integration fixtures.

**Approach:** Serialize dashboard membership mutations with database row locks, preserve 404 Problem Details for unmatched routes, and strengthen service and PostgreSQL integration tests for the reviewed behaviors.

</frozen-after-approval>

## Implementation Notes

- Reuse a pessimistic write lock on the current dashboard rows for every task create/update/status mutation so concurrent operations cannot change dashboard membership between the membership check and save. Do not add entity versions or ETags.
- Add regression tests for unknown routes returning 404 `NOT_FOUND`, full replacement of a History task being rejected without saving, and dashboard-order integration tests running against isolated fixture data.
- The dashboard lock query uses the shared recency sort and is repeated after locking to re-read membership with a fresh statement snapshot. The persistence readback test flushes and clears the EntityManager before loading the row.
- Verification: `.\gradlew.bat test` passed the full standard backend suite. PostgreSQL integration tests were not run because `TASKS_TEST_DATABASE_URL` is not configured.

## Review Triage Log

- `low` — fixed: persistence readback now flushes and clears the EntityManager before reloading, so the assertion verifies a database read rather than a managed entity.
- `maybe-false` — deferred: no separate-transaction concurrency integration test was added; a PostgreSQL test with concurrent create/update operations would confirm row-lock behavior under contention.
- `low` — fixed: dashboard lock selection uses the same `RECENCY_ORDER` as dashboard reads, avoiding duplicated ordering rules.
- `low` — fixed: unknown-route response coverage includes both GET and POST requests.
