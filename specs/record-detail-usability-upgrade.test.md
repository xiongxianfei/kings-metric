# Record Detail Usability Upgrade Test Spec

## Scope

This test spec covers detail-screen grouped presentation, user-facing labels,
screenshot-preview availability messaging, stable navigation back to history,
and safe missing-record fallback.

## Unit Tests

- `T1` Detail screen-state mapper returns grouped sections with user-facing
  labels instead of raw internal identifiers.
- `T2` Detail state surfaces screenshot-preview availability distinctly from the
  field-data payload.
- `T3` Detail state preserves readable fallback values for optional empty
  fields.
- `T4` Detail route-state mapper preserves predictable back navigation to
  history.

## Integration Tests

- `IT1` Normal saved record renders grouped data with user-facing labels and a
  visible summary section near the top of the screen.
- `IT2` Missing screenshot file shows explicit unavailable-preview messaging
  while keeping saved field data visible.
- `IT3` Incomplete saved record with optional empty fields remains readable and
  does not look corrupted.
- `IT4` Missing-record fallback remains safe and visible.
- `IT5` User can return from detail to history predictably.

## What Not To Test

- Full record editing.
- Delete-record behavior.
- Advanced image tooling.

## Coverage Map

- Grouped labels and summary readability covered by `T1`, `IT1`
- Preview availability handling covered by `T2`, `IT2`
- Optional-field readability covered by `T3`, `IT3`
- Navigation and safe fallback covered by `T4`, `IT4`, `IT5`

## Not Directly Testable

- Subjective design quality beyond grouped structure, user-facing labels, and
  preserved field visibility.
