---
title: 'Organize task backend packages'
type: 'refactor'
created: '2026-10-15'
status: 'done'
route: 'oneshot'
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Task controllers, services, repositories, models, DTOs, and exceptions currently share one package, making the backend layers harder to find.

**Approach:** Organize the task feature into role-specific subpackages for controller, service, repository, model, request/response DTOs, and exceptions. Move corresponding tests and update package references while preserving all API behavior, persistence behavior, and public routes.

</frozen-after-approval>

## Implementation Notes

- Moved classes to `task.controller`, `task.service`, `task.repository`, `task.model`, `task.dto.request`, `task.dto.response`, and `task.exception`; moved controller/service tests into corresponding test packages.
- Changed package declarations and imports only; controller routes and service, repository, entity, and database mapping logic are unchanged. These Java types are application internals; the HTTP API remains the consumer contract.
- Verified with `backend\gradlew.bat test` from the backend directory; all configured tests passed. PostgreSQL-tagged integration tests were excluded because `TASKS_TEST_DATABASE_URL` was not set.

## Review Triage Log

- low — Clarified verification scope: PostgreSQL integration tests were excluded, and only package declarations/imports changed; persistence logic and mappings were not edited.
- low — Added the exact target package layout and test placement here; source files were moved and the old flat task package no longer contains classes.
- false — Java fully qualified names changed, but this repository is a Spring Boot application, not a Java library; the HTTP API remains the consumer contract and its routes are unchanged.
- low — Finalized the spec status as `done` after implementation and verification.
