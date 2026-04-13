# Match History UI Spec

## Goal and Context

Define the first-release match history experience for browsing saved matches, opening one saved record, and viewing its linked screenshot and extracted stats.

This spec defines Feature 6 from `docs/plan.md` and depends on local record persistence.

## Concrete Examples

### Example 1: History With Saved Records

Input:
- User opens the history screen after saving multiple matches.

Expected behavior:
- The app shows a list of saved matches in a stable order.
- The user can open one record for more detail.

### Example 2: Empty History

Input:
- User opens history before any records have been saved.

Expected behavior:
- The app shows an explicit empty state instead of a blank list.

### Example 3: Saved Record With Missing Screenshot File

Input:
- A saved record exists, but the linked screenshot file is unavailable.

Expected behavior:
- The record detail still loads field data.
- The screenshot area shows an unavailable state rather than crashing.

## Requirements

- The system MUST show a list of saved match records.
- The system MUST show an explicit empty state when there are no saved records.
- The system MUST let the user open a saved record detail view.
- The detail view MUST show the saved structured fields.
- The detail view MUST attempt to show the linked screenshot.
- The system MUST handle missing screenshot files without crashing and MUST still show saved field data.
- The history view SHOULD use a stable default order that favors recent records first unless a future spec defines another rule.

## Error-State Expectations

- If local history loading fails, the system MUST show a recoverable error state.
- If a requested record no longer exists, the system MUST return to a safe UI state and report the failure.

## Edge Cases

- No saved records exist.
- Many saved records exist.
- One record has a missing screenshot file.
- One record is deleted or unavailable before detail load completes.

## Non-Goals

- Server-backed history.
- Advanced filtering, search, or tagging.
- Cross-device history sync.

## Acceptance Criteria

- User can browse saved matches.
- Empty state is explicit.
- User can open one record and inspect fields plus linked screenshot when available.
- Missing screenshot files do not crash the app.

## Gotchas

- None yet.
