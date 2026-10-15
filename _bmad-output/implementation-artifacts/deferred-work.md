- source_spec: `_bmad-output/implementation-artifacts/spec-1-2-edit-task-details-and-change-progress.md`
  summary: Run the task persistence and dashboard-cutoff integration tests against a reachable PostgreSQL test database.
  evidence: The PostgreSQL integration run could not start because Flyway could not connect to `jdbc:postgresql://127.0.0.1:5433/simple_todo_test`; standard backend tests passed but use mocked repositories for service-level mutation behavior.
