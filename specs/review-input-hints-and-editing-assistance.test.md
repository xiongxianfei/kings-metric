# Review Input Hints And Editing Assistance Test Spec

## Scope

This test spec covers review-screen editing hints, format guidance, input
affordances, unresolved-field prioritization, and edit retention when save is
blocked or fails.

## Unit Tests

- `T1` Review field metadata exposes concise format guidance for ambiguous
  editable fields.
- `T2` Review field metadata chooses the correct input affordance category for
  supported field types where the platform supports it.
- `T3` Review state prioritizes unresolved required fields ahead of optional
  clean-up without changing save semantics.
- `T4` Invalid or wrong-format user input remains in state so the user can
  correct it instead of losing edits.
- `T5` Optional highlighted fields are never labeled or surfaced as required
  blockers.

## Integration Tests

- `IT1` Ambiguous percentage-like or rate-like field shows concise expected
  format guidance during manual editing.
- `IT2` Numeric count field uses an appropriate numeric-friendly input
  configuration.
- `IT3` Review with multiple unresolved fields makes required fixes easier to
  prioritize than optional clean-up.
- `IT4` Wrong-format edit leaves the user on review with the entered value
  intact and the blocking guidance still visible.
- `IT5` Save failure retains the user's edits and current review context.

## What Not To Test

- Automatic correction beyond current normalization rules.
- Heavy custom input frameworks.
- OCR confidence thresholds.

## Coverage Map

- Format guidance covered by `T1`, `IT1`
- Input affordances covered by `T2`, `IT2`
- Required-vs-optional prioritization covered by `T3`, `T5`, `IT3`
- Edit retention on failure covered by `T4`, `IT4`, `IT5`

## Not Directly Testable

- Subjective perceptions of "helpful" hints beyond concise field-specific
  guidance, input-category choice, and preserved save semantics.
