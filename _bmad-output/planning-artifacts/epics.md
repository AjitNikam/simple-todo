---
status: final
stepsCompleted: ["validated-prerequisites", "requirements-extracted", "epic-design-approved", "story-1.1-approved", "story-1.2-approved", "story-1.3-approved", "story-1.4-approved", "epic-1-approved", "story-2.1-approved", "story-2.2-approved", "epic-2-approved"]
inputDocuments:
  - prds/prd-simple-todo-2026-10-10/prd.md
  - prds/prd-simple-todo-2026-10-10/addendum.md
  - architecture/architecture-simple-todo-2026-10-10/ARCHITECTURE-SPINE.md
  - architecture/architecture-simple-todo-2026-10-10/api-contract.md
  - architecture/architecture-simple-todo-2026-10-10/openapi.yaml
  - ux-designs/ux-simple-todo-2026-10-10/DESIGN.md
  - ux-designs/ux-simple-todo-2026-10-10/EXPERIENCE.md
---

# simple-todo - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for simple-todo, decomposing the requirements from the PRD, UX Design, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: The user can create a ToDo item with a required title and optional notes, and a newly created item starts in TODO status.
FR2: The user can review up to seven ToDo items total across statuses, ordered by most recently updated and grouped under TODO, In progress, and Completed.
FR3: The user can open a History tasks dialog to view tasks outside the seven-item dashboard set and return a task to the dashboard.
FR4: The user can edit a ToDo item's title, notes, and status, and a task in History must be restored before it can be changed.
FR5: The user can delete a ToDo item from the dashboard after it is restored from History, and deletion failures must leave the task visible with clear feedback.
FR6: The user can create or update the current day's diary entry, choose a full entry or a self-written summary, and save non-empty content only while the local date is current.
FR7: The user can view a diary entry for a chosen local date and see whether no entry exists, with past entries remaining read-only.
FR8: The user can review the four local calendar dates immediately before today, including dates with no entry, and access today's entry separately from that history.

### NonFunctional Requirements

NFR1: The app must operate as a private, single-user tool with no authentication, multi-user accounts, or shared-workspace behavior in v1.
NFR2: Task and diary data must remain available after ordinary reloads and application restarts.
NFR3: Diary logic must use the browser/device local calendar date and required X-Client-Time-Zone header for write eligibility, read-only behavior, and the four-day history window.
NFR4: The dashboard must enforce a maximum of seven tasks across all statuses, selected by recency, with older tasks remaining persisted and accessible in History.
NFR5: The UI must present clear loading, empty, validation, save, and recoverable error states without misrepresenting saved or deleted state.
NFR6: The app must preserve user-authored diary content exactly as entered without generation, rewriting, summarization, or AI-assisted transformation.
NFR7: The interface must remain usable across narrow and wide browser viewports while preserving all required task and diary controls.
NFR8: The experience must support keyboard access, visible focus indicators, semantic structure, and a WCAG 2.2 AA accessibility baseline.

### Additional Requirements

- The architecture spine specifies React 19.3.0 with Vite 8.3.4, Node.js 26.7.0 for local development, Java 25 LTS, Spring Boot 4.1.1, Gradle 9.8.1, Spring Data JPA managed by Spring Boot, and PostgreSQL 18.6. Node.js 26 is a Current release and must be reassessed before production deployment.
- Frontend and backend are separate deployable units connected only through REST. The backend owns validation, business rules, and persistence orchestration; the frontend must not access the database directly.
- Use environment configuration for `VITE_API_BASE_URL`, database URL/credentials, and the exact allowed CORS origin. Local defaults are loopback-only ports 5173 (UI), 8080 (API), and 5432 (PostgreSQL); do not bind to `0.0.0.0` or use wildcard CORS.
- Use Flyway versioned SQL migrations as the sole schema evolution path. Start PostgreSQL and create the local `simple_todo` database before the API; the API applies migrations and becomes ready only after they succeed.
- V1 runs locally only. Do not expose the unauthenticated app or API to an untrusted network; revisit authentication and authorization after the local personal-use trial and before network exposure.
- The API contract is versioned as v1 without authentication and requires a valid IANA X-Client-Time-Zone header for diary writes and history requests; missing or invalid values return 400 INVALID_TIME_ZONE.
- Task status API values are restricted to TODO, IN_PROGRESS, and COMPLETED, displayed in the UI as TODO, In progress, and Completed; extra create-request properties including status are rejected.
- Dashboard and history endpoints must differentiate the seven-item dashboard set from the remaining older tasks and maintain deterministic ordering by updatedAt desc and id asc.
- Diary entries are stored by local YYYY-MM-DD date, with one entry per date, explicit same-day upserts, future dates rejected, and past entries read-only and non-deletable. Entry types are FULL_ENTRY or SUMMARY.
- Save operations are explicit; empty diary content is rejected and no autosave or draft recovery is included in v1.
- The architecture API explicitly models `null` diary results for missing entries and requires error handling for validation, not-found, and date conflicts.
- No starter template or generation scaffold is specified; the implementation should use a standard web + API baseline rather than a custom greenfield template.
- The architecture spine is final; Sprint Planning should proceed against its decisions and the API contracts.
- The local PostgreSQL service was verified at 18.3; upgrade to the selected 18.6 target before implementation. Node.js 26.7.0 and Amazon Corretto Java 25.0.2.10.1 are installed locally; Node 26 is a Current (non-LTS) branch.
- Client task and diary state is ephemeral presentation state only; no persisted browser cache or offline mode is included. Only API-confirmed results are represented as saved. Refetch after every successful mutation and whenever the page becomes active; dashboard and History dialog share the same application-level task state and refresh pipeline.
- V1 uses last-committed-write-wins for task and diary mutations, without entity versions or ETag preconditions. A stale full-task replacement or diary save may replace newer fields/content; refresh the current server state after each mutation and on page activation.
- Resolve the browser's current IANA timezone before every diary API request. Recheck date/timezone on focus or visibility changes and at local midnight, then refresh diary eligibility and history before treating them as current.

### UX Design Requirements

UX-DR1: Build a single-page app with Tasks and Diary sections in that order, using the existing page structure without introducing extra navigation routes.
UX-DR2: Display at most the seven most recently updated tasks across all statuses on the Tasks dashboard, and place older tasks into a view-only History dialog with Return to dashboard and close actions.
UX-DR3: Implement the Task editor and task status controls with the exact labels TODO, In progress, and Completed, requiring a non-blank title and allowing completed tasks to be moved back to unfinished states.
UX-DR4: Show the four local dates immediately before today in the Diary history, including dates with no entry, and keep today's diary action separate from the historical dates.
UX-DR5: Implement the current-date diary editor as an explicit-save form that supports a full entry or user-written summary, rejects empty content, and updates the same date instead of creating a duplicate.
UX-DR6: Make past diary entries view-only with no edit or delete action, and surface missing-date states clearly as absence rather than a saved blank entry.
UX-DR7: Use a neutral, readable design system with text-led status labeling, visible focus states, and accessible feedback for validation, save, loading, and recoverable failures.
UX-DR8: Preserve the same core task/diary experience across common narrow and wide viewports without introducing separate mobile or desktop workflows.

### FR Coverage Map

FR1: Epic 1 - Create ToDo items with required titles and optional notes.
FR2: Epic 1 - Review the seven most recently updated tasks grouped by status.
FR3: Epic 1 - View and restore tasks in History.
FR4: Epic 1 - Edit task details and progress status.
FR5: Epic 1 - Delete dashboard tasks with reliable failure handling.
FR6: Epic 2 - Write or update today's full diary entry or self-written summary.
FR7: Epic 2 - Retrieve an entry by date and distinguish missing entries.
FR8: Epic 2 - Review the four local calendar dates immediately before today.

## Epic List

### Epic 1: Manage and Retrieve Task Work
Aj can capture intended work, see the seven most recently updated tasks grouped by progress, update or remove dashboard tasks, and retrieve older tasks through History. This epic delivers the complete task-management value independently.
**FRs covered:** FR1, FR2, FR3, FR4, FR5.
**Implementation notes:** Establish the React/Vite single-page app and task dashboard, persist tasks through the separate Spring Boot REST API and PostgreSQL, enforce dashboard/history membership by recency, and provide accessible loading, validation, success, and recovery states. Keep frontend/backend as separate deployable units with REST-only integration; backend owns validation, business rules, and persistence. Source database credentials and API base URLs from environment configuration, configure local CORS deliberately, and do not add authentication in v1.

### Epic 2: Record and Revisit Daily Diary Entries
Aj can write and explicitly save today's own account of the day, choose a full entry or self-written summary, and revisit the previous four local calendar dates with missing entries clearly identified. This epic adds a complete diary workflow to the app without changing the task workflow.
**FRs covered:** FR6, FR7, FR8.
**Implementation notes:** Integrate the Diary section into the single-page app, use the client timezone and local calendar dates for write eligibility and history, preserve content as authored, and keep past entries read-only and non-deletable. Apply the same REST boundary and environment-based configuration; backend validation enforces the diary date and content rules.

## Epic 1: Manage and Retrieve Task Work

Aj can capture intended work, see the seven most recently updated tasks grouped by progress, update or remove dashboard tasks, and retrieve older tasks through History.

### Story 1.1: Add and View Tasks on the Dashboard

As Aj,
I want to add tasks and see my most recently updated work,
So that I can capture and review what I intend to do.

**Requirements:** FR1, FR2.

**Acceptance Criteria:**

**Given** the task dashboard has loaded and contains no tasks
**When** I open the Tasks section
**Then** I see a clear empty state and an Add task action
**And** the dashboard communicates loading separately from a successfully loaded empty list.

**Given** task retrieval fails
**When** I open or retry loading the dashboard
**Then** I see a clear retrieval error and a retry action
**And** the failure is not shown as a successfully loaded empty task list.

**Given** I choose to add a task
**When** I submit a title containing non-whitespace characters and optional notes
**Then** the task is persisted and shown with its title and TODO status
**And** reloading the app retrieves the saved task.

**Given** I submit a valid task and the create request fails
**When** the failure is reported
**Then** I see clear failure feedback and the task is not presented as saved
**And** my entered title and notes remain available to retry.

**Given** I am creating a task
**When** I submit a missing or whitespace-only title
**Then** the task is not created
**And** I see the clear validation message “Title is required.”

**Given** a task creation request contains an unsupported property, including status
**When** the API validates the request
**Then** it rejects the request without creating a task.

**Given** more than seven tasks exist
**When** the dashboard is displayed
**Then** it shows only the seven most recently updated tasks across all statuses, ordered by updated time descending with task ID ascending as the tie-breaker
**And** the visible tasks are grouped under TODO, In progress, or Completed with their status labels and optional notes available.

**Given** I use the task form and dashboard controls by keyboard or a narrow viewport
**When** I navigate and submit the form
**Then** the task actions remain operable with a visible focus indicator and usable reflow
**And** validation and operation feedback are conveyed as readable text, not color alone
**And** controls have accessible names and the interface uses semantic headings and the DESIGN.md baseline tokens.

### Story 1.2: Edit Task Details and Change Progress

As Aj,
I want to edit a dashboard task and change its progress,
So that my task list stays accurate as work changes.

**Requirements:** FR4.

**Acceptance Criteria:**

**Given** a task is on the dashboard
**When** I edit its title and/or optional notes and save valid values
**Then** the updated values are persisted and shown on the task
**And** a missing or whitespace-only title is rejected with “Title is required.” without persisting the edit.

**Given** a task is on the dashboard
**When** I change its status
**Then** the UI offers exactly TODO, In progress, and Completed, mapping them to the API values TODO, IN_PROGRESS, and COMPLETED
**And** I can move a completed task back to TODO or In progress.

**Given** I update a task's details or status
**When** the change succeeds
**Then** its updated time is refreshed and the dashboard's seven-task membership and status grouping are recalculated
**And** if another task falls outside the seven most recently updated, it remains persisted for History.
**And** the client refreshes task state from the API after the mutation and whenever the page becomes active.

**Given** a task was changed in another tab after I loaded it
**When** I submit a valid task update
**Then** the API accepts the update without a version or ETag conflict check and the latest committed full replacement is authoritative
**And** the client refreshes from the API to show the resulting server state.

**Given** an edit or status change request fails
**When** the failure is reported
**Then** I see clear text that the change did not save
**And** the task is not presented as successfully updated and my entered values remain available to retry.

### Story 1.3: Find and Restore Older Tasks

As Aj,
I want to review tasks outside my dashboard and return a task to it,
So that older work stays accessible without crowding my current task list.

**Requirements:** FR3.

**Acceptance Criteria:**

**Given** I am on the Tasks dashboard
**When** I open History tasks
**Then** a dialog lists every task outside the current seven most recently updated tasks, with title, notes when present, and current status
**And** tasks are ordered by updated time descending, with task ID ascending as the deterministic tie-breaker
**And** the dialog distinguishes loading, successfully empty history, and retrieval failure, with a retry path for failure.

**Given** a task is shown in History
**When** I review its available actions
**Then** Return to dashboard and close are available, but edit, status-change, and delete actions are not.

**Given** I return a history task to the dashboard
**When** the restore succeeds
**Then** its recency is refreshed and it appears among the seven dashboard tasks
**And** any displaced task remains persisted in History.

**Given** restoring a history task fails
**When** the failure is reported
**Then** the task remains in History and I see clear failure feedback without a false success state.

**Given** the History dialog opens and closes
**When** I operate it by keyboard
**Then** focus moves into the dialog, remains within it until closed, supports Escape and the close control, and returns to the History tasks trigger.

### Story 1.4: Delete Dashboard Tasks

As Aj,
I want to delete a dashboard task I no longer need,
So that my task list contains only work I still want to track.

**Requirements:** FR5.

**Acceptance Criteria:**

**Given** a task is on the dashboard
**When** I choose delete and the operation succeeds
**Then** the task no longer appears in the task list after the success response
**And** the interface communicates that deletion succeeded.

**Given** a task is on the dashboard
**When** deletion fails
**Then** the task remains visible and its state is not presented as deleted
**And** I see clear text explaining that the operation failed and can retry.

**Given** a task is in History
**When** I review its available actions
**Then** delete is unavailable until I first return that task to the dashboard.

## Epic 2: Record and Revisit Daily Diary Entries

Aj can write and explicitly save today's own account of the day, choose a full entry or self-written summary, and revisit the previous four local calendar dates with missing entries clearly identified.

### Story 2.1: Write and Save Today's Diary Entry

As Aj,
I want to write and save today's diary entry in my own words,
So that I can keep a personal record of the day.

**Requirements:** FR6.

**Acceptance Criteria:**

**Given** today's diary has not been saved
**When** I open the Diary section
**Then** today's local date has a separate action to add its diary
**And** no entry is created until I explicitly submit non-empty content.

**Given** I create or update today's entry
**When** I choose Full entry or Summary, enter non-whitespace content, and explicitly save
**Then** the selected entry type and content are persisted for today's browser/device local calendar date
**And** a subsequent save on the same date updates the one existing entry rather than creating a duplicate
**And** the latest committed save is authoritative without a version or ETag conflict check, including when an older form submission replaces newer content.
**And** the UI choices map to API values FULL_ENTRY and SUMMARY; other values are rejected.

**Given** today's entry has already been saved
**When** I return to the app or open today's diary again
**Then** I can retrieve the saved entry type and content for review or editing
**And** reloading or restarting the app does not lose the saved entry.

**Given** I submit empty or whitespace-only diary content
**When** I attempt to save
**Then** the content is not saved and clear validation feedback explains that content is required.

**Given** an entry is being saved or updated
**When** the request is sent
**Then** it includes the required X-Client-Time-Zone header and the API accepts writes only when the entry date is today in that zone
**And** a past or future date is rejected without creating or changing an entry.

**Given** I have entered diary content
**When** saving fails, or local midnight passes while the app is open
**Then** the app does not claim the content was saved and keeps unsaved text available when possible
**And** the previous date's entry becomes read-only while the new current local date becomes eligible to save.

**Given** a save succeeds
**When** the result is shown
**Then** it confirms success only after persistence and preserves my content exactly as entered, without generating or rewriting it
**And** the interface provides accessible text feedback and remains usable by keyboard and narrow viewport.

**Given** today's diary entry exists
**When** I review its available actions
**Then** I can update and explicitly save it again today
**And** no delete action is provided for the diary entry.

### Story 2.2: Review and Read the Four-Day Diary History

As Aj,
I want to see my diary entries for the four days before today,
So that I can revisit recent days and tell when I did not make an entry.

**Requirements:** FR7, FR8.

**Acceptance Criteria:**

**Given** the Diary section loads for today's browser/device timezone
**When** the history is displayed
**Then** it shows exactly the four local calendar dates immediately before today, oldest first, excluding today
**And** each date is present whether it has an entry or not, while today's diary action remains separate.

**Given** the Tasks and Diary sections are displayed
**When** I use the app
**Then** the Diary section appears after Tasks on the same single-page experience
**And** accessing it does not require navigating to a separate route.

**Given** a history date has a saved entry
**When** I open or review that date
**Then** I can read its date, entry type, and original content
**And** the past entry is read-only with no edit or delete action.

**Given** a history date has no saved entry
**When** its date is displayed
**Then** it is clearly identified as having no entry, not as a saved blank diary.

**Given** diary history is requested
**When** the app calls the API
**Then** it sends the required X-Client-Time-Zone header and uses local calendar arithmetic for the four-date window, including across month/year and daylight-saving transitions.

**Given** history or a date's entry cannot be retrieved
**When** the request fails
**Then** the app distinguishes retrieval failure from a successfully loaded missing entry and offers a retry path.

**Given** the browser/device's local date advances while the app is open
**When** diary dates are refreshed
**Then** yesterday becomes read-only history, the four-date window is recalculated, and the new current date remains separate.
**And** the app rechecks the current date and IANA timezone when the page becomes active or visible, and resolves the timezone again before each diary API request.
