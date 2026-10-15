# API Contract — Simple ToDo and Daily Diary

## 1. Scope and conventions

This contract defines the backend REST API for the single-user Simple ToDo and Daily Diary app. It is intentionally small and aligned to the PRD and architecture spine.

- Machine-readable OpenAPI definition: [openapi.yaml](./openapi.yaml)
- Base path: `/api`
- Response format: JSON
- Date format: ISO local date string, `YYYY-MM-DD`
- Timezone rule: diary identity is a local calendar date. Diary writes and history requests send the browser/device IANA timezone in `X-Client-Time-Zone`; the API uses it to evaluate the current local date.
- Timestamp format: RFC 3339 date-time with an explicit UTC offset.
- Content rule: diary content is always user-authored and never generated or rewritten by the app.
- Success shape: the resource or collection is returned directly as JSON; successful task deletion returns no content.
- Error shape: [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457) using `application/problem+json`, with `code` and `details` extension members.
- `openapi.yaml` is the wire-contract source of truth. Each operation's documented response shape is intentional: `GET /api/tasks` returns a bare array, while dashboard/history return named collection objects.

## 2. Shared response and error conventions

### 2.1 Success responses

- `200 OK` for reads and updates
- `201 Created` for create operations
- `204 No Content` for successful deletes

### 2.2 Error response: RFC 9457 Problem Details

```json
{
  "type": "urn:simple-todo:problem:validation-error",
  "title": "Request validation failed",
  "status": 400,
  "detail": "Title is required.",
  "instance": "/api/tasks",
  "code": "VALIDATION_ERROR",
  "details": [
    {
      "field": "title",
      "issue": "must not be blank"
    }
  ]
}
```

`type`, `title`, `status`, `detail`, and `instance` follow RFC 9457. `code` and `details` are API-defined extension members. The HTTP status is authoritative and must match the `status` member. Error responses use the `application/problem+json` media type.

Error codes:

| Code | HTTP status | Meaning |
| --- | --- | --- |
| `VALIDATION_ERROR` | `400` | Invalid body, path date, or field value |
| `INVALID_TIME_ZONE` | `400` | Missing or invalid `X-Client-Time-Zone` |
| `NOT_FOUND` | `404` | Task does not exist |
| `TASK_IN_HISTORY` | `409` | History task must be restored before mutation |
| `TASK_NOT_IN_HISTORY` | `409` | Restore was requested for a task already on the dashboard |
| `DIARY_DATE_NOT_CURRENT` | `409` | Diary write date is past or future in the supplied timezone |
| `INTERNAL_ERROR` | `500` | Unexpected server error |

### 2.3 HTTP status codes

| Code | Meaning |
| --- | --- |
| `200` | Request succeeded |
| `201` | Resource created |
| `204` | Resource deleted successfully |
| `400` | Invalid request or validation failure |
| `404` | Resource not found |
| `409` | Conflict with a domain rule, such as a task in History or a diary write for a non-current date |
| `500` | Unexpected server error |

## 3. Domain models

### 3.1 Task

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "Write release notes",
  "notes": "Keep it short and include testing notes.",
  "status": "TODO",
  "createdAt": "2026-10-10T09:00:00Z",
  "updatedAt": "2026-10-10T10:30:00Z"
}
```

Field rules:
- `id`: UUID string, stable identifier
- `title`: required, trimmed string, minimum length 1 after trim
- `notes`: optional string
- `status`: enum = `TODO`, `IN_PROGRESS`, `COMPLETED`
- `createdAt`, `updatedAt`: RFC 3339 date-time with UTC offset

### 3.2 Diary entry

```json
{
  "id": "5d120e36-2341-4d8a-9d7c-dfd83d92f0db",
  "date": "2026-10-10",
  "entryType": "FULL_ENTRY",
  "content": "Today I finished the onboarding plan and noted the follow-up questions.",
  "createdAt": "2026-10-10T19:00:00Z",
  "updatedAt": "2026-10-10T19:05:00Z"
}
```

Field rules:
- `date`: local calendar date, unique per entry
- `entryType`: enum = `FULL_ENTRY`, `SUMMARY`
- `content`: required, non-empty after trim
- `createdAt`, `updatedAt`: RFC 3339 date-time with UTC offset

## 4. Task endpoints

### 4.1 Create task

`POST /api/tasks`

Request body:

```json
{
  "title": "Plan sprint goals",
  "notes": "Include review and QA checklists."
}
```

Success response: `201 Created`

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "Plan sprint goals",
  "notes": "Include review and QA checklists.",
  "status": "TODO",
  "createdAt": "2026-10-10T09:12:00Z",
  "updatedAt": "2026-10-10T09:12:00Z"
}
```

Validation rules:
- `title` is required
- `title` trimmed length must be > 0
- a new task always starts in `TODO`
- extra request properties such as `status` are rejected

### 4.2 List tasks

`GET /api/tasks`

Optional query params:
- `includeHistory=true` (default) to include all tasks; `false` limits the result to the seven most recently updated dashboard tasks

Tasks are ordered by `updatedAt` descending and then id ascending to break ties. The response is unpaginated.

Success response:

```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "Plan sprint goals",
    "notes": "Include review and QA checklists.",
    "status": "TODO",
    "createdAt": "2026-10-10T09:12:00Z",
    "updatedAt": "2026-10-10T09:12:00Z"
  }
]
```

### 4.3 Get task by id

`GET /api/tasks/{taskId}`

Success response: single `Task` object.

### 4.4 Update task

`PUT /api/tasks/{taskId}`

Request body:

```json
{
  "title": "Plan sprint goals and follow-up",
  "notes": "Updated notes with release checklist.",
  "status": "IN_PROGRESS"
}
```

Success response: updated `Task` object.

Validation rules:
- same as create validation
- all fields may be replaced in the update
- a task in History must first be returned to the dashboard; otherwise return `409 Conflict`
- No entity-version or ETag precondition is used. The latest committed mutation is authoritative; a full replacement submitted from a stale view may replace newer fields. Clients refetch after each successful mutation and whenever their page becomes active.

### 4.5 Update status only

`PATCH /api/tasks/{taskId}/status`

Request body:

```json
{
  "status": "COMPLETED"
}
```

Success response: updated `Task` object.

The task must be on the dashboard; a History task must first be returned, otherwise return `409 Conflict`. No entity-version or ETag precondition is used; the latest committed mutation is authoritative for the fields it changes.

### 4.6 Delete task

`DELETE /api/tasks/{taskId}`

Success response: `204 No Content`

Domain rule:
- if the task does not exist, return `404`
- if the task is currently in History, the API rejects deletion with `409 Conflict`; the user must restore it to the dashboard first

### 4.7 Dashboard tasks

`GET /api/tasks/dashboard`

Returns the seven most recently updated tasks across all statuses, grouped by status in the UI layer.

Success response:

```json
{
  "items": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "title": "Plan sprint goals",
      "notes": "Include review and QA checklists.",
      "status": "TODO",
      "createdAt": "2026-10-10T09:12:00Z",
      "updatedAt": "2026-10-10T09:12:00Z"
    }
  ],
  "total": 1,
  "limit": 7
}
```

### 4.8 Task history

`GET /api/tasks/history`

Returns tasks outside the dashboard set, preserving each task's current status and details.
Tasks are ordered by `updatedAt` descending and then id ascending to break ties. The response is unpaginated.

Success response:

```json
{
  "items": [
    {
      "id": "b6a92c84-2dce-4ca8-9262-2b2ed0d0d7dc",
      "title": "Archive old sprint notes",
      "notes": "Keep for audit trail.",
      "status": "COMPLETED",
      "createdAt": "2026-10-01T08:00:00Z",
      "updatedAt": "2026-10-08T17:00:00Z"
    }
  ],
  "total": 1
}
```

### 4.9 Return task to dashboard

`POST /api/tasks/{taskId}/restore`

Success response: updated `Task` object.

Behavior:
- returning a task to dashboard refreshes its `updatedAt` value and reintroduces it into the recency-ordered set
- if the dashboard exceeds seven items, the least recently updated visible task is displaced to `history`
- if the task is already on the dashboard, return `409 Conflict` with error code `TASK_NOT_IN_HISTORY`

## 5. Diary endpoints

### 5.1 Create or update diary entry for a date

`PUT /api/diary/entries/{date}`

Required header: `X-Client-Time-Zone`, an IANA timezone identifier such as `Asia/Kolkata`.

Request body:

```json
{
  "entryType": "FULL_ENTRY",
  "content": "Today I completed the initial planning and reviewed the backlog."
}
```

Success response: `200 OK`

```json
{
  "id": "5d120e36-2341-4d8a-9d7c-dfd83d92f0db",
  "date": "2026-10-10",
  "entryType": "FULL_ENTRY",
  "content": "Today I completed the initial planning and reviewed the backlog.",
  "createdAt": "2026-10-10T19:00:00Z",
  "updatedAt": "2026-10-10T19:05:00Z"
}
```

Rules:
- `date` is path parameter in local-date format `YYYY-MM-DD` and must equal the current date in `X-Client-Time-Zone`
- `content` cannot be blank after trimming
- `entryType` must be `FULL_ENTRY` or `SUMMARY`
- a future or past date is rejected with `409 Conflict`
- the UI resolves the browser's current IANA timezone before every diary request and sends it for each diary write
- a missing or invalid IANA timezone identifier returns `400 Bad Request` with error code `INVALID_TIME_ZONE`
- no entity-version or ETag precondition is used; the latest committed save replaces the date's entry, so a stale save may replace newer content

### 5.2 Get diary entry by date

`GET /api/diary/entries/{date}`

Success response: `200 OK` with a `DiaryEntry` object.

If no entry exists:

```json
{
  "entry": null
}
```

### 5.3 Get diary history for previous four dates

`GET /api/diary/history`

Optional query param:
- `asOfDate=YYYY-MM-DD` to compute the window relative to a specific local date; default is today

Required header: `X-Client-Time-Zone`, an IANA timezone identifier. If `asOfDate` is omitted, today is calculated in this timezone.

Success response:

```json
{
  "entries": [
    { "date": "2026-10-06", "entry": null },
    { "date": "2026-10-07", "entry": null },
    { "date": "2026-10-08", "entry": null },
    {
      "date": "2026-10-09",
      "entry": {
        "id": "5d120e36-2341-4d8a-9d7c-dfd83d92f0db",
        "date": "2026-10-09",
        "entryType": "SUMMARY",
        "content": "Quick update on the last review cycle.",
        "createdAt": "2026-10-09T21:00:00Z",
        "updatedAt": "2026-10-09T21:11:00Z"
      }
    }
  ],
  "windowStart": "2026-10-06",
  "windowEnd": "2026-10-09"
}
```

Rules:
- returns exactly the four local calendar dates immediately preceding the reference date
- excludes the current date
- returns dates oldest first, each with an `entry` property containing a `DiaryEntry` or `null`

### 5.4 Diary entry is read-only after the date passes

This is a domain rule, not a separate endpoint. After a diary date is in the past, the UI must not send any write request for that date. A backend guard enforces it for safety.

## 6. Validation examples

### 6.1 Invalid task title

`POST /api/tasks`

```json
{
  "title": "   "
}
```

Response:

```json
{
  "type": "urn:simple-todo:problem:validation-error",
  "title": "Request validation failed",
  "status": 400,
  "detail": "Title is required.",
  "instance": "/api/tasks",
  "code": "VALIDATION_ERROR",
  "details": [
    { "field": "title", "issue": "must not be blank" }
  ]
}
```

### 6.2 Invalid diary entry type

`PUT /api/diary/entries/2026-10-10`

```json
{
  "entryType": "AUTO_SUMMARY",
  "content": "This should not be allowed."
}
```

Response:

```json
{
  "type": "urn:simple-todo:problem:validation-error",
  "title": "Request validation failed",
  "status": 400,
  "detail": "entryType must be FULL_ENTRY or SUMMARY.",
  "instance": "/api/diary/entries/2026-10-10",
  "code": "VALIDATION_ERROR",
  "details": [
    { "field": "entryType", "issue": "invalid enum value" }
  ]
}
```

## 7. Implementation guidance for the frontend

- UI fetches are direct to the backend base URL, not hardcoded per environment.
- Use `.env` files for `VITE_API_BASE_URL` and ensure local development CORS is deliberate.
- Do not cache diary writes across dates; each date has a single entry boundary.
- The dashboard should call `/api/tasks/dashboard` and history should call `/api/tasks/history`.
- The diary form should call `/api/diary/entries/{date}` using the local date it is editing and send `X-Client-Time-Zone` from a freshly resolved browser IANA timezone identifier.
- Client task/diary state is in-memory presentation state only; do not persist a browser cache or provide offline mode. Show a mutation as saved only after API confirmation. Refetch after successful mutations and whenever a page becomes active; recheck timezone/date on focus/visibility and at local midnight.

## 8. Acceptance notes

This contract satisfies the PRD and architecture constraints by defining:
- task create/read/update/delete semantics
- dashboard + history semantics with recency ordering
- diary date-based lifecycle and read-only behavior
- RFC 9457 Problem Details error contract
- explicit local-date handling for diary and history
