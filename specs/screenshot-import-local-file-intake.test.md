# Screenshot Import Local File Intake Test Spec

## Scope

This test spec covers selecting one screenshot, copying it into app-managed local storage, assigning a stable screenshot reference, and handling local intake failures.

## Unit Tests

- `T1` Accept one selected screenshot input.
- `T2` Treat repeated import of the same screenshot as a new intake attempt.
- `T3` Create a stable screenshot reference after successful copy.
- `T4` Preserve original screenshot content metadata through intake.

## Integration Tests

- `IT1` User selects one screenshot and the app stores it locally.
  Assert:
  - one image is accepted
  - copy to app-managed storage succeeds
  - stable screenshot reference is created
  - downstream processing can receive that reference

- `IT2` Intake stops when source image cannot be read.
- `IT3` Intake stops when local storage write fails.
- `IT4` User cancel during image selection returns to idle import state.

## Edge Case Coverage

- Repeated import attempt covered by `T2`
- Unreadable image source covered by `IT2`
- Local storage failure covered by `IT3`

## What Not To Test

- OCR accuracy
- Template validation rules
- Cloud or sync behavior

## Coverage Map

- User MUST be able to select exactly one screenshot.
  Covered by: `T1`, `IT1`

- Screenshot MUST be copied into app-managed local storage.
  Covered by: `T3`, `IT1`

- System MUST assign a stable local identifier.
  Covered by: `T3`, `IT1`

- System MUST stop when storage fails.
  Covered by: `IT3`

- Duplicate detection is out of scope and repeated imports remain valid attempts.
  Covered by: `T2`
