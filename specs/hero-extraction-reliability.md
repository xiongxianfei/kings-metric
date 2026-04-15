# Hero Extraction Reliability Spec

## Goal and Context

Improve hero extraction on the one supported post-match personal-stats
screenshot so the main import path requires manual hero entry less often,
without weakening the existing strict-template, low-confidence, or user-review
rules.

This feature refines the current supported-template recognition path. It does
not add a new OCR provider, a new screenshot template, or any non-visible data
inference.

## Concrete Examples

### Example 1: Supported Screenshot With Readable Hero

Input:
- User imports the supported detailed-data screenshot.
- The screenshot contains enough visible hero text or supported hero signal to
  resolve the hero reliably.

Expected behavior:
- The system creates a draft with `hero` populated.
- `hero` is not left in the blocking review state.
- Existing supported-field extraction behavior remains intact.

### Example 2: Supported Screenshot With Unreadable Hero

Input:
- User imports the supported detailed-data screenshot.
- The rest of the required fields are readable, but the hero cannot be
  resolved reliably from the supported screenshot.

Expected behavior:
- The system still creates a reviewable draft when the rest of the supported
  draft is valid.
- `hero` remains unresolved and review-required.
- The system does not invent or guess a hero value.

### Example 3: Unsupported Screenshot Or Unsupported Language

Input:
- User imports a cropped, unsupported, or unsupported-language screenshot.

Expected behavior:
- The system still rejects the screenshot according to the existing supported
  template rules.
- Hero-reliability improvements do not make unsupported screenshots look
  supported.

## Requirements

### Recognition Behavior

- The supported-template recognition path MUST attempt to resolve `hero` before
  draft creation.
- The system MUST populate `hero` when the supported screenshot contains a
  reliable supported-template hero signal.
- The system MUST keep `hero` unresolved when the supported screenshot does not
  provide a reliable hero signal.
- The system MUST NOT invent, guess, or backfill `hero` from non-visible data,
  saved history, dashboard state, or unsupported-template assumptions.

### Draft And Review Behavior

- When `hero` is resolved reliably, the created draft MUST treat `hero` as
  satisfied rather than review-blocking.
- When `hero` remains unresolved but the rest of the supported draft is still
  valid, the system MUST create a reviewable draft instead of failing the whole
  import.
- When `hero` remains unresolved, the review state MUST continue to block final
  save until the user resolves it manually.
- Hero-reliability improvements MUST NOT weaken the current required/optional
  field rules for any other field.

### Supported-Template Safety

- Hero-reliability improvements MUST preserve the existing supported-template
  validation boundary.
- Hero-reliability improvements MUST preserve the existing unsupported and
  low-confidence behavior for non-hero fields.
- The system SHOULD improve hero extraction on the real supported screenshot
  fixture that currently leaves `hero` unresolved.

## Interface Expectations

- Successful hero extraction should reduce unnecessary manual review burden on
  the main supported import path.
- Failed hero extraction should remain explicit and recoverable through the
  existing review flow, not through hidden fallback behavior.

## Error-State Expectations

- If hero extraction fails but the rest of the supported draft remains valid,
  the system MUST keep the import in the reviewable-draft path.
- If the screenshot is unsupported, the system MUST still reject it rather than
  exposing a partial hero-only success.

## Edge Cases

- The supported screenshot contains partial or noisy hero text.
- The supported screenshot resolves hero successfully while another required
  field remains unresolved.
- The supported screenshot fixture improves hero extraction, but an unsupported
  cropped screenshot still must be rejected.
- Hero extraction succeeds on one supported screenshot style but must not
  silently assume the same pattern on unsupported language or template variants.

## Non-Goals

- Adding support for new screenshot templates or non-Chinese screenshots.
- Adding a second OCR engine or cloud recognition path.
- Using saved history or dashboard usage to guess the hero.
- Full hero-image classification beyond the current supported-template import
  workflow unless a later spec explicitly adds that scope.

## Acceptance Criteria

- The real supported screenshot fixture no longer requires manual hero entry
  when the hero can be resolved reliably from the supported screenshot.
- Supported screenshots with unresolved hero still produce a reviewable draft
  when the rest of the draft is valid.
- Unsupported screenshots remain rejected.
- Existing required-field review/save rules remain unchanged except for the
  improved resolved-hero case.

## Gotchas

- None yet.
