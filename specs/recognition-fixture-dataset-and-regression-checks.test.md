# Recognition Fixture Dataset And Regression Checks Test Spec

## Scope

This test spec covers the required fixture categories, expected outcomes, and regression assertions for validator and parser stability.

## Unit Tests

- `T1` Fixture catalog includes one supported full fixture.
- `T2` Fixture catalog includes one supported optional-missing fixture.
- `T3` Fixture catalog includes one supported required-missing fixture.
- `T4` Fixture catalog includes one unsupported fixture.
- `T5` Fixture catalog includes one low-confidence fixture.
- `T6` Supported full fixture remains accepted with expected parsed output.
- `T7` Optional-missing fixture remains accepted with expected missing optional field.
- `T8` Required-missing fixture remains accepted as draftable but unresolved.
- `T9` Unsupported fixture remains rejected.
- `T10` Low-confidence fixture remains flagged for review.

## Integration Tests

- `IT1` Regression suite reports which fixture changed when expected outcomes drift.
- `IT2` Validator change that affects acceptance or rejection behavior fails the suite.
- `IT3` Parser change that alters expected field mapping fails the suite.

## Edge Case Coverage

- Optional unreadable field covered by `T7`
- Required unreadable field covered by `T8`
- Unsupported cropped screen covered by `T9`
- Low-confidence numeric ambiguity covered by `T10`

## What Not To Test

- Large-scale OCR accuracy benchmarking
- Unsupported template expansion
- Automatic expectation regeneration

## Coverage Map

- Required fixture categories covered by `T1` to `T5`
- Stable acceptance and rejection outcomes covered by `T6` to `T10`
- Clear regression failure behavior covered by `IT1` to `IT3`
