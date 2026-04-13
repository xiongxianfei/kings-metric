# Room Repositories And Observed History Dashboard Integration Spec

## Goal and Context

Define the Android local data layer that persists confirmed records in Room and exposes observable flows for history and dashboard state.

This spec refines local persistence, match history, and metrics dashboard features for actual Android integration.

## Concrete Examples

### Example 1: User Saves A Reviewed Record

Input:
- Review flow confirms a valid draft.

Expected behavior:
- Room persists the structured record with stable screenshot linkage.
- History and dashboard observers reflect the new record.

### Example 2: Detail Screen Requests Missing Record

Input:
- Detail `ViewModel` asks Room for a record id that no longer exists.

Expected behavior:
- The UI gets a safe not-found result rather than crashing.

## Requirements

- The app MUST persist confirmed records in Room.
- The Room schema MUST preserve stable linkage to the stored screenshot identifier and local path.
- The repository layer MUST expose observable history data suitable for list and dashboard refresh.
- History and dashboard screens MUST update when the underlying saved record set changes.
- Missing-record lookups MUST return a safe not-found result.
- Repository code MUST keep Room and file-storage concerns out of Compose UI code.
- The repository SHOULD map Room entities into domain models before UI consumption.

## Error-State Expectations

- Room write failure MUST surface a save failure without falsely reporting success.
- Room read failure MUST surface an error state instead of stale fabricated UI data.

## Edge Cases

- Save succeeds and observers refresh.
- Record id requested after deletion.
- Screenshot file exists in DB linkage but no longer exists on disk.

## Non-Goals

- Cloud sync.
- Multi-device merge behavior.
- Arbitrary analytics SQL optimization work.

## Acceptance Criteria

- Room-backed repositories support save, history list, detail lookup, and dashboard observation.
- History and dashboard refresh from observable local data.
- Missing-record and read/write failures degrade safely.
