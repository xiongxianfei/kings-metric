# Compose Review Screen And ViewModel Integration Test Spec

## Scope

This test spec covers review screen state exposure, field editing, screenshot preview handling, save blocking, and save-failure retention behavior.

## Unit Tests

- `T1` Review `ViewModel` exposes screenshot path, field state, and confirm availability from a draft.
- `T2` Editing a field updates state and clears resolved blocking status when valid.
- `T3` Save validation failure keeps current edits in `ViewModel` state.

## Integration Tests

- `IT1` Compose review screen shows highlighted required unresolved fields.
- `IT2` Optional unresolved fields remain highlighted but do not block confirm.
- `IT3` Missing screenshot preview shows unavailable-preview UI while field data remains visible.
- `IT4` Save failure leaves the user on review with edits intact.

## What Not To Test

- OCR region extraction.
- Dashboard calculations.

## Coverage Map

- `ViewModel` state mapping covered by `T1`, `T2`, `T3`
- Review rendering and failure behavior covered by `IT1` to `IT4`
