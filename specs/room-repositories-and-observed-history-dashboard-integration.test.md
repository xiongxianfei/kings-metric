# Room Repositories And Observed History Dashboard Integration Test Spec

## Scope

This test spec covers Room persistence integration, repository mapping, observable history/dashboard refresh, and not-found behavior.

## Unit Tests

- `T1` Repository maps Room entities into history/detail/dashboard domain models.
- `T2` Missing record lookup maps to a safe not-found result.
- `T3` Save failure maps to a repository error result without false success.

## Integration Tests

- `IT1` Confirmed save persists a Room record with screenshot linkage.
- `IT2` History observer emits updated data after save.
- `IT3` Dashboard observer emits updated aggregate inputs after save.
- `IT4` Read failure surfaces an error result.
- `IT5` Detail lookup for missing record returns a safe not-found state.

## What Not To Test

- ML Kit OCR behavior.
- Navigation transitions.

## Coverage Map

- Repository mapping covered by `T1`
- Safe not-found and failure behavior covered by `T2`, `T3`, `IT4`, `IT5`
- Observable refresh behavior covered by `IT1`, `IT2`, `IT3`
