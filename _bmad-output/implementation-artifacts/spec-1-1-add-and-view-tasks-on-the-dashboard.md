---
title: 'Add and View Tasks on the Dashboard'
type: 'feature'
created: '2026-10-13'
status: 'done'
route: 'dispatch'
baseline_commit: 'NO_VCS'
review_loop_iteration: 0
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-1-context.md'
  - '{project-root}/_bmad-output/planning-artifacts/architecture/architecture-simple-todo-2026-10-10/openapi.yaml'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Aj needs a reliable place to capture tasks and see the most recently updated work. Without a persisted dashboard, tasks cannot be trusted to survive reloads, and loading or request failures could be mistaken for an empty or saved state.

**Approach:** Establish the first runnable slice of the approved single-user application: a React/Vite task dashboard backed by a Spring Boot REST API and PostgreSQL. Support task creation and dashboard retrieval only; retain the approved boundaries so later stories can add editing, history, deletion, and diary workflows.

## Boundaries & Constraints

**Always:** Keep frontend and backend separate and communicate through the documented REST contract. Persist task data in PostgreSQL; the API owns validation, TODO initialization, dashboard selection (at most seven across statuses, updatedAt descending then id ascending), and ordering. Show only API-confirmed saved state. Distinguish loading, empty, validation, and request-error states; failed create retains entered values. Use exact visible status labels TODO, In progress, Completed; keep controls keyboard-operable, semantically labeled, readable without color alone, and usable on narrow viewports. Show Tasks before a Diary section with a clearly labeled “Diary is not available yet” placeholder; the placeholder has no diary behavior or false saved/empty-data state. Use the approved React 19.3.0/Vite 8.3.4, Java 25/Spring Boot 4.1.1/Gradle 9.8.1, environment-based configuration, loopback-only service binding, and versioned Flyway SQL. Use the current local PostgreSQL version without upgrading it; the planning artifact reports 18.3, while 18.6 remains the project's approved target.

**Never:** Add task editing, status mutation, History/restore, deletion, diary persistence or controls, authentication, multi-user support, browser persistence/offline behavior, or network exposure. Do not accept client-supplied status on task creation, add an unsupported status, or report a failed operation as saved.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Initial load | Dashboard request pending, then returns no tasks | Loading is shown first; then an empty state with Add task | Do not render the empty state before success |
| Retrieval failure | Dashboard endpoint returns an error | Show a retrieval error and retry action, not an empty list | Keep retry available; surface failure in text |
| Valid create | Non-blank title, optional notes | POST creates a persisted TODO task; refetch shows it in the correct status group and it survives reload | — |
| Invalid title | Missing or whitespace-only title | Create nothing and show “Title is required.” | Keep the form available to correct |
| Create failure | Valid title/notes; POST fails | Do not show the task as saved | Explain failure and retain entered values |
| Dashboard limit | More than seven persisted tasks | Display only the API-selected seven, grouped by their status | Never choose membership independently in the UI |

</frozen-after-approval>

## Code Map

- No application source, package manifests, build files, or tests exist yet; the workspace currently contains planning artifacts and BMAD output only. There is no implementation behavior to reuse or preserve.
- `_bmad-output/implementation-artifacts/epic-1-context.md` -- compiled Epic 1 constraints and cross-story boundaries.
- `_bmad-output/planning-artifacts/architecture/architecture-simple-todo-2026-10-10/ARCHITECTURE-SPINE.md` -- adopted component boundary, configuration, persistence, stack, and test conventions.
- `_bmad-output/planning-artifacts/architecture/architecture-simple-todo-2026-10-10/openapi.yaml` -- canonical create-task and dashboard request/response/error contract; implement only this story’s operations.
- `_bmad-output/planning-artifacts/ux-designs/ux-simple-todo-2026-10-10/DESIGN.md` and `EXPERIENCE.md` -- visual tokens, empty/loading/error patterns, copy, accessibility, and page composition.
- Create the initial implementation in `frontend/` and `backend/`, including task schema migration, API, UI, configuration, and focused tests. Limit Diary to the approved static placeholder; defer diary behavior, later task lifecycle actions, and unrelated planning artifacts.

## Tasks & Acceptance

**Execution:**
- [x] `backend/` -- establish the Spring Boot service, environment-based local configuration and Flyway task schema; implement validated task creation and the authoritative seven-task dashboard endpoint -- persistence, validation, and selection belong to the API.
- [x] `backend/src/test/` -- verify create validation/default status, dashboard limit/order, API error behavior, CORS allowlist, and PostgreSQL persistence -- prove contract and data boundaries.
- [x] `frontend/` -- establish the React/Vite app and implement the task dashboard, grouped status display, create form, loading/empty/error/retry/validation/success states, and API refresh behavior -- keep the UI honest about server state.
- [x] `frontend/` -- show a non-interactive Diary section with a clearly labeled “Diary is not available yet” placeholder -- preserve the approved page order without suggesting diary data has loaded.
- [x] `frontend/` -- add focused component and API-client tests for keyboard-accessible creation, validation, empty/loading distinction, retry, and failed-create value retention -- cover user-visible outcomes.
- [x] `frontend/` and `backend/` -- document required local environment settings and verify loopback-only defaults with exact-origin CORS -- keep the unauthenticated v1 local-only.

**Acceptance Criteria:**
- Given Aj creates a valid task, when the task is saved and the app is reloaded, then the API returns the persisted TODO task and the dashboard displays it with its title, optional notes, and status.
- Given the task dashboard is used by keyboard or at a narrow viewport, when Aj navigates and submits, then controls remain operable with visible focus, semantic names, and text feedback not conveyed by color alone.
- Given Aj reaches the Diary section in this first slice, when diary functionality has not been implemented, then a clearly labeled unavailable placeholder appears without diary controls or a misleading empty/saved state.

### Review Findings

- [x] [Review][Patch] Align the documented frontend URL with the exact configured CORS origin [README.md:21]
- [x] [Review][Patch] Include the required request URI in every problem response [backend/src/main/java/com/example/simpletodo/api/ApiExceptionHandler.java:75]
- [x] [Review][Patch] Preserve client-error statuses for unsupported HTTP methods and media types [backend/src/main/java/com/example/simpletodo/api/ApiExceptionHandler.java:56]

#### Rejected

- false — The reported missing Gradle wrapper is not a workspace issue: `backend/gradlew.bat` and its wrapper files exist; they were omitted from the temporary source snapshot.
- false — The reported missing npm lockfile is disproved by `frontend/package-lock.json`, which exists in the workspace but was omitted from the snapshot.
- false — The report that dashboard `total` undercounts the database is contrary to the contract: the OpenAPI schema defines it as the number of tasks in `items`.
- false — The absence of a History retrieval endpoint is intentional for Story 1.1; History is explicitly excluded from this story's scope.
- false — The dashboard not displaying `total`/`limit` is not a defect against this story's acceptance criteria, which specify the API-selected maximum of seven and defer History behavior.
- false — Status mutation is explicitly prohibited in this story; creating tasks as TODO and displaying the three status groups is the specified first slice.
- false — The reported missing favicon is disproved by `frontend/public/favicon.svg`, which was omitted from the temporary snapshot.
- false — Duplicate dashboard-total report from Edge Case Hunter: the response count intentionally describes `items`, per the OpenAPI contract.
- false — The unregistered-status `ResponseStatusException` crash is not reachable in this implementation: no source code constructs or throws `ResponseStatusException`.
- false — Duplicate dashboard-total verification-gap report: the API contract intentionally defines `total` as the number of items returned, not the database-wide count.

## Implementation Notes

- Added the React/Vite dashboard, explicit create form, API client, semantic status groups, loading/empty/error/retry states, and unavailable Diary placeholder. The frontend refreshes the dashboard after successful create and presents only server responses as saved.
- Added the Spring Boot task API and PostgreSQL/Flyway persistence with TODO initialization, trimmed non-blank titles, UUID identity, RFC 3339 timestamps, and a pageable updatedAt-descending/id-ascending seven-task query. Unknown create fields are rejected.
- Added unit, MockMvc contract/CORS, and opt-in PostgreSQL persistence tests. `TASKS_TEST_DATABASE_URL` enables the tagged persistence test; without it, the standard test task does not require a configured database.
- Added the Gradle 9.8.1 wrapper with the official distribution checksum. The wrapper ran successfully after the Java downloader's CDN connection reset was bypassed by caching the checksum-verified official distribution.
- Verified persistence and Flyway against an isolated disposable database on the current PostgreSQL 18.3 service; the test database was dropped afterward. No user database was migrated or changed.
- Environment check found the existing PostgreSQL service listens on port 5433 and on all interfaces. No PostgreSQL service settings were changed; restrict the service to loopback before using the local-only application.

## Spec Change Log

## Review Triage Log

- false — The alleged mojibake is a display artifact: source files contain U+2026/U+2019/U+2014, no replacement characters or mojibake sequences, and the browser shows the intended loading/error copy.
- false — Dashboard `total` intentionally counts returned items; the OpenAPI schema explicitly defines it that way for this unpaginated response, so seven is correct at the limit.
- medium — Returning to the app did not refresh tasks; added a window-focus refresh.
- medium — Overlapping dashboard requests could let an older response overwrite a newer one; latest-request identity now guards success, error, and loading updates.
- medium — Inputs could be changed during create and then cleared on success; title and notes are disabled while the request is pending.
- low — Network failures incorrectly marked the valid title `aria-invalid`; this flag now represents title validation only.
- low — The loaded-empty transition was not announced; the empty dashboard now has a status live region.
- false — The API's 5432 default follows the approved architecture; the README explicitly documents overriding it for the local 5433 service.
- medium — Persistence tests did not exercise real dashboard cutoff/order; integration coverage now persists nine rows with timestamp ties and checks the exact seven returned IDs.
- low — API-client behavior lacked direct tests; added isolated tests for JSON requests, invalid dashboard data, problem details, malformed JSON, and empty responses.
- false — Narrow layout and keyboard-focus concerns were checked in the browser at 375px: document width matched viewport width, and keyboard navigation exposed `:focus-visible`.
- low — Setup used `npm install` despite a lockfile; README now recommends `npm ci`.
- medium — The initial database-backed dashboard test did not prove persisted ordering/cutoff; the added PostgreSQL integration test covers both.
- medium — `TASKS_TEST_DATABASE_URL` enabled integration tests without selecting their datasource; Gradle now sets the test JVM's datasource URL from that value, verified while the ordinary datasource env variable pointed elsewhere.
- medium — Duplicate report of out-of-order dashboard refresh; covered by the latest-request guard and its concurrency test.
- medium — A successful dashboard response with a non-array `items` value could crash rendering; the API client now rejects it as a recoverable load error and direct-client tests cover the shape guard.
- medium — Duplicate report of integration tests connecting to a non-test database; the Gradle test JVM override now targets `TASKS_TEST_DATABASE_URL`.

## Verification

**Commands:**
- `npm --prefix frontend test -- --run` -- passed: 12 frontend behavior and API-client tests.
- `npm --prefix frontend run build` -- passed: Vite production build.
- `npm --prefix frontend run lint` -- passed: Oxlint.
- `.\gradlew.bat test` from `backend`, with `TASKS_TEST_DATABASE_URL` and matching `SPRING_DATASOURCE_*` set to a disposable PostgreSQL database -- passed: 9 backend unit, MockMvc/CORS, Flyway, and PostgreSQL persistence tests.

**Manual checks (if no CLI):**
- Confirm the local PostgreSQL listener is restricted to loopback before using the local-only application.
