# Review Input Hints And Editing Assistance Spec

## Goal and Context

Define the editing assistance on the review screen so users can correct
extracted fields with less guesswork and fewer formatting mistakes.

This spec refines how manual correction is guided, not what data is stored.

## Concrete Examples

### Example 1: User Corrects A Percentage Field

Input:
- A percentage field is unresolved and the user edits it manually.

Expected behavior:
- The field provides enough hinting that the expected format is obvious.

### Example 2: User Corrects A Numeric Count

Input:
- An optional numeric field was missed by OCR.

Expected behavior:
- The editing experience makes it easy to enter the value correctly.

### Example 3: User Faces Multiple Unresolved Fields

Input:
- Review opens with several unresolved fields across different sections.

Expected behavior:
- The screen helps the user understand what each field means and what must be
  fixed first.

## Requirements

- The review experience MUST help the user understand the expected format for
  editable fields that are likely to be ambiguous.
- The review experience SHOULD use input affordances that reduce formatting
  mistakes where the platform supports them.
- The review experience MUST keep unresolved-field recovery clear and focused.
- The review experience MUST preserve all current validation and save rules.
- The review experience MUST NOT imply that optional highlighted fields are
  required if they are not real blockers.

## Interface Expectations

- Users should not need to infer the meaning of a field from an internal label
  alone.
- Required corrections should be easier to prioritize than optional clean-up.
- Helpful hints should remain concise rather than overwhelming the screen.

## Error-State Expectations

- If a save remains blocked, the screen MUST still point the user toward the
  unresolved required fields.
- Failed edits MUST remain in place when save fails or validation still blocks
  confirmation.

## Edge Cases

- A user enters a value in the wrong format and save remains blocked.
- A field is highlighted for review but is not a save blocker.
- Multiple unresolved fields require different input styles.

## Non-Goals

- Automatic correction of user input beyond existing normalization rules.
- Adding custom keyboards or heavy input frameworks.
- Replacing manual review with automated confidence thresholds alone.

## Acceptance Criteria

- Editing guidance reduces ambiguity around format and meaning.
- Required corrections are easier to understand and complete.
- Existing save and validation behavior remains unchanged.

## Gotchas

- 2026-04-15: Concise format hints may be intentionally reused across multiple
  related fields, especially percentage-style metrics. Compose regression tests
  should assert existence or expected counts instead of assuming each hint
  string is unique on screen.
