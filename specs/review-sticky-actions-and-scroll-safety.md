# Review Sticky Actions And Scroll Safety Spec

## Goal and Context

Define the review-screen layout behavior that keeps the screenshot preview,
field form, and primary action reachable on normal phones without layout traps.

This spec exists because the current review task can become unusable if the
preview image dominates the screen or actions fall below the fold.

## Concrete Examples

### Example 1: Tall Screenshot Preview

Input:
- Review opens with a large portrait screenshot preview.

Expected behavior:
- The preview remains visible but does not consume the whole screen.
- The user can still reach editing fields and the primary save action.

### Example 2: Many Editable Fields

Input:
- Review opens with many grouped fields and multiple unresolved items.

Expected behavior:
- The user can scroll through the full form safely.
- The primary action remains discoverable.

### Example 3: Small Phone

Input:
- Review opens on a normal phone-sized display.

Expected behavior:
- No critical action is effectively hidden by layout assumptions that only work
  on taller or larger screens.

## Requirements

- The review screen MUST keep the primary save action reachable on normal
  phones.
- The review screen MUST keep editable fields reachable even when a screenshot
  preview is present.
- The screenshot preview MUST NOT dominate the screen so completely that the
  review form becomes effectively unusable.
- The layout MUST support scrolling through the full review task safely.
- The layout SHOULD preserve enough preview context that the user can still
  cross-check extracted data against the image.

## Interface Expectations

- Primary action placement should remain obvious even after the user scrolls.
- The review page should behave like one coherent task, not multiple unrelated
  scrolling traps.
- Preview and form should coexist without one making the other unreachable.

## Error-State Expectations

- Missing preview MUST degrade to a usable review form without leaving empty
  layout gaps.
- Layout refinements MUST NOT hide blocker or error messages when they are
  present.
- Opening the software keyboard MUST NOT make the active field or primary save
  action effectively unreachable on normal phones.

## Edge Cases

- Very tall screenshot preview.
- Many grouped sections with a long form.
- A blocker summary plus preview plus form on a smaller device.
- Editing while the software keyboard is open.

## Non-Goals

- Image zoom, pan, or advanced media controls.
- Tablet-optimized split-pane review.
- Changing save validation semantics.

## Acceptance Criteria

- Review remains usable on normal phones with real portrait screenshot
  previews.
- Users can reach both form fields and the primary action without layout traps.
- Preview handling does not regress existing review behavior when no image is
  available.

## Gotchas

- 2026-04-15: The most reliable phone-sized review layout in this repo is one
  vertical form surface with the primary save action anchored outside the
  scrolling content. Nested scrolling or in-form save buttons are easier to
  lose once previews, blocker cards, and IME resizing are all present.
