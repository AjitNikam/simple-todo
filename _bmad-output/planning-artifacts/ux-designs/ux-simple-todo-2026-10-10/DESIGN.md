---
title: "Simple ToDo and Daily Diary — Design"
status: final
created: 2026-10-10
updated: 2026-10-10
sources:
  - ../../prds/prd-simple-todo-2026-10-10/prd.md
  - ../../prds/prd-simple-todo-2026-10-10/addendum.md
name: "Simple ToDo and Daily Diary"
description: "A single-user web app for tracking task progress and recording a user-written daily diary."
colors:
  canvas: '#F7F8FA'
  surface: '#FFFFFF'
  text-primary: '#1F2937'
  text-secondary: '#4B5563'
  border: '#D1D5DB'
  action: '#1D4ED8'
  action-text: '#FFFFFF'
  feedback-error: '#B42318'
  feedback-success: '#067647'
  focus-ring: '#1D4ED8'
typography:
  heading:
    fontFamily: 'system-ui, sans-serif'
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
  body:
    fontFamily: 'system-ui, sans-serif'
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
  label:
    fontFamily: 'system-ui, sans-serif'
    fontSize: 14px
    fontWeight: '500'
    lineHeight: '1.4'
  meta:
    fontFamily: 'system-ui, sans-serif'
    fontSize: 13px
    fontWeight: '400'
    lineHeight: '1.4'
rounded:
  sm: 4px
  md: 8px
  lg: 12px
  full: 9999px
spacing:
  '1': 4px
  '2': 8px
  '3': 12px
  '4': 16px
  '5': 24px
  '6': 32px
  section-gap: 32px
  page-gutter: 24px
components:
  primary-action:
    background: '{colors.action}'
    foreground: '{colors.action-text}'
    radius: '{rounded.md}'
  content-surface:
    background: '{colors.surface}'
    foreground: '{colors.text-primary}'
    border: '{colors.border}'
    radius: '{rounded.md}'
  text-field:
    background: '{colors.surface}'
    foreground: '{colors.text-primary}'
    border: '{colors.border}'
    focus-ring: '{colors.focus-ring}'
    radius: '{rounded.sm}'
  inline-feedback:
    error: '{colors.feedback-error}'
    success: '{colors.feedback-success}'
  task-status:
    text: '{colors.text-primary}'
    surface: '{colors.canvas}'
---

## Brand & Style

The confirmed product shape is a practical, single-user web tool with one page and two main sections: a task dashboard and a diary dashboard. No brand, palette, or visual style was supplied. **[ASSUMPTION]** Use a restrained, neutral interface whose hierarchy comes from readable text, spacing, and clear section boundaries; treat the tokens below as a replaceable starting point, not a user-selected identity. Keep task administration and personal writing equally legible without adding decorative or motivational elements.

## Colors

**[ASSUMPTION]** The small, neutral palette is a baseline for implementation, not a confirmed preference.

- **Canvas (`{colors.canvas}`)** separates the page background from content.
- **Surface (`{colors.surface}`)** is for readable content areas and input fields.
- **Primary text (`{colors.text-primary}`)** carries titles, task names, and diary content; **secondary text (`{colors.text-secondary}`)** carries supporting information such as dates.
- **Border (`{colors.border}`)** defines subtle boundaries between content and sections.
- **Action (`{colors.action}`)** is reserved for primary actions; pair it with `{colors.action-text}`.
- **Feedback colors (`{colors.feedback-error}`, `{colors.feedback-success}`)** are reserved for explicit error and saved feedback. Do not rely on color alone: include text.
- **Focus (`{colors.focus-ring}`)** makes keyboard focus visible.

Task statuses must remain understandable from their written labels, not color alone. Do not assign emotional meaning or additional workflow states through color.

## Typography

**[ASSUMPTION]** Use the system sans-serif stack for a dependable baseline; no typeface or typographic character was chosen. `{typography.heading}` identifies page and section headings, `{typography.body}` is used for task and diary content, `{typography.label}` for form controls, and `{typography.meta}` for dates and secondary details. Preserve user-authored diary text without transformation or typographic decoration that changes its meaning.

## Layout & Spacing

**[ASSUMPTION]** Use a single page with a clear reading order and a vertically stacked layout as the baseline. The confirmed section order is Tasks followed by Diary. Any visual separation beyond clear headings is not prescribed. Use `{spacing.section-gap}` between major sections and `{spacing.page-gutter}` at page edges, reducing gutters when needed to preserve usable content width.

**[ASSUMPTION — responsive behavior]** The web app should remain usable on common narrow and wide browser viewports, but no target device, breakpoint, or desktop/mobile composition was selected. Reflow the same two sections without hiding required actions or diary dates; do not introduce a separate mobile screen or navigation model.

Dashboard layout reference: [dashboard mock](../mockups/key-dashboard.html) illustrates the Tasks-then-Diary page with empty task groups and the four preceding diary dates. The mock is illustrative; this spine remains authoritative if they differ.

Task History reference: [History dialog mock](../mockups/key-task-history.html) illustrates the view-only dialog and Return to dashboard action. Its `[Task title]` text is a placeholder, not sample product content; this spine remains authoritative if the mock differs.

## Elevation & Depth

**[ASSUMPTION]** Use flat surfaces and borders for grouping; elevation is not required for hierarchy. Avoid adding shadows unless implementation testing shows a clear separation need.

## Shapes

**[ASSUMPTION]** The radii `{rounded.sm}`, `{rounded.md}`, and `{rounded.lg}` are a neutral starting scale for fields, controls, and content surfaces respectively. Use shape consistently; no distinctive shape language was specified.

## Components

- **Page and section headings** — Use `{typography.heading}` to identify the one-page app and its Tasks and Diary sections. Keep both sections present on the same page.
- **Task dashboard** — Show no more than the seven most recently updated task items, grouped by their status. Older items belong in the History dialog, not in a truncated scroll area that hides the seven-item limit.
- **ToDo item** — Show the required title and current status; expose optional notes when present. Status uses exactly TODO, In progress, and Completed. Keep text labels visible and distinguishable without color.
- **Task editor** — Use `{components.text-field}` for title and optional notes. Provide Add task and edit controls for dashboard items; a missing or whitespace-only title receives clear validation feedback and is not created. A new item begins in TODO.
- **Task status control** — Allow dashboard items to move among TODO, In progress, and Completed, including back to an unfinished state.
- **Task History dialog** — Place the History tasks button beside Add task. The dialog lists ToDo items outside the seven-item dashboard limit, with their current status. Style the Return to dashboard action distinctly from Add task without introducing a new color role.
- **Diary history** — Identify each of the four local calendar dates preceding today, including dates with no entry. An entry shows its date, type, and user-authored content; an absent entry is not represented as a saved blank.
- **Today's diary editor** — Provide an action for today's diary. It allows the user to choose full entry or their own summary, enter content, and explicitly save. Empty content is not saved. The app does not generate or rewrite the content.
- **Past-date Diary entry** — Past entries are view-only. Do not provide an edit or delete action. Diary entries are never deletable.
- **Primary action** — Use `{components.primary-action}` for the principal action in a section or form; do not add actions or surfaces not required by the confirmed scope.
- **Content surface** — `{components.content-surface}` groups related content without implying another page or workflow.
- **Feedback** — Use `{components.inline-feedback}` with readable text for loading completion, saved, validation, and recoverable-error outcomes. A failed save must never look saved.

## Do's and Don'ts

| Do | Don't |
|---|---|
| Keep the task dashboard and diary dashboard on one page | Add unconfirmed pages, account flows, or extra sections |
| Make all three task statuses explicit and allow returning work to an unfinished state | Imply a fourth status or use color as the only status signal |
| Clearly distinguish today's editable diary from read-only past dates | Allow past edits, future entry creation, or diary deletion |
| Show whether a diary date has an entry or is empty | Represent missing content as a successfully saved blank entry |
| Treat every palette, type, shape, and layout token as an assumption until confirmed | Present the assumed visual baseline or viewport behavior as a user preference |
| Preserve diary writing as user-authored text | Generate, rewrite, or summarize diary content for the user |
