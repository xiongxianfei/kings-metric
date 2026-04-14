# History And Dashboard Screen Binding Test Spec

## Scope

This test spec covers Compose screen binding to repository flows for
history, detail, and dashboard behavior.

## Unit Tests

- `T1` Screen state mappers convert repository models into history and
  dashboard UI state.
- `T2` Detail screen state preserves field visibility when screenshot
  preview is unavailable.

## Integration Tests

- `IT1` History screen renders repository-backed saved records.
- `IT2` Dashboard screen renders repository-backed metrics.
- `IT3` Empty repository state shows explicit empty states.
- `IT4` Repository updates refresh visible history and dashboard UI.
- `IT5` Missing screenshot file renders unavailable-preview UI in detail.

## What Not To Test

- OCR extraction.
- Room write transaction behavior.

## Coverage Map

- State mapping covered by `T1`, `T2`
- Screen binding and refresh covered by `IT1` to `IT5`
