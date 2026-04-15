# History List Readability Upgrade Spec

## Goal and Context

Define the usability improvements for the saved-match history list so users can
scan saved records quickly, understand what each item represents, and enter
detail confidently.

This spec refines the list presentation, not the history data source.

## Concrete Examples

### Example 1: User Has Several Saved Matches

Input:
- History screen opens with multiple saved records.

Expected behavior:
- The user can quickly distinguish hero, result, and recency.
- The list feels readable without entering detail for each row.

### Example 2: Record Has Missing Hero

Input:
- A saved record exists but the hero field is still empty.

Expected behavior:
- The item remains readable and selectable.
- Missing hero information is handled gracefully rather than making the row
  look broken.

### Example 3: Screenshot Link Is Broken

Input:
- A history row points to a record whose screenshot preview is unavailable.

Expected behavior:
- The list remains usable.
- The user is not misled into thinking the whole record is invalid.

## Requirements

- The history list MUST emphasize the key scan fields needed to choose a saved
  match quickly.
- The history list MUST remain readable when hero, screenshot preview, or other
  secondary information is missing.
- The history list MUST keep record selection obvious.
- The history list SHOULD communicate recency clearly.
- The history list MUST NOT require opening detail just to distinguish basic
  saved records from one another.

## Interface Expectations

- Each row should present the most useful decision-making information first.
- Missing secondary information should degrade gracefully.
- Empty and error states should remain understandable and actionable.

## Error-State Expectations

- If history loading fails, the screen MUST still present a visible error state.
- If no saved matches exist, the empty state MUST remain clear.

## Edge Cases

- Many saved records.
- Missing hero or missing screenshot linkage on a row.
- Empty or failed history load state.

## Non-Goals

- Filtering, search, or sort customization.
- Bulk actions on saved records.
- Editing records directly from the history list.

## Acceptance Criteria

- Users can scan and choose saved matches more quickly than in the current
  low-signal list.
- Missing secondary data does not make rows unusable.
- Existing history loading and navigation behavior remains intact.

## Gotchas

- 2026-04-15: Preview-unavailable messaging should stay secondary row metadata.
  The primary row scan content still needs to read as a valid saved match
  rather than as a broken-record warning.
