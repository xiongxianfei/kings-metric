# Match History UI Test Spec

## Scope

This test spec covers loading local match history, empty state behavior, record detail display, and missing linked screenshot handling.

## Unit Tests

- `T1` History state exposes records in stable recent-first order.
- `T2` Empty history state is explicit.
- `T3` Detail state includes saved fields and screenshot reference when available.
- `T4` Detail state degrades gracefully when screenshot file is missing.

## Integration Tests

- `IT1` Saved records appear in the history list.
- `IT2` Empty local storage shows the empty state.
- `IT3` User opens one record from history and sees saved fields.
- `IT4` User opens one record with a missing screenshot file and sees a non-crashing unavailable-image state.
- `IT5` History load failure shows recoverable error state.

## Edge Case Coverage

- Empty history covered by `T2`, `IT2`
- Missing screenshot file covered by `T4`, `IT4`
- History load failure covered by `IT5`

## What Not To Test

- Metrics dashboard calculations
- Cloud-backed history
- Search or filtering features

## Coverage Map

- Show local list covered by `IT1`
- Explicit empty state covered by `T2`, `IT2`
- Open saved record detail covered by `IT3`
- Handle missing screenshot without crash covered by `T4`, `IT4`
