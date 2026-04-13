# Local Persistence And Screenshot Linkage Spec

## Goal and Context

Define the first-release persistence behavior for saving confirmed match records locally, preserving stable linkage to the stored screenshot, and handling record-write failures without misleading success state.

This spec refines Feature 5 from `docs/plan.md`.

## Concrete Examples

### Example 1: Successful Save

Input:
- User confirms a valid reviewed draft.

Expected behavior:
- The structured record is saved locally.
- The saved record preserves stable linkage to the stored screenshot.

### Example 2: Required Field Still Missing

Input:
- User attempts to save while one required field remains unresolved.

Expected behavior:
- Save is blocked.
- No final record is persisted.

### Example 3: Record Persistence Fails

Input:
- Screenshot storage already succeeded, but record persistence fails.

Expected behavior:
- The system reports that the record could not be saved.
- The screenshot remains locally stored.
- The app does not report a successful final save.

## Requirements

- The system MUST save the confirmed structured record locally.
- The system MUST preserve linkage between the saved record and the stored original screenshot.
- The system MUST block final save when required fields are unresolved.
- The system MUST allow final save when unresolved fields are optional and the spec permits them to remain empty.
- The system MUST keep screenshot linkage stable enough for later re-checking by the user.
- If record persistence fails after screenshot storage succeeded, the system MUST report the failure clearly and MUST NOT claim the record was saved.
- The system MUST NOT add destructive cleanup of the stored screenshot unless a future spec explicitly defines it.

## Error-State Expectations

- Save validation failure MUST keep the reviewed draft editable.
- Record-write failure MUST preserve the user's review state long enough to retry or recover.
- Broken screenshot linkage MUST be treated as an invalid final save condition.

## Edge Cases

- Record persistence fails after screenshot intake succeeded.
- Optional fields remain empty but required fields are valid.
- Screenshot linkage reference is missing at final save time.

## Non-Goals

- Cloud sync or backup of saved records.
- Automatic cleanup of screenshots after failed record save.
- Bulk reprocessing of saved screenshots.

## Acceptance Criteria

- Valid reviewed drafts save locally with screenshot linkage.
- Missing required fields block save.
- Optional unresolved fields can remain empty when allowed.
- Save failure does not masquerade as success.

## Gotchas

- None yet.
