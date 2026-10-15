---
title: "Simple ToDo and Daily Diary — Experience"
status: final
created: 2026-10-10
updated: 2026-10-10
sources:
  - ../../prds/prd-simple-todo-2026-10-10/prd.md
  - ../../prds/prd-simple-todo-2026-10-10/addendum.md
---

# Simple ToDo and Daily Diary — Experience Spine

## Foundation

A single-user web app with one page and two main sections: a task dashboard and a diary dashboard. The Tasks section shows up to seven of the most recently updated tasks across statuses and provides Add task and History tasks actions. Older tasks remain available in the History dialog. The Diary section shows the four local calendar dates before today, including dates with no entry, and provides an action for today's diary. Today's entry is accessed separately from the four-day history.

There is no login or multi-user workflow in v1. Technical direction is React/Vite for the frontend and a separate Spring Boot REST API with PostgreSQL persistence. Saved task and diary data remain available after ordinary reloads and application restarts. The browser/device local calendar date governs diary eligibility, editability, and history.

`DESIGN.md` defines the visual tokens and clearly marks unconfirmed aesthetic and responsive choices as assumptions. Its references, such as `{colors.action}`, `{typography.body.fontSize}`, `{rounded.md}`, and `{spacing.4}`, describe the assumed baseline; they do not imply confirmed brand preferences.

**[ASSUMPTION — responsive behavior]** The same one-page experience should remain usable on common narrow and wide browser viewports. Exact target viewport, breakpoints, and responsive compositions remain undecided; preserve both sections and every required action while reflowing content.

## Information Architecture

| Surface | Reached from | Purpose |
|---|---|---|
| Single app page — Tasks section | App open | Review up to seven of the most recently updated tasks grouped by status; add, edit, change status, or delete a dashboard task. |
| Task History dialog | History tasks button beside Add task | View tasks outside the seven-item dashboard set and return a task to the dashboard. |
| Single app page — Diary section | App open | Review the four preceding local dates, including missing entries, and access today's diary action. |
| Today's diary state | Today's action in Diary | Create or edit the one entry for the current local date; choose full entry or self-written summary and explicitly save. |
| Past-date diary state | A date in the four-day history | Read its saved entry and type, or see that no entry exists. Past dates are read-only. |

These are sections and states of the same page, not a proposal for separate routes. Task History opens as a dialog over the page. Do not add login, settings, search, export, or Diary history beyond the four preceding dates in v1.

The [dashboard mock](../mockups/key-dashboard.html) illustrates the single-page section order and empty states. The [History dialog mock](../mockups/key-task-history.html) illustrates the view-only history and return action; `[Task title]` is a placeholder, not sample product content. These mocks are illustrative; this spine remains authoritative if they differ.

## Voice and Tone

Use concise, neutral language. Give direct status and next-step information without pressure, celebration, streak language, or judgment. Diary copy must not suggest that the app will write or improve the user's content.

| Do | Don't |
|---|---|
| “Add task” | “Get productive!” |
| “TODO”, “In progress”, “Completed” | Substitute unclear icons or extra status names |
| “Add today’s diary” | “We’ll write your daily summary” |
| “Saved.” only after the save succeeds | Show “Saved” for an unsaved or failed save |
| “Title is required.” | Vague validation such as “Something went wrong.” |
| “This date is read-only.” | Offer a past-date edit action that cannot succeed |
| “Couldn’t save. Your changes are still here; try again.” when the entered text remains available | Claim data was saved after a request failed |
| State clearly when a date has no entry | Treat a missing diary date as a blank saved entry |

## Component Patterns

Behavioral rules live here; visual references are in `DESIGN.md` → Components.

| Component | Use | Behavioral rules |
|---|---|---|
| Page and section headings | Single app page | Use a single page heading followed by section headings for Tasks and Diary; keep the reading order aligned with the page order. |
| Task dashboard | Tasks section | Show up to seven tasks total across all statuses, selected by most recently updated and grouped by TODO, In progress, and Completed. Show Add task and History tasks actions together. Include an empty state with a way to add the first task. |
| Task History dialog | Tasks section | Open from History tasks beside Add task. List every task outside the seven-item dashboard set, preserving its current status and details. Tasks are view-only in this dialog; provide Return to dashboard and a way to close the dialog. |
| ToDo item | Each item in its status group | Show the required title and status; make optional notes available when present. Provide ways to edit, change status, and delete. Successful changes update recency and dashboard placement; a failed delete keeps the item visible and communicates failure. |
| Task editor | Add or edit a ToDo item | Title is required; reject missing or whitespace-only titles without creating an item. Notes are optional. A newly created item starts in TODO. |
| Task status control | Each task | Offer exactly TODO, In progress, and Completed. Allow movement among them, including returning a completed task to an unfinished status. |
| Primary action | Task and Diary sections | Use the primary action for Add task and Add today's diary; submit explicit form saves only after validation. |
| Content surface | Task and Diary content groups | Group related tasks, diary dates, or editor content without implying navigation to another page. |
| Diary history | Diary section | Identify each of the four local calendar dates immediately before today, excluding today. Each date is present whether or not it has an entry. Show entry date, type, and user-authored content when available; identify missing content clearly. |
| Today's diary editor | Current local date | Allow one entry for today, with a choice between full entry and the user's own summary. The user explicitly saves non-empty content. The user may update that same date's entry again today; saving updates it rather than making a duplicate. |
| Past-date Diary entry | One of the four historical dates | Display the saved entry and type read-only. If no entry exists, say so. Never offer edit or delete; diary entries cannot be deleted at any date. |
| Feedback | Tasks and diary | Communicate loading, empty, saved, validation, and recoverable-error states close to the affected content. A failed operation is not represented as success. |

## State Patterns

| State | Surface | Treatment |
|---|---|---|
| Loading | Task list, task history, or diary dates/entry | Identify the relevant content as loading; do not imply that empty data has loaded successfully. |
| Empty tasks | Tasks section | Say there are no tasks yet and provide the add-task action. |
| Empty historical date | Diary history | Keep the date visible and mark its entry as absent; do not show a saved blank. |
| Today's diary not yet created | Diary section | Keep today's action available. No entry is created until the user submits non-empty content. |
| Editing today's entry | Today's diary | Make the selected entry type and editable user-authored content clear. Saving is explicit, not autosave. |
| Saved | Task or today's diary | Confirm only after persistence succeeds. Diary text remains as authored. |
| Validation failure | Task or diary editor | Explain the correction: task title cannot be blank; diary content cannot be empty. Do not submit invalid content. |
| Recoverable request failure | Task or diary operation | State that the action failed and what to do next. Do not falsely update the UI to a saved/deleted state; keep entered content available when possible. |
| Past date | Diary history/detail | Read-only. No controls can change or delete that entry. |
| Local date advances | Current diary state | The prior day's entry becomes read-only and the new local date becomes eligible for one entry. Recalculate the four preceding dates using local calendar arithmetic, including month/year and daylight-saving transitions. |
| Missing or failed data retrieval | Affected section/date | Distinguish a recoverable retrieval error from a successfully loaded empty state and offer a retry path. |
| Task History dialog | History tasks action | Show older tasks as read-only; allow return to dashboard or dialog close. An empty history state is distinct from a load failure. |

## Interaction Primitives

- Keep navigation within the one-page Tasks and Diary sections; no extra destinations are implied.
- Task add, edit, status change, and deletion are explicit user actions. Use the exact three status labels. A user can reverse completion by selecting an unfinished state.
- Show at most the seven most recently updated tasks on the dashboard across all statuses. All other tasks remain in History with their statuses preserved. A task update changes its recency and can move it into the dashboard's seven; no task is removed merely because it leaves that view.
- Open History tasks as a dialog beside the Tasks dashboard. Tasks cannot be edited, status-changed, or deleted in History. Return to dashboard is the only task action in History; returning updates recency and displaces the least recently updated visible task if needed.
- Diary entry creation and updates require an explicit save. Permit create/update only when the entry's date equals the browser/device's current local date. Reject future dates; lock past dates.
- Store at most one diary entry for a local calendar date. A same-day save updates that entry. Do not provide a diary delete action.
- The four history dates are the four local calendar dates before today, excluding today; the current date has its own add/edit action. Display missing dates as dates with no entry.
- Preserve user-authored diary content as entered. Do not generate, rewrite, or summarize it.
- Keep operation results legible in text and associated with the task or date they affect.
- **[ASSUMPTION]** Support standard pointer and keyboard interaction for web controls; no shortcut scheme or gesture has been selected.

## Accessibility Floor

Visual contrast and focus appearance are specified by the assumed tokens in `DESIGN.md`, including `{colors.text-primary}`, `{colors.text-secondary}`, `{colors.focus-ring}`, and `{typography.body}`.

- Use semantic headings for the page and its Tasks and Diary sections; preserve a logical reading and focus order.
- Every action and form control has an accessible name. Associate validation and save/error feedback with the relevant control or content.
- Make task status available as text to assistive technology and do not convey it by color alone.
- Expose loading and operation feedback to assistive technology without moving focus unexpectedly.
- Ensure all actions work by keyboard, with a visible focus indicator and no keyboard trap.
- **[ASSUMPTION — accessible modal behavior]** When Task History opens, move focus into the dialog and keep it there until the dialog closes; support Escape and the close control, then return focus to the History tasks trigger.
- Diary date, entry type, read-only status, and missing-entry state are available as text, not visual styling alone.
- **[ASSUMPTION]** Use WCAG 2.2 AA as the implementation accessibility target; exact testing and conformance verification remain implementation work.
- **[ASSUMPTION]** Controls should have comfortable touch target sizes on narrow viewports; no platform-specific target size or breakpoint was selected.

## Key Flows

### Flow 1 — Capture and track a task (Aj, planning work)

1. Aj opens the app and sees up to seven of the most recently updated tasks, separated by TODO, In progress, and Completed.
2. Aj chooses the add-task action and enters a title, optionally adding notes.
3. If the title is blank or whitespace-only, the app explains that a title is required and creates nothing.
4. Aj submits a valid task. It appears in TODO.
5. As work proceeds, Aj changes the task from TODO to In progress and later to Completed.
6. If the work needs attention again, Aj can move it back to an unfinished status.
7. **Climax:** The task appears in the status group matching Aj's latest choice, so the dashboard reflects what is complete and what still needs attention.

Failure: If saving or updating fails, the app identifies the failed operation and does not present the change as persisted. If deletion fails, the task remains visible with an explanation and retry path.

### Flow 2 — Write or update today's diary (Aj, recording the day)

1. Aj opens the Diary section and sees the current local date's diary action separately from the four preceding dates.
2. Aj starts today's entry and chooses either a full entry or a summary Aj writes personally.
3. Aj writes non-empty content. The app does not generate or rewrite any of it.
4. Aj explicitly saves. Empty content is rejected with clear validation feedback.
5. On success, today's entry and selected type are shown as saved. If Aj edits and saves again before the local date changes, the same date's entry is updated rather than duplicated.
6. **Climax:** Aj sees the saved entry for today's date with its type, and the confirmation appears only after persistence succeeds.

Failure: If save fails, the app says it could not save and keeps the unsaved text available when possible. It must not show the entry as saved.

### Flow 3 — Revisit a recent date (Aj, checking the past four days)

1. Aj reviews the four dates immediately preceding the current browser/device local date; today is not one of those four.
2. Aj opens a historical date. If it contains an entry, Aj sees the date, entry type, and original user-authored content; if not, Aj sees that no entry exists.
3. A saved past entry is read-only. There is no edit or delete action.
4. **Climax:** Aj can distinguish a recorded day from a missing entry while the historical content remains unchanged and non-deletable.

### Flow 4 — Local date changes (Aj, crossing midnight)

1. The browser/device local calendar date advances while Aj has the app open or returns to it.
2. The previous date's diary becomes read-only; the new current date becomes eligible for a diary entry.
3. The four history dates recalculate from the new local date, excluding today and respecting calendar boundaries.
4. **Climax:** The app reflects the new local day without permitting an edit to yesterday's entry or a future-dated entry.

### Flow 5 — Remove a task (Aj, cleaning up the task list)

1. Aj chooses delete on a task that is no longer needed.
2. On success, that task no longer appears in the list and the app communicates the result.
3. **Climax:** The task list reflects the successful removal.

Failure: If deletion fails, the task remains visible and Aj is told the action failed. This task deletion behavior does not apply to diary entries; diaries are never deletable.

### Flow 6 — Retrieve an older task (Aj, returning to prior work)

1. Aj opens History tasks beside Add task.
2. A dialog shows tasks outside the seven most recently updated dashboard items, grouped or labeled with their existing status.
3. Aj reviews a task and chooses Return to dashboard; no editing or status change is available inside History.
4. The task's recency is updated and it appears within the dashboard's seven; the least recently updated dashboard task moves into History if needed.
5. **Climax:** Aj can bring an older task back into the active dashboard without losing or silently deleting any other task.

Failure: If the history list fails to load or returning a task fails, keep the dialog open, preserve the current task organization, and explain how Aj can retry.
