---
title: "Simple ToDo and Daily Diary"
status: final
created: 2026-10-10
updated: 2026-10-10
---

# PRD: Simple ToDo and Daily Diary

## 0. Document Purpose

This PRD defines the user-facing capabilities and boundaries for a private, single-user web app that helps Aj plan work, track task progress, write a daily diary entry or self-written summary, and revisit recent diary entries. It is intended to guide UX, architecture, and implementation planning. Technical stack research and API-level design are preserved in [addendum.md](./addendum.md); the source brief is [technical research brief](../../research/technical-react-and-node-js-todo-application-archi-2026-10-10/brief.md). Assumptions are called out inline and indexed in §10.

## 1. Vision

The app gives one person a straightforward place to see what they intend to do, what they have completed, and what remains in progress. Instead of splitting task tracking and end-of-day reflection across separate notes, the user can use one private web app to manage both.

At the end of a day, the user can write a full diary entry or a shorter summary in their own words. The app keeps entries associated with their calendar dates and makes the four calendar dates immediately before today easy to revisit. It never generates, rewrites, or summarizes diary content.

## 2. Target User

### 2.1 Jobs To Be Done

- When planning or working through a day, Aj wants to capture intended tasks and update their progress so they can tell what is complete and what still needs attention.
- At the end of a day, Aj wants to record a full diary entry or a concise summary in their own words so the day's record reflects their perspective.
- When reflecting on recent days, Aj wants to see the diary entries from the previous four calendar days without searching separate notes.

### 2.2 Non-Users (v1)

- Other people who need separate accounts, workspaces, or private data boundaries.
- Teams that need shared task lists, collaboration, or task assignment.

### 2.3 Key User Journeys

- **UJ-1. Aj plans work and tracks progress through the day.**
  - Aj opens the app and sees up to seven of the most recently updated tasks grouped by TODO, In progress, and Completed. Aj adds tasks, moves them among statuses as work proceeds, and uses History tasks to retrieve older items when the dashboard would otherwise become crowded.
- **UJ-2. Aj records the day and revisits recent entries.**
  - On a given day, Aj writes either a full entry or a self-written summary for that same date, saves or updates it, and can read it that day. After that date passes, the entry becomes read-only. Aj can revisit entries from the four calendar dates immediately before today in the diary history; future-dated entries cannot be created.

## 3. Glossary

- **ToDo item** — A user-created task describing work the user intends to do, with a progress status.
- **Task status** — The current progress state of a ToDo item: TODO, in progress, or completed.
- **Diary entry** — User-authored content recorded for one local calendar date, either as a full entry or a concise summary; writable only on that date.
- **Diary history** — The view of diary entries associated with the four calendar dates before the current date.
- **Entry type** — Whether the user-authored Diary entry is a full entry or a self-written summary.

## 4. Features

### 4.1 ToDo Management

**Description:** Aj uses a task dashboard to capture intended work and keep its progress current. The dashboard shows up to seven tasks total across statuses, ordered by most recently updated and grouped by status. Older tasks remain available from a History tasks dialog. This realizes UJ-1.

**Functional Requirements:**

#### FR-1: Create ToDo items

The user can create a ToDo item with a required title and optional supporting notes. A new ToDo item starts in TODO status.

**Consequences (testable):**
- A successfully created item appears in the user's task list.
- A missing or whitespace-only title is rejected with a clear validation message and does not create an item.

#### FR-2: Review the task dashboard

The user can view up to seven ToDo items total across statuses, ordered by most recently updated and grouped under TODO, In progress, or Completed.

**Consequences (testable):**
- Each dashboard item displays its title and Task status; supporting notes are available when present.
- When more than seven tasks exist, only the seven most recently updated appear on the dashboard; every other task remains available in History tasks with its current status.
- The dashboard provides an empty state with a clear way to create the first item.

#### FR-3: Retrieve older tasks

The user can open a History tasks dialog from a button beside Add task and view tasks outside the seven most recently updated dashboard items.

**Consequences (testable):**
- The History tasks dialog lists all tasks not currently among the seven most recently updated, preserving their current status and task details.
- Tasks in History are view-only except for a Return to dashboard action; they cannot be edited, status-changed, or deleted while in History.
- Returning a task updates its recency and places it on the dashboard; if this exceeds seven visible tasks, the least recently updated dashboard task moves to History.
- The user can close the dialog and return to the same dashboard context.

#### FR-4: Update ToDo items and progress

The user can edit a dashboard ToDo item's title and notes and update its Task status. A task in History must first be returned to the dashboard.

**Consequences (testable):**
- A user can mark an item complete and return it to an unfinished state if needed.
- The updated values are shown when the user next views the item.
- Task status has exactly three states: TODO, in progress, and completed.
- Updating a task makes it one of the most recently updated tasks and can move it into the seven-item dashboard set.

#### FR-5: Delete ToDo items

The user can delete a ToDo item from the dashboard when it is no longer needed. A task in History must first be returned to the dashboard.

**Consequences (testable):**
- A deleted item no longer appears in the task list after the operation succeeds.
- The UI communicates success or failure and does not remove the item from view when deletion fails.

### 4.2 Daily Diary

**Description:** Aj can record their own account of the current day, choosing a full Diary entry or a concise Entry type that they write themselves. There is at most one Diary entry per local calendar date. The user may create or edit it only on that matching date; past entries are read-only and future-dated entries cannot be created. Content is not generated or transformed by the app. This realizes UJ-2.

**Functional Requirements:**

#### FR-6: Write and save a Diary entry

The user can create or update the current day's Diary entry, choose an Entry type (full entry or self-written summary), and save it. The user cannot create an entry for a future date or edit an entry after its associated local calendar date has passed.

**Consequences (testable):**
- A saved entry is associated with exactly one local calendar date and can be retrieved for that date.
- Saving again on the same date updates that date's entry rather than creating a duplicate.
- An entry can be created or updated only while its associated local date is the current date; a future date is rejected and a past entry is read-only.
- When the browser/device local date advances at midnight, the prior date's entry becomes read-only and the new current date becomes eligible for an entry.
- The user-authored content is preserved as entered; the app does not generate or rewrite it.
- Saved Diary entries cannot be deleted.
- Empty content is not saved.
- **[ASSUMPTION]** The user explicitly saves the entry; autosave and recovery of unsaved drafts are not part of the first version.

#### FR-7: Retrieve a Diary entry by date

The user can open an available date and view its saved Diary entry and Entry type, or see that no entry exists for that date. Past entries are view-only.

**Consequences (testable):**
- A date with no saved entry is presented as missing content, not as a successfully saved blank entry.
- The current date's entry can be edited and saved only on that date; previously saved past entries cannot be changed.

#### FR-8: Review the four-day Diary history

The user can view the Diary history for the four local calendar dates immediately preceding the current date, excluding today.

**Consequences (testable):**
- The history identifies each of the four dates, including dates with no entry.
- Entries are displayed with their date and user-authored content; missing dates have a clear empty state.
- The four dates are calculated using the user's local calendar day, including across month, year, and daylight-saving transitions.
- The current day's entry is accessed separately from this history section.

## 5. Non-Goals (Explicit)

- Authentication, authorization, accounts, and role management in v1.
- Multiple users, shared data, collaboration, or synchronization between devices.
- AI-generated summaries, content suggestions, rewriting, or external AI services.
- Reminders, notifications, recurring tasks, calendar integrations, or advanced task prioritization.
- Analytics or usage tracking beyond what is necessary to verify the product during personal use.

## 6. MVP Scope

### 6.1 In Scope

- A single-user web experience for managing a personal ToDo list.
- Create, list, update, complete/uncomplete, and delete ToDo items.
- Show the seven most recently updated tasks on the dashboard, with a view-only history dialog and a Return to dashboard action for older tasks.
- A daily Diary entry with a choice between a full entry and a user-written summary.
- One saved Diary entry per calendar date, with retrieval and editing.
- Diary history for the previous four calendar dates, including clear handling of dates with no entry; task history remains available for all tasks outside the dashboard's seven-item set.
- Persistence across normal app reloads and restarts.

### 6.2 Out of Scope for MVP

- Search, export, backup/restore UI, and Diary history beyond the four-day view.

## 7. Success Metrics

Given the single-user scope, success is assessed through a short personal-use trial rather than product analytics. **[ASSUMPTION]** The initial trial lasts two weeks.

**Primary**
- **SM-1**: After a two-week trial, Aj can capture tasks, review the seven most recently updated tasks, retrieve and return older tasks from History, and retrieve diary entries for the current and previous four calendar dates without losing saved content. Validates FR-1–FR-8.
- **SM-2**: At the end of the two-week trial, Aj chooses to continue using the app; if not, the product scope is revisited before further feature work. Validates FR-1–FR-8.

**Counter-metrics (do not optimize)**
- **SM-C1**: Diary writing remains optional and user-directed; do not optimize for daily entry streaks or a target volume of personal content. Counterbalances SM-2.
- **SM-C2**: Task maintenance should not require more effort than the planning value it provides; avoid adding workflow or tracking complexity to improve apparent completion rates. Counterbalances SM-1.

## 8. Cross-Cutting Requirements and Guardrails

### Privacy and access boundary

- V1 is for one user and provides no app-level authentication or authorization.
- Diary content is personal. The app must be treated as a private single-user tool and must not be exposed to an untrusted network without a separate access-control measure.
- The app must not send diary content to an external AI service or use it for generated content.

### Date and data integrity

- Diary dates are calendar dates, not timestamps chosen by server location.
- The browser/device local calendar date determines the one-entry-per-day rule, edit window, and four-day history window.
- At local midnight, the previous date's entry becomes read-only and the new current date becomes eligible for a new entry.
- The task dashboard contains at most seven tasks across all statuses, selected by most recently updated time; tasks outside that set remain persisted and accessible in History.
- Saved task and diary data remain available after normal reloads and application restarts.

### Interaction states

- Task and diary screens provide clear loading, empty, saved, validation, and recoverable error states.
- A failed save or update must be surfaced; the UI must not present unsaved content as saved.
- While data is loading, the relevant view shows a loading state; when a list or date has no saved data, it shows an empty state; and validation or request failures explain what the user can do next.

## 9. Open Questions

There are no outstanding product questions for this version.

## 10. Assumptions Index

- §4.2 FR-6 — The user explicitly saves Diary entries; unsaved draft recovery is out of scope.
- §7 — The initial personal-use trial lasts two weeks.
