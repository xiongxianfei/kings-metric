# Review Field Grouping And Labels Spec

## Goal and Context

Define the user-facing structure of the review screen so extracted match fields
are grouped logically, labeled clearly, and presented with obvious required vs
optional and blocker semantics.

This spec refines the review screen information architecture, not the save
rules.

## Concrete Examples

### Example 1: Clean Draft

Input:
- Review opens for a draft with most fields resolved.

Expected behavior:
- Fields appear in logical groups rather than as one long undifferentiated
  list.
- Labels are user-facing and readable.

### Example 2: Multiple Blockers

Input:
- Review opens with several unresolved required fields.

Expected behavior:
- The screen summarizes that save is blocked.
- The blocked fields are easy to locate and understand.

### Example 3: Optional Missing Fields

Input:
- Review opens with one or more optional unresolved fields.

Expected behavior:
- The screen shows that those fields still need review.
- The user can understand that they are not save blockers.

## Requirements

- The review screen MUST group fields into logical sections instead of one flat
  unlabeled list.
- The review experience MUST define stable section identifiers and a stable
  field-to-section mapping so grouping remains testable and predictable.
- The review screen MUST use user-facing labels rather than internal enum-style
  identifiers.
- The review screen MUST make required vs optional meaning visible.
- The review screen MUST make blocking vs non-blocking review states visible.
- The review screen MUST help the user find blocked fields quickly.
- The review screen MUST show a blocker summary in a consistent location when
  required unresolved fields exist.
- The review screen MUST preserve the ability to edit any field.
- The grouping and labels MUST NOT alter the underlying validation behavior.

## Interface Expectations

- A user should be able to identify what kind of information each section
  contains without reading every field first.
- Blocker summaries should guide the user toward resolution rather than only
  restating that save is blocked.
- Highlight-only fields should be visually distinct from true blockers.

## Error-State Expectations

- If the screenshot preview is unavailable, grouped field data MUST still
  remain fully usable.
- If save remains blocked, the screen MUST explain that required information is
  still unresolved.

## Edge Cases

- Draft with many missing required fields.
- Draft with only optional unresolved fields.
- Draft where the screenshot preview is unavailable but field data is present.

## Non-Goals

- Changing field extraction logic.
- Adding new tracked fields.
- Full visual redesign of editing components outside the grouping and labeling
  problem.

## Acceptance Criteria

- Review fields are grouped logically and labeled in user-facing language.
- Required vs optional and blocking vs non-blocking states remain explicit.
- Users can locate blocked fields faster than in the current flat-list layout.

## Gotchas

- 2026-04-15: Once fields are grouped into multiple sections, phone-sized
  review screens will not keep every section header in the initial viewport.
  Keep blocker guidance in a consistent top location and write Compose tests so
  top guidance must be visible while below-the-fold section content only needs
  to exist and remain reachable.
