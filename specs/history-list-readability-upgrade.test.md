# History List Readability Upgrade Test Spec

## Scope

This test spec covers history-row scanability, graceful degradation for missing
secondary data, obvious selection behavior, and empty/error-state clarity.

## Unit Tests

- `T1` History row mapper promotes the primary scan fields needed to identify a
  match quickly.
- `T2` History row mapper degrades gracefully when hero or screenshot preview
  data is missing.
- `T3` History row mapper exposes recency in a user-facing way when date/time
  information is available.
- `T4` History state preserves a clear selectable-row affordance independent of
  missing secondary data.

## Integration Tests

- `IT1` Multi-record history renders rows where hero, result, and recency are
  visible on each row without opening detail.
- `IT2` Record with missing hero remains readable and selectable.
- `IT3` Record with missing screenshot linkage remains readable and selectable
  without implying record corruption.
- `IT4` Empty history state remains clear and actionable.
- `IT5` History-load failure remains visible and understandable.

## What Not To Test

- Search, filtering, or sort customization.
- Record editing from the history list.
- Room query implementation details.

## Coverage Map

- Key scan fields covered by `T1`, `IT1`
- Missing secondary data handling covered by `T2`, `IT2`, `IT3`
- Recency communication covered by `T3`, `IT1`
- Selection clarity covered by `T4`, `IT2`, `IT3`
- Empty/error states covered by `IT4`, `IT5`

## Not Directly Testable

- Measured scan speed improvements. Use explicit row-content visibility and
  selection clarity as the proxy.
