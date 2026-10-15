# Review of current spine and contracts

I re-reviewed the current spine and the contract updates against the real compatibility hazards. The previously identified seam categories are now either explicitly closed or converted into explicit operational rules.

## Status: resolved

No remaining material issue is evident at the architecture boundary after the latest changes.

### Confirmed closures

1. Browser state is no longer a persisted source of truth
   - AD-1 now says UI state is presentation/interaction-only, with no persisted browser cache or offline mode.
   - The contract clarifies that only API-confirmed results are treated as saved state; unsaved form text may remain only in the active view for retry.
   - This closes the earlier ownership ambiguity between browser-local draft state and backend-recorded state.

2. Dashboard/history membership and refresh behavior are authoritative and synchronized
   - AD-3 now makes the backend authoritative for dashboard/history membership and ordering.
   - AD-11 and AD-12 require a single shared task state and refresh after mutation and on tab activation.
   - This removes the previous risk that different UI layers would independently compute a different seven-item dashboard or dialog state.

3. Diary timezone is now explicitly revalidated at the client edge
   - AD-5 requires fresh timezone resolution before each diary request and rechecks on focus/visibility and local midnight.
   - The API remains the authoritative date evaluator in that timezone, and the header contract remains strict.
   - This eliminates the earlier mismatch between browser-local date logic and server-side date evaluation.

4. Last-write-wins is now explicit and not hidden
   - The API contract and OpenAPI now disclose that there are no versions / ETags / preconditions.
   - This makes the stale-overwrite semantics intentional instead of an accidental divergence hazard.
   - For the current scope, this is a valid trade-off because the product is local-only, single-user, and explicitly chooses latest committed write wins.

## Remaining review note

There are no remaining material architectural seams requiring a tightened invariant. The remaining concerns are implementation hygiene rather than cross-unit incompatibility: ensure the UI does not keep non-persisted draft text as if it were saved, and ensure refreshes happen after mutations and on activation exactly as specified.
