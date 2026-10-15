---
title: 'Create OpenAPI contract for Simple ToDo and Daily Diary'
type: 'feature'
created: '2026-10-10'
status: 'done'
route: 'oneshot'
review_loop_iteration: 0
context:
  - '{project-root}/_bmad-output/planning-artifacts/architecture/architecture-simple-todo-2026-10-10/api-contract.md'
  - '{project-root}/_bmad-output/planning-artifacts/architecture/architecture-simple-todo-2026-10-10/ARCHITECTURE-SPINE.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** The API contract exists as prose, so frontend and backend work lack a machine-readable source for paths, request/response schemas, and error cases.

**Approach:** Add an OpenAPI 3.1 YAML document beside the prose contract. Describe the existing task and diary operations, payloads, validation rules, and shared error envelope without adding authentication or changing product behavior.

</frozen-after-approval>

## Implementation Notes

- Added `openapi.yaml` beside the prose API contract, describing all task and diary operations with reusable request, response, and error schemas.
- Required `X-Client-Time-Zone` on diary writes and history reads so the backend can apply the specified browser/device-local date rules.
- Resolved the diary history representation as exactly four chronological days with an explicit nullable `entry`; standardized timestamps to RFC 3339.
- Kept task creation TODO-only, matching the PRD; clarified History mutations as `409 Conflict`.
- Updated `api-contract.md` to link the OpenAPI file and keep the prose contract consistent with the machine-readable format.
- Addressed content review findings by constraining blank strings, defining all error codes, collection totals/order, restore conflicts, timezone validation, and create-task extra-property behavior.
- YAML syntax and structural checks passed with Ruby Psych: 71 internal references resolved, 12 unique operation IDs, no duplicate YAML keys, and exactly four diary history items. No application code or runtime tests apply to this documentation-only change.

## Review Triage Log

- `false` — The date-specific diary GET does not need `X-Client-Time-Zone`: its path already supplies the local-date identity, and the operation does not evaluate “today”; timezone is required only for current-date write eligibility and default history-window calculation.
- `medium` — Blank-only title/content values passed `minLength`; added `pattern: '\S'` to reject them in the OpenAPI schemas.
- `medium` — `INVALID_REQUEST` was listed without defined behavior; removed it and documented the supported error codes, including the specific invalid-timezone response.
- `medium` — Collection total/order semantics were unclear; specified that totals count returned items, responses are unpaginated, and recency ties use ascending task ID.
- `medium` — Restore behavior for an already-visible task was undefined; it now returns `409 TASK_NOT_IN_HISTORY`.
- `medium` — Invalid IANA timezone handling was undefined; documented `400 INVALID_TIME_ZONE` and the v1 trust of the client-reported timezone.
- `medium` — Task create did not state how an extraneous `status` field is handled; schema forbids extra properties and prose documents rejection.
- `medium` — History ordering was unspecified; defined updatedAt-descending and id-ascending ordering.
