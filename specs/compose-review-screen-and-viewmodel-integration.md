# Compose Review Screen And ViewModel Integration Spec

## Goal and Context

Define the Android review experience that presents the stored screenshot and extracted draft fields together, allows edits, and confirms save through a `ViewModel`-driven Compose screen.

This spec refines the review-and-manual-correction-flow feature for actual Android UI integration.

## Concrete Examples

### Example 1: Draft With Required Missing Field

Input:
- Review screen opens with a draft where one required field is unresolved.

Expected behavior:
- The field is visibly highlighted.
- Confirm/save action stays blocked until the user resolves it.

### Example 2: Save Validation Fails After User Edits

Input:
- User edits fields and taps confirm, but validation still fails.

Expected behavior:
- The screen stays on review.
- User edits remain intact.
- Field-level errors remain visible.

## Requirements

- The review UI MUST be driven by a `ViewModel` that exposes explicit screen state via `StateFlow`.
- The screen MUST show the linked screenshot and extracted fields together when the screenshot file is available.
- The screen MUST render highlighted and blocking fields distinctly.
- The screen MUST allow editing any field, not only flagged ones.
- The screen MUST keep user edits intact when validation or save fails.
- The screen MUST trigger the existing save validation rules before final persistence.
- The screen MUST degrade safely when the screenshot preview file is unavailable.
- The screen SHOULD avoid embedding business rules directly in composables.

## Error-State Expectations

- Save failure MUST keep the user on the review screen with all edits intact.
- Missing screenshot preview MUST show an unavailable preview state without hiding field data.

## Edge Cases

- Required unresolved field blocks confirm.
- Optional unresolved field highlights but does not block confirm.
- Screenshot preview unavailable.
- Save failure after a valid-looking review form.

## Non-Goals

- Fancy image zoom tooling.
- Multi-draft editing.
- Background autosave of edits.

## Acceptance Criteria

- Compose review UI can render the current draft and screenshot state from a `ViewModel`.
- Validation and save failures preserve user edits.
- Blocking vs non-blocking review states are visible to the user.

## Gotchas

- 2026-04-14: A generic `Could not save record locally.` error on a valid review
  can come from calling Room on the main thread, not only from real storage
  failure. Keep the repository-backed save path off the UI thread and cover it
  with an Android test that uses a real Room database rather than only fake
  record stores.
- 2026-04-15: Do not decode the full original screenshot bitmap just to render
  the review preview. Real imported screenshots can be large enough that a
  full-resolution preview decode crashes the review screen immediately after
  import. Use a bounded downsampled decode sized for the preview surface.
- 2026-04-15: For tall portrait screenshots, do not choose the preview sample
  size from a single generic max dimension alone. That can shrink the decoded
  width far below the on-screen review surface and make the preview look blurry
  even though the stored screenshot is clear. Size the preview decode against
  the actual preview surface width and height.
