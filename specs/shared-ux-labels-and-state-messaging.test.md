# Shared UX Labels And State Messaging Test Spec

## Scope

This test spec covers shared field-label mapping, shared state-message mapping,
and cross-screen consistency for import, review, history, dashboard, and detail.

## Unit Tests

- `T1` Shared field-label mapper returns user-facing labels for all fields shown
  in review, history, dashboard, and detail.
- `T2` Shared state-message mapper returns non-technical messages for empty,
  loading, retryable failure, review-required, blocked-save, and save-success
  states.
- `T3` Shared copy mapping preserves required vs optional and blocking vs
  non-blocking semantics in the returned text or metadata.
- `T4` Missing-preview message explicitly states that saved or extracted field
  data remains available.
- `T5` Unsupported-screenshot message explicitly states that the screenshot
  cannot be used and retrying with another screenshot is the next step.
- `T6` Save-failure copy distinguishes blocked review state from true local
  persistence failure.
- `T7` Import state copy uses user-facing action labels and status wording
  rather than technical implementation terms.
- `T8` Shared labels and messages come from a centralized mapping or equivalent
  single source of truth.

## Integration Tests

- `IT1` Review screen renders shared user-facing labels instead of raw enum-like
  field identifiers.
- `IT2` Import screen unsupported state uses retry-oriented shared wording
  rather than exception-like wording.
- `IT3` History and dashboard empty states use the same no-data wording style
  while preserving screen-specific next-step context.
- `IT4` Detail screen missing-preview state uses the shared fallback wording and
  still shows record data.

## What Not To Test

- OCR or parsing correctness.
- Room persistence semantics.
- Pixel-perfect typography or theme styling.

## Coverage Map

- Shared field labels covered by `T1`, `IT1`, `IT4`
- Shared state messaging covered by `T2`, `T4`, `T5`, `T6`, `T7`, `IT2`, `IT3`,
  `IT4`
- Semantic preservation covered by `T3`, `T6`
- Centralized ownership covered by `T8`

## Not Directly Testable

- Precise wording taste or brand voice beyond clarity and consistency.
