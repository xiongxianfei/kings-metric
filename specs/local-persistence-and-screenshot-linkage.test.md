# Local Persistence And Screenshot Linkage Test Spec

## Scope

This test spec covers save validation, local record persistence, screenshot linkage preservation, and record-write failure handling.

## Unit Tests

- `T1` Save validation passes when all required fields are valid.
- `T2` Save validation passes when only optional fields are empty.
- `T3` Save validation fails when any required field is unresolved.
- `T4` Missing screenshot linkage invalidates final save.

## Integration Tests

- `IT1` Confirmed valid draft saves a local record with screenshot linkage.
- `IT2` Save attempt with unresolved required field is blocked and no record is written.
- `IT3` Save attempt with only optional unresolved fields succeeds.
- `IT4` Record-write failure reports failure and does not mark the record saved.
- `IT5` Screenshot remains available after record persistence failure.

## Edge Case Coverage

- Optional empty fields covered by `T2`, `IT3`
- Missing screenshot linkage covered by `T4`
- Record-write failure covered by `IT4`, `IT5`

## What Not To Test

- OCR recognition quality
- Review UI layout
- Cloud sync

## Coverage Map

- Save confirmed record locally covered by `IT1`
- Preserve screenshot linkage covered by `IT1`
- Block unresolved required fields covered by `T3`, `IT2`
- Allow optional unresolved fields covered by `T2`, `IT3`
- Report persistence failure clearly covered by `IT4`
