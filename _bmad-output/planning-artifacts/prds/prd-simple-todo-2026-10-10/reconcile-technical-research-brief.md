# Reconciliation report: technical brief vs. PRD and addendum

Scope checked: `brief.md` against the current PRD and addendum. Overall: no direct contradictions, but the PRD/addendum omit several material requirements that the research brief makes explicit.

## Material gaps / requirements to preserve

1. Simple, maintainable, non-production-scope intent
   - The brief makes the app intentionally small and maintainable: a simple single-user app, not a production-scale platform, with a clear usable UI and a beginner-to-intermediate implementation path.
   - The PRD describes features and privacy boundaries, but it does not explicitly preserve the brief's design posture that the implementation should stay deliberately lightweight and avoid enterprise-scale complexity.

2. User-written summary constraint is stronger than a general non-goal
   - The brief states the summary must be written by the user and explicitly forbids AI-generated summarization or an external AI service.
   - The PRD captures “user-authored summary” and “no AI-generated summaries,” but this should be treated as a firm product constraint rather than a permissive assumption; the app must never generate, rewrite, or summarize diary content on the user's behalf.

3. Four-day history semantics need confirmation as a requirement, not an assumption
   - The brief calls for a history section showing the previous four calendar days and stresses local date boundaries, including month/year and daylight-saving transitions.
   - The PRD frames “exclude today” as an assumption and leaves the exact semantics in open questions. This should be elevated to a confirmed requirement: the diary history is the four local calendar dates immediately preceding the current date, excluding today.

4. Explicit scope boundary for personal, single-user, no-device-sync product
   - The brief calls out authentication, collaboration, and multi-device synchronization as out of scope.
   - The PRD covers most of this, but the brief is more explicit about the product boundary: this is not a shared, synced, or multi-device diary/task system. That should remain an explicit v1 scope statement to prevent later feature creep.

5. Technical requirements are under-specified in the PRD/addendum
   - The brief requires a concrete implementation baseline and technical guardrails: React + Vite, Spring Boot REST API, PostgreSQL persistence; environment-based DB configuration; local dev CORS setup without hardcoded deployment URLs; explicit API contracts, validation, error responses, and test coverage for date boundaries and missing entries.
   - The addendum notes these as future architecture work, but the PRD does not yet formalize them. This is a material gap because the brief expects the technical constraints to be enforced during implementation, not deferred to later artifacts.

## Conclusion

No direct feature contradiction is present, but the brief contributes several operational guardrails that the PRD/addendum should preserve as explicit requirements. The most important are: keep the app deliberately simple and single-user; ensure the diary summary remains entirely user-authored and not AI-generated; fix the four-day history semantics to a confirmed local-calendar rule; and formalize the technical implementation constraints, particularly environment configuration, API contract discipline, and date-boundary validation.
