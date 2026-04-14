# Record Detail Usability Upgrade Spec

## Goal and Context

Define the readability and navigation improvements for the saved-record detail
screen so users can understand one record quickly, verify screenshot
availability, and return to history without confusion.

This spec refines the current detail presentation without expanding edit scope.

## Concrete Examples

### Example 1: Normal Saved Record

Input:
- User opens a saved record with a valid screenshot path.

Expected behavior:
- The screen presents the record in a readable grouped format with clear labels.
- Returning to history is straightforward.

### Example 2: Missing Screenshot File

Input:
- User opens a saved record whose screenshot file is unavailable.

Expected behavior:
- The screen explains that the preview is unavailable.
- The saved field data remains fully readable.

### Example 3: Incomplete Saved Record

Input:
- User opens a saved record where some optional fields are empty.

Expected behavior:
- The screen remains readable and does not make the record look corrupted.

## Requirements

- The detail screen MUST use user-facing labels rather than raw internal field
  identifiers.
- The detail screen MUST group related data logically.
- The detail screen MUST make screenshot preview availability clear.
- The detail screen MUST keep field data readable even when the screenshot is
  unavailable.
- The detail screen MUST preserve stable navigation back to history.
- The detail screen SHOULD emphasize the most useful summary information first.

## Interface Expectations

- The screen should be readable without requiring the user to interpret field
  keys or raw storage-like output.
- Missing preview should be treated as a secondary issue, not as total record
  failure.

## Error-State Expectations

- Missing record fallback MUST remain safe and visible.
- Missing screenshot preview MUST not hide or remove the saved field data.

## Edge Cases

- Screenshot preview unavailable.
- Optional fields empty.
- User arrives from history and wants to return quickly.

## Non-Goals

- Full record editing redesign.
- Deleting saved records.
- Advanced image tooling in detail.

## Acceptance Criteria

- Detail is as readable and navigable as the other primary screens.
- Missing preview handling is explicit and non-destructive.
- Existing detail-route safety behavior remains intact.

## Gotchas

- 2026-04-15: Grouped detail cards render labels and values as separate nodes.
  Compose tests should assert section title plus label/value visibility instead
  of assuming the old flat `"Label: Value"` output.
