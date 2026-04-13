# Metrics Dashboard Test Spec

## Scope

This test spec covers dashboard metric computation from local saved records, empty state behavior, missing optional data handling, and refresh on record-set changes.

## Unit Tests

- `T1` Compute win rate from saved records.
- `T2` Compute average KDA from saved records that contain valid KDA values.
- `T3` Compute hero usage counts from saved records.
- `T4` Do not fabricate metrics from missing optional inputs.
- `T5` Metric logic is exposed outside UI code and testable as pure logic.

## Integration Tests

- `IT1` Dashboard shows aggregate metrics when saved records exist.
- `IT2` Dashboard shows explicit empty state when no records exist.
- `IT3` Dashboard refreshes after saved record set changes.
- `IT4` One metric can degrade gracefully when required source data is missing while other metrics remain visible.
- `IT5` History load failure shows error state.

## Edge Case Coverage

- Empty history covered by `IT2`
- Single record dataset covered by `IT1`
- Missing optional inputs covered by `T4`, `IT4`
- Source data updates covered by `IT3`

## What Not To Test

- OCR extraction logic
- Cloud analytics
- Predictive recommendations

## Coverage Map

- Compute metrics from local records covered by `T1` to `T3`, `IT1`
- Empty state covered by `IT2`
- No invented metrics from missing inputs covered by `T4`, `IT4`
- Logic kept out of UI covered by `T5`
