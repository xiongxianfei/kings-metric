# Android Photo Picker And File Storage Integration Test Spec

## Scope

This test spec covers the Android Photo Picker contract, `Uri` intake behavior, file copy integration, and failure handling.

## Unit Tests

- `T1` Picker result mapper converts a selected `Uri` into an intake request.
- `T2` Cancelled picker result maps to a non-error idle outcome.
- `T3` Storage adapter maps unreadable `Uri` access to unreadable-source failure.
- `T4` Storage adapter maps copy failure to local-storage failure.

## Integration Tests

- `IT1` Selecting one image copies it into app-managed storage.
- `IT2` Cancelling picker leaves import state idle.
- `IT3` Unreadable `Uri` reports import failure without partial downstream work.
- `IT4` Copy failure reports storage failure and does not start recognition.

## What Not To Test

- OCR extraction.
- Review UI field editing.

## Coverage Map

- Picker contract covered by `T1`, `T2`, `IT2`
- File-copy integration covered by `T3`, `T4`, `IT1`, `IT3`, `IT4`
