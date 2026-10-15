# Epic 1 Context: Manage and Retrieve Task Work

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

This epic delivers the core task-management workflow for a single-user app: Aj can capture work, keep the dashboard to the seven most recently updated tasks, update or remove tasks that remain visible, and recover older tasks through History. It establishes the task data model, REST API boundary, and dashboard behaviors that the rest of the app depends on.

## Stories

- Story 1.1: Add and View Tasks on the Dashboard
- Story 1.2: Edit Task Details and Change Progress
- Story 1.3: Find and Restore Older Tasks
- Story 1.4: Delete Dashboard Tasks

## Requirements & Constraints

- Creating a task requires a non-blank title; notes are optional; new tasks begin in TODO.
- The dashboard shows at most seven tasks total across all statuses, ordered by updatedAt descending with id ascending as the tie-breaker.
- Tasks outside the dashboard remain persisted and are accessible only through a History dialog; History tasks are read-only until restored.
- Dashboard items can be edited, status-changed, and deleted; a task in History must be restored before any mutation.
- Task status values are fixed to TODO, IN_PROGRESS, and COMPLETED, displayed as TODO, In progress, and Completed.
- API and UI must use REST only; backend owns validation, business rules, and persistence. The frontend is presentation-only and refreshes from the server after each successful mutation or page activation.
- Input validation, empty/loading/error states, and recoverable failures must be explicit in the UI; failed save or delete must leave the task visible with readable feedback.
- Local-only v1: no auth, shared accounts, or network exposure; runtime configuration must use environment variables and loopback-only local ports.
- Task mutation semantics are last-write-wins without version checks; stale client replacements can overwrite newer data, so the client must refetch after mutations and when the page becomes active.

## Technical Decisions

- The app is a single-page React + Vite frontend and a separate Spring Boot + PostgreSQL backend connected only through REST; the database is not accessed directly from the frontend.
- Task dashboard membership is derived from the server at read time, not stored in a separate flag. The API is authoritative for sorting, grouping, and the seven-item cutoff.
- The canonical task model is simple: id, title, notes, status, createdAt, updatedAt; status is constrained to TODO, IN_PROGRESS, COMPLETED.
- Successful create/update/delete flows follow explicit HTTP semantics: 201 for create, 200 for updates, 204 for delete, and RFC 9457 Problem Details for validation and domain conflicts.
- `TASK_IN_HISTORY` and `TASK_NOT_IN_HISTORY` conflict rules ensure History tasks are restored before editing, deleting, or status-changing.
- Local CORS, database connection strings, and `VITE_API_BASE_URL` come from environment configuration; defaults remain loopback-only local ports. No wildcard CORS or hardcoded secrets.
- Keep task state ephemeral on the client; only API-confirmed data is considered saved, and tasks are re-fetched after successful mutation and on page activation.

## UX & Interaction Patterns

- Tasks appear before Diary on the same page, with the History dialog presented as a modal over the dashboard rather than a separate route.
- The dashboard is grouped by TODO, In progress, and Completed, with older tasks moved to a view-only History dialog and a Return to dashboard action.
- The add/edit task form uses a non-blank title validation message and keeps entered values available for retry after failed submission.
- Keyboard and narrow-viewport support are required: visible focus states, semantic headings, readable feedback text, and reflowed controls without color-only meaning.
- The dialog and dashboard share one task state and refresh pipeline; history is read-only until restored, with focus trapping and Escape/close behavior.

## Cross-Story Dependencies

- Story 1.1 establishes the dashboard model and task creation path, which Story 1.2, 1.3, and 1.4 all depend on.
- Story 1.2 depends on the server-side task state and recency rules established by the dashboard/history split; successful updates may move tasks in or out of the seven-item set.
- Story 1.3 relies on the dashboard/history API contract and refresh semantics from Story 1.1 and 1.2, because restore operations recalculate recency and membership.
- Story 1.4 depends on the same mutation safeguards and failure handling as the earlier task stories and is blocked until a task is restored from History.
