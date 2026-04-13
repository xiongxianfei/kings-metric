# History And Dashboard Screen Binding Spec

## Goal and Context

Define the real Compose screens that bind the existing history and
dashboard logic to Room-backed repositories once Android persistence
exists.

This spec refines the existing match-history and metrics-dashboard
features into runnable app screens.

## Concrete Examples

### Example 1: Saved Records Exist

Input:
- User opens history or dashboard after saving matches.

Expected behavior:
- History shows saved records from repository flows.
- Dashboard shows aggregate metrics from repository-backed data.

### Example 2: Empty Local Data

Input:
- User opens history or dashboard before any record exists.

Expected behavior:
- Each screen shows an explicit empty state rather than blank or fake data.

## Requirements

- The app MUST render real Compose screens for history, detail, and
  dashboard.
- History UI MUST bind to repository-provided observable data.
- Dashboard UI MUST bind to repository-provided observable data and
  existing metric logic.
- The detail screen MUST show structured fields and degrade safely when
  the linked screenshot file is unavailable.
- Empty and error states MUST remain explicit.
- Business rules for ordering, metric calculation, and missing-file
  handling MUST remain outside leaf composables.

## Error-State Expectations

- Repository read failure MUST render an error state rather than stale
  or fabricated UI.
- Missing linked screenshot files MUST NOT block access to saved field data.

## Edge Cases

- No records exist.
- One record exists.
- Linked screenshot missing on disk.
- Repository flow updates while screen is visible.

## Non-Goals

- Search and filtering.
- Advanced charting.
- Cross-device sync.

## Acceptance Criteria

- History and dashboard render from real repository flows.
- Empty and error states are explicit.
- Detail screen remains usable when screenshot preview is missing.
