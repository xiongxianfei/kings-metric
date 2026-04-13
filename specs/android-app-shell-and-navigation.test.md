# Android App Shell And Navigation Test Spec

## Scope

This test spec covers Android app startup, navigation graph wiring, safe argument handling, and post-save navigation behavior.

## Unit Tests

- `T1` Navigation route definitions cover import, review, history, dashboard, and detail destinations.
- `T2` Navigation reducer or coordinator maps save success to the expected destination.
- `T3` Missing required route arguments resolve to a safe fallback instruction.

## Integration Tests

- `IT1` App launch shows the root destination without crashing.
- `IT2` User can navigate from import to review and then to the post-save destination.
- `IT3` Opening detail with a missing record id returns to a safe destination with an error state.
- `IT4` Launch with empty local data still allows navigation to dashboard and history empty states.

## What Not To Test

- OCR correctness.
- Room query semantics.
- Dashboard metric calculations.

## Coverage Map

- Root shell and graph wiring covered by `T1`, `IT1`
- Post-save navigation covered by `T2`, `IT2`
- Safe fallback behavior covered by `T3`, `IT3`
