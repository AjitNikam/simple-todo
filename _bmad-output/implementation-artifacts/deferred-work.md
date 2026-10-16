- source_spec: `_bmad-output/implementation-artifacts/spec-1-2-edit-task-details-and-change-progress.md`
  summary: Run the task persistence and dashboard-cutoff integration tests against a reachable PostgreSQL test database.
  evidence: The PostgreSQL integration run could not start because Flyway could not connect to `jdbc:postgresql://127.0.0.1:5433/simple_todo_test`; standard backend tests passed but use mocked repositories for service-level mutation behavior.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-2-code-review-fixes.md`
  summary: Add a PostgreSQL integration test proving concurrent task mutations serialize dashboard membership checks.
  evidence: Service tests cover refreshed membership decisions, but a separate-transaction contention test could not be validated because `TASKS_TEST_DATABASE_URL` is not configured.
