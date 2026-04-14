# Import Status And Guidance UX Spec

## Goal and Context

Define the import-screen usability improvements that explain what screenshot to
select, what the app is doing after selection, and what the user can do next
after success or failure.

This spec refines the Android import surface for clarity rather than changing
the underlying picker or recognition flow.

## Concrete Examples

### Example 1: Before Import

Input:
- User opens the import screen for the first time.

Expected behavior:
- The screen explains what kind of screenshot is supported.
- The primary import action is obvious.

### Example 2: Unsupported Screenshot

Input:
- User selects a screenshot that fails supported-template validation.

Expected behavior:
- The screen explains the failure clearly.
- The user sees an obvious retry path.

### Example 3: Review-Ready Success

Input:
- User selects a supported screenshot and recognition succeeds.

Expected behavior:
- The screen communicates that review is ready.
- The user has an explicit way to continue into review even if automatic
  navigation does not occur.

## Requirements

- The import screen MUST explain the supported screenshot expectation in
  user-facing language.
- The import screen MUST expose one clear primary action for starting import.
- The import screen MUST show distinct states for idle, in-progress, failed,
  unsupported, and review-ready outcomes.
- Unsupported, unreadable-source, and local-save failures MUST remain distinct
  in user-facing guidance.
- A review-ready result MUST leave the user with an explicit and visible path
  into review.
- Picker cancellation MUST return the user to a non-error import state.
- Picker cancellation MUST NOT be presented as unsupported, import failure, or
  local-save failure.
- Picker cancellation MUST preserve or restore an actionable import screen state
  so the user can retry immediately.
- The import screen SHOULD reduce ambiguity about what happens after screenshot
  selection.

## Interface Expectations

- The idle state should orient the user before they pick a screenshot.
- Failure states should suggest the next useful action.
- Success states should not leave the user staring at a passive status message
  with no actionable next step.

## Error-State Expectations

- Unsupported screenshot: explain mismatch and allow retry.
- Import/read failure: explain that the source could not be used.
- Local save failure: explain that the screenshot could not be stored locally.
- Picker cancellation: return to a usable import state without a failure banner.

## Edge Cases

- Picker is cancelled.
- Review-ready state is visible but automatic navigation does not fire.
- Repeated failures should still leave the import action obvious.

## Non-Goals

- Changing photo picker implementation details.
- Camera capture support.
- Multi-image import.

## Acceptance Criteria

- The import screen explains the supported input and next actions clearly.
- Success and failure states are visually distinct and actionable.
- Review-ready success is never a dead-end state.

## Gotchas

- 2026-04-14: Keep unsupported-template, unreadable-source, local-save
  failure, and generic recognition failure as distinct UI states. If they are
  collapsed into one generic failed state, the import screen loses the exact
  retry guidance this feature exists to provide.
