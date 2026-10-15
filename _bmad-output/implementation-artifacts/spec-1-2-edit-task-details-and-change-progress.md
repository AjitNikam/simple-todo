---
title: 'Edit Task Details and Change Progress'
type: 'feature'
created: '2026-10-15'
status: 'done'
route: 'dispatch'
baseline_commit: 'NO_VCS'
review_loop_iteration: 0
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-1-context.md'
  - '{project-root}/_bmad-output/planning-artifacts/architecture/architecture-simple-todo-2026-10-10/api-contract.md'
  - '{project-root}/_bmad-output/planning-artifacts/ux-designs/ux-simple-todo-2026-10-10/DESIGN.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Dashboard tasks cannot be revised as work changes, so their saved details and progress become inaccurate.

**Approach:** Allow editing title and optional notes inline within each dashboard task card, with a status dropdown on the card. Persist mutations through the REST API, refresh server state after success, and keep failures visible and recoverable.

## Boundaries & Constraints

**Always:** Use `PUT /api/tasks/{taskId}` for full title/notes/status replacement and `PATCH /api/tasks/{taskId}/status` for status-only changes. Reject blank titles with “Title is required.”; accept only TODO, IN_PROGRESS, and COMPLETED, displayed as TODO, In progress, and Completed. Mutations refresh `updatedAt` and dashboard membership. The API is authoritative; do not optimistically represent failed changes as saved. Failed edits retain entered values. Do not add version or ETag conflict checks.

**Never:** Mutate tasks outside the dashboard; a History task must be restored before editing or changing status and receives `409 TASK_IN_HISTORY`. Do not implement History/restore, deletion, Diary behavior, authentication, browser persistence, or database schema changes in this story.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Edit valid details | Dashboard task with non-blank title and optional notes | Persist replacement and return updated task; refresh dashboard from API | — |
| Change status | Dashboard task and one of the three supported statuses | Persist status, refresh recency and dashboard grouping/membership | — |
| Blank title | Missing or whitespace-only title in replacement | No update is persisted | 400 `VALIDATION_ERROR`, “Title is required.” |
| Task outside dashboard | Update requested for a History task | No mutation | 409 `TASK_IN_HISTORY`; UI reports failure |
| Missing task | Unknown task ID | No mutation | 404 `NOT_FOUND` |
| Mutation failure | API rejects or request fails | Keep task and entered values available; do not show success | Clear recoverable error text |

</frozen-after-approval>

## Code Map

- `frontend/src/App.jsx` — task cards currently render title, notes, and static status; reuse `statuses`, dashboard refresh (`loadTasks`), stale-request protection, focus refresh, and create-form validation/feedback patterns.
- `frontend/src/tasksApi.js` — reuse `request()` for JSON and Problem Details; add the contract’s PUT and PATCH operations.
- `frontend/src/App.css` — existing form, card, focus, feedback, and responsive styles; extend these for the chosen edit surface and status control.
- `frontend/src/App.test.jsx` and `frontend/src/tasksApi.test.js` — extend component and API-client coverage for valid/invalid mutations, failures, payloads, and refresh behavior.
- `backend/src/main/java/com/example/simpletodo/task/TaskController.java` — currently exposes only dashboard GET and create POST; add PUT and status PATCH routes.
- `backend/src/main/java/com/example/simpletodo/task/TaskService.java` — reuse repository, `TaskResponse.from`, and `RECENCY_ORDER`; enforce dashboard membership before mutation and refresh `updatedAt`.
- `backend/src/main/java/com/example/simpletodo/task/Task.java` — add a clear entity mutation path; fields currently have no mutators.
- `backend/src/main/java/com/example/simpletodo/task/CreateTaskRequest.java` — follow `@NotBlank` validation when defining full-update and status-only request records; do not allow unsupported fields.
- `backend/src/main/java/com/example/simpletodo/api/ApiExceptionHandler.java` — current generic `ResponseStatusException` mapping labels non-404 statuses `VALIDATION_ERROR`; align 409 history conflicts with `TASK_IN_HISTORY`.
- `backend/src/test/java/com/example/simpletodo/task/TaskControllerTest.java`, `TaskServiceTest.java`, and `TaskPersistenceIntegrationTest.java` — extend existing validation, service, and PostgreSQL persistence/order coverage for update and status changes.
- `_bmad-output/implementation-artifacts/spec-1-1-add-and-view-tasks-on-the-dashboard.md` — completed-story continuity: existing dashboard ordering, request-race protection, focus refresh, frontend/backend test conventions, and README setup; its Code Map and task list show the first slice and what it intentionally deferred.
- `_bmad-output/implementation-artifacts/epic-1-context.md` — authoritative cross-story constraints and mutation semantics.

## Tasks & Acceptance

**Execution:**
- [x] `backend/src/main/java/com/example/simpletodo/task/` — implement validated full replacement and status-only mutations with dashboard-only enforcement, refreshed recency, and API-contract error responses — preserve backend authority and History boundaries.
- [x] `backend/src/test/java/com/example/simpletodo/task/` — cover valid updates, blank titles, supported/unsupported statuses, missing and History tasks, persisted timestamps, and dashboard ordering after mutation — verify contract and data behavior.
- [x] `frontend/src/tasksApi.js` and `frontend/src/App.jsx` — add edit and status controls using the approved edit surface; refetch after success and preserve user input with explicit failure feedback — keep the UI aligned with server state.
- [x] `frontend/src/App.css` — style the chosen editor and status control using existing focus, feedback, and narrow-layout patterns.
- [x] `frontend/src/App.test.jsx` and `frontend/src/tasksApi.test.js` — verify request methods/bodies, validation, retryable failures, and refreshed task grouping/membership.
- [x] `README.md` — document the dashboard’s task editing and progress controls — keep user-facing setup documentation current.

**Acceptance Criteria:**
- Given a dashboard task, when I save a non-blank title and optional notes, then the API persists the replacement and the refreshed dashboard shows the saved values.
- Given a dashboard task, when I choose TODO, In progress, or Completed, then the API saves the matching status and the task appears in the refreshed status group; a completed task can be reopened.
- Given a task mutation succeeds, when the dashboard refreshes, then updated recency and the seven-task limit determine membership, without deleting displaced tasks.
- Given an edit or status request fails, when the error is shown, then the task is not presented as updated and my entered values remain available to retry.
- Given a task is not on the dashboard, when an update is requested, then the API rejects it with `409 TASK_IN_HISTORY` and does not mutate it.

## Implementation Notes

- Added dashboard-only `PUT /api/tasks/{taskId}` and `PATCH /api/tasks/{taskId}/status` endpoints. Full replacements validate and trim titles, both paths refresh `updatedAt`, and unknown/history tasks return the contract's `NOT_FOUND`/`TASK_IN_HISTORY` Problem Details.
- Added inline card editing and status controls. Successful mutations refetch the dashboard; failed detail edits preserve entered fields, and failed status changes remain visible as unsaved selections with a retry action.
- Added backend request/service/controller coverage, frontend mutation and retry coverage, responsive editor/control styles, and README instructions.

## Spec Change Log

## Review Triage Log

- `medium` — `patch`: malformed UUIDs on the new task mutation routes reached the generic exception handler and returned `500 INTERNAL_ERROR`; added a `400 VALIDATION_ERROR` mapping and a controller regression test, which passed.
- `low` — `patch`: the status-change component test checked the returned status label but not which dashboard group contained the task; added assertions for presence under Completed and absence under TODO.
- `low` — rejected: malformed task-dashboard item payloads are not fully validated by the existing API client and could fail during rendering, but the backend contract and persistence constraints guarantee valid task records; defensive validation for corrupted server responses is unlikely to affect everyday use.
- `medium, unverified` — `defer`: the PostgreSQL persistence/cutoff integration test is present but could not be run because Flyway could not connect to the configured test database; mocked service/controller tests do not establish real database persistence behavior.
- Blind Hunter did not review the change because its tool session could not access the system-temp snapshot; the other review layers completed and findings above were triaged.

## Verification

**Commands:**
- `npm --prefix frontend test -- --run` — passed: all 20 frontend tests.
- `npm --prefix frontend run build` — passed: production build succeeds.
- `npm --prefix frontend run lint` — passed: no lint errors.
- `.\gradlew.bat test --rerun-tasks` from `backend` — passed: all 23 standard backend tests executed.
- PostgreSQL integration run with `TASKS_TEST_DATABASE_URL=jdbc:postgresql://127.0.0.1:5433/simple_todo_test` — blocked before test setup because Flyway could not connect to the configured database; all three persistence integration tests remain unverified in this environment.
