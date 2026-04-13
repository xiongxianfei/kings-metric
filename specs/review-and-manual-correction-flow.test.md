# Review And Manual Correction Flow Test Spec

## Scope

This test spec covers review state generation, field highlighting, manual edits, confirmation gating, and preservation of user edits.

## Unit Tests

- `T1` Review state includes screenshot reference and extracted fields.
- `T2` Missing fields are highlighted.
- `T3` Invalid fields are highlighted.
- `T4` Low-confidence fields are highlighted.
- `T5` User edit updates field value in review state.
- `T6` User edit clears unresolved status when the new value is valid.

## Integration Tests

- `IT1` Clean draft can be reviewed and confirmed without edits.
- `IT2` Low-confidence field can be edited and then confirmed.
- `IT3` Missing required field blocks final confirmation.
- `IT4` Missing optional field does not block final confirmation.
- `IT5` Validation failure keeps user edits intact in review state.
- `IT6` Screenshot preview failure does not discard field data.

## Edge Case Coverage

- Edit low-confidence field covered by `IT2`
- Save with optional unresolved field covered by `IT4`
- Save blocked by required unresolved field covered by `IT3`
- Missing screenshot preview covered by `IT6`

## What Not To Test

- OCR extraction behavior
- Room write behavior
- Multi-screenshot comparison

## Coverage Map

- Show screenshot during review covered by `T1`, `IT6`
- Allow editing covered by `T5`, `IT2`
- Highlight missing/invalid/low-confidence covered by `T2` to `T4`
- Require confirmation before final save covered by `IT1` to `IT4`
- Preserve edits on validation failure covered by `IT5`
