# Review And Manual Correction Flow Spec

## Goal and Context

Define the first-release review step where the user sees the stored screenshot and extracted fields together, corrects missing or unreliable values, and explicitly confirms before final save.

This spec refines Feature 4 from `docs/plan.md` and depends on the draft produced by the import and recognition flow.

## Concrete Examples

### Example 1: Clean Draft

Input:
- User opens a draft with all required fields resolved.

Expected behavior:
- The screenshot and extracted fields are shown together.
- The user can confirm without edits.

### Example 2: Draft With Low-Confidence Field

Input:
- One numeric field is low confidence.

Expected behavior:
- The field is highlighted for review.
- The user can edit it before confirming.

### Example 3: Draft With Missing Required Field

Input:
- One required field is unresolved.

Expected behavior:
- The field is visibly blocked for save.
- The user cannot finalize until it is corrected.

## Requirements

- The system MUST show the original stored screenshot during review.
- The system MUST show extracted field values during review.
- The system MUST highlight fields that are missing, invalid, or low-confidence.
- The system MUST allow the user to edit extracted values before save.
- The system MUST require explicit user confirmation before final save.
- The system MUST preserve user edits in review state until save succeeds, save is cancelled, or the draft is discarded.
- The system MUST block final save when required fields remain unresolved.
- The system MUST allow final save when only optional fields remain unresolved.

## Error-State Expectations

- If review state cannot load the linked screenshot, the system MUST still preserve field data and SHOULD report that the image preview is unavailable.
- If validation fails on confirmation, the system MUST keep the user in review with their edits intact.

## Edge Cases

- User edits a low-confidence field and then saves.
- User leaves an optional field empty and saves.
- User attempts to save while a required field remains unresolved.
- Screenshot preview fails while the draft fields still load.

## Non-Goals

- Automatic field correction without user confirmation.
- Side-by-side comparison with multiple screenshots.
- Collaborative or shared review workflow.

## Acceptance Criteria

- Review always shows the screenshot and fields together.
- Flagged fields are clearly identifiable.
- User edits are possible before save.
- Required unresolved fields block final save.
- Optional unresolved fields do not block final save.

## Gotchas

- None yet.
