# ML Kit Recognition Adapter And Image Region Integration Test Spec

## Scope

This test spec covers Android bitmap loading, ML Kit adapter behavior, template-region mapping, and propagation of unresolved recognition results.

## Unit Tests

- `T1` Region-mapping logic requests the expected field regions for the supported template.
- `T2` Low-confidence OCR output is preserved as review-required field state.
- `T3` Unsupported template detection blocks supported-draft creation.

## Integration Tests

- `IT1` Stored screenshot file is decoded and passed through the ML Kit adapter.
- `IT2` OCR failure returns a clear import failure.
- `IT3` Supported screenshot with one unreadable required field still produces a draftable unresolved result.
- `IT4` Unsupported cropped screenshot remains rejected.

## What Not To Test

- Room persistence.
- Navigation graph behavior.

## Coverage Map

- Region selection and mapping covered by `T1`
- Low-confidence propagation covered by `T2`, `IT3`
- Rejection and OCR failure paths covered by `T3`, `IT2`, `IT4`
