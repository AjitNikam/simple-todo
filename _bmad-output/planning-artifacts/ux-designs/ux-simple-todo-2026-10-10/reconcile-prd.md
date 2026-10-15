---
title: "Simple ToDo and Daily Diary — PRD Reconciliation"
status: final
created: 2026-10-10
updated: 2026-10-10
sources:
  - ../../prds/prd-simple-todo-2026-10-10/prd.md
  - ../../prds/prd-simple-todo-2026-10-10/addendum.md
  - DESIGN.md
  - EXPERIENCE.md
---

# PRD Reconciliation

## Result

No UX-relevant PRD requirements were omitted, contradicted, or weakened in `DESIGN.md` or `EXPERIENCE.md`. The updated task-history rule is captured in the PRD and the spines: seven most recently updated dashboard tasks across statuses, with older tasks in a view-only dialog and a return action. The confirmed one-page, two-section structure and technical direction are represented.

## Coverage

| PRD requirement | UX coverage |
|---|---|
| Single-user app; no login, accounts, or multi-user workflows | Foundation; Information Architecture |
| One page with task and diary dashboards | Foundation; Information Architecture |
| Create tasks with required title and optional notes; new tasks default to TODO; blank titles rejected | Component Patterns; State Patterns; Flow 1 |
| Show up to seven most recently updated tasks total across TODO, In progress, Completed; all others stay available in History | Component Patterns; State Patterns; Interaction Primitives; Flows 1 and 6 |
| History tasks button beside Add task opens a view-only dialog; Return to dashboard brings a task into the seven-item set and displaces the oldest visible task | Information Architecture; Component Patterns; State Patterns; Flow 6 |
| Edit, change status, reopen, and delete dashboard tasks; task-history items require return before modification | Component Patterns; Interaction Primitives; Flows 1, 5, and 6 |
| Failed task deletion leaves task visible; communicate operation outcome | Component Patterns; State Patterns; Flows 1 and 5 |
| One user-written full diary or own summary per browser/device local date | Component Patterns; Interaction Primitives; Flow 2 |
| Explicit save; empty content rejected; same-day save updates instead of duplicating | Foundation; State Patterns; Interaction Primitives; Flow 2 |
| Create/edit only on matching current local date; no future creation; past entries read-only | Component Patterns; State Patterns; Interaction Primitives; Flows 2–4 |
| Prior date locks at local midnight; new date becomes eligible | State Patterns; Flow 4 |
| Four previous local calendar dates, excluding today; include dates with no entry | Foundation; Information Architecture; State Patterns; Flows 3–4 |
| Past entries can be retrieved and are read-only; diary deletion never available | Component Patterns; Interaction Primitives; Flow 3 |
| Loading, empty, saved, validation, and recoverable-error feedback; failed saves not shown as saved | Voice and Tone; State Patterns; Accessibility Floor; Key Flows |
| Data persists through normal reloads/restarts | Foundation |
| No generated, rewritten, or AI-summarized diary content | Foundation; Component Patterns; Interaction Primitives; Flow 2 |
| React/Vite frontend; separate Spring Boot REST API; PostgreSQL | Foundation |

## Assumptions and Blockers

- Palette, typography, shape, layout, and responsive behavior are explicitly marked `[ASSUMPTION]` in the design/experience documents; no viewport or breakpoint is claimed as confirmed.
- Explicit diary save and lack of unsaved-draft recovery follow the PRD's indexed assumption.
- No unresolved PRD UX questions or phase-blocking UX decisions were identified. Responsive breakpoint choices and visual identity remain intentionally unconfirmed design assumptions, not blockers to documenting the requested UX.

## Visual references

- `mockups/key-dashboard.html` — one-page Tasks-then-Diary dashboard and empty date/task states.
- `mockups/key-task-history.html` — view-only task History dialog and Return to dashboard action; `[Task title]` is placeholder text, not example product data.
- Both mocks are linked from their relevant sections in DESIGN.md and EXPERIENCE.md; the spines remain authoritative on conflict.

## Gaps or Mismatches

None identified against the confirmed PRD and addendum.
