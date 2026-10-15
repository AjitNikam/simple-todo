# PRD Addendum — Technical Context

This addendum preserves technical direction from the user-provided [technical research brief](../../research/technical-react-and-node-js-todo-application-archi-2026-10-10/brief.md). These choices and implementation details inform downstream architecture work; the PRD's functional requirements remain technology-independent.

## Requested technical direction

- **Frontend:** React with Vite.
- **Backend:** Spring Boot 4.1.1 REST API. The original Spring Boot 4.0.7 value was a provisional research baseline and is superseded.
- **Java runtime:** Java 25 LTS.
- **Build:** Gradle 9.8.1, verified compatible with Java 25 and Spring Boot 4.1.1.
- **Frontend runtime:** Node.js 26.7.0 for local development; it satisfies Vite 8.3.4's engine range. Reassess before production deployment because Node.js 26 is Current, not LTS.
- **Frontend packages:** React 19.3.0 and Vite 8.3.4.
- **Persistence:** PostgreSQL 18.6 target with Spring Data JPA and Flyway versioned SQL migrations; the installed local PostgreSQL service is 18.3 and must be upgraded before implementation.
- **Schema evolution:** Flyway versioned SQL migrations, using the PostgreSQL module and Spring Boot-managed dependency versions.
- **Application shape:** A separate frontend and backend, developed locally, with environment-based database configuration. Bind services to loopback only, use deliberate exact-origin CORS, and do not expose the unauthenticated app to a network before authentication and authorization are added.
- **Scope constraint:** Keep the implementation small and maintainable for a single-user ToDo and daily diary app. Avoid production-scale or enterprise complexity; choose a beginner-to-intermediate-friendly project structure.

## Technical constraints for architecture and implementation

- Use the approved current baseline: React 19.3.0, Vite 8.3.4, Node.js 26.7.0 for local development, Java 25 LTS, Spring Boot 4.1.1, Gradle 9.8.1, Spring Data JPA and Flyway versions managed by Spring Boot, and PostgreSQL 18.6. These versions were checked against current official release and compatibility documentation on 2026-10-10. The original Spring Boot 4.0.7 value was a provisional research baseline, not the final selected version.
- Define minimal ToDo and Diary entry data models, including task status (TODO, in progress, completed), user-authored content and entry type, calendar date, and persistence behavior.
- Define REST contracts for ToDo create, list/read, update, mark-complete, delete, and task-history retrieval/return-to-dashboard operations; and Diary entry create-or-save for a date, retrieve by date, and list the four calendar dates immediately preceding today. Task dashboard selection uses the seven most recently updated tasks across all statuses; all others remain in History. Record endpoint paths, request/response examples, validation behavior, status codes, and a consistent error response shape in the architecture/API artifact.
- Keep API base URLs deployment-configurable rather than hardcoded. Document local frontend-to-backend configuration and a deliberate development CORS policy.
- Diary identity and history use local calendar dates, not server-local timestamps. The user may create or update exactly one Diary entry only on its associated date; past entries are read-only and future entries are prohibited. Cover month/year rollover, daylight-saving transitions, and missing diary dates in tests.
- Preserve the explicit product constraint: diary content and summaries remain user-written; no AI service generates or rewrites them.

## Candidate data shape

The brief calls for a ToDo record with a stable identifier, title, optional notes, status, and created/updated timestamps. The user selected the three Task status values TODO, in progress, and completed.

A Diary entry is associated with one browser/device local calendar date and contains user-authored content plus an indication of whether it is a full entry or a self-written summary. There is one entry per date, and the user can create or update it only on that exact date; the entry is read-only after the date passes, and future dates cannot be written. The history is the four browser/device local calendar dates immediately before today, excluding today.

## Implementation and verification direction

The brief proposes an incremental sequence: establish the project and environment-based database configuration, implement ToDo CRUD and tests, add diary save/retrieval, implement the four-date history, and then verify date boundaries, missing entries, persistence, and loading/empty/error UI behavior.

Minimum validation should cover React component behavior, API/controller behavior, persistence behavior, and PostgreSQL-backed integration tests. In particular, test ToDo create/list/update/complete/delete; dashboard selection and ordering of the seven most recently updated tasks; History retrieval and return-to-dashboard behavior including displacement of the least-recently-updated dashboard item; Diary create/update/retrieval; missing entries; the exact four-calendar-day window; month/year/time-zone boundaries; and saved data after reload or restart. UI validation should cover loading, empty, validation, and recoverable error states. The original brief was a research request, not a completed compatibility finding. The approved baseline above supersedes its provisional Spring Boot 4.0.7 value.

## Further work intentionally deferred

Concrete endpoint payloads, exact paths, status codes, full validation and error contracts, project directory structure, test-container/tool selections, and deployment configuration belong in the architecture and implementation artifacts. Authentication, multi-user data isolation, collaboration, device synchronization, and AI-generated content are outside v1.
