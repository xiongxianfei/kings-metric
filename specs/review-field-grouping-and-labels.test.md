# Review Field Grouping And Labels Test Spec

## Scope

This test spec covers review-screen section grouping, user-facing labels,
required vs optional semantics, blocking vs non-blocking summaries, and blocked
field discoverability.

## Unit Tests

- `T1` Review screen-state mapper assigns every editable field to a named review
  section rather than an undifferentiated flat list.
- `T1a` Review screen-state mapper uses a stable field-to-section mapping for
  the supported review fields.
- `T2` Review state exposes user-facing labels for every review field.
- `T3` Review state marks required vs optional fields distinctly.
- `T4` Review state marks blocking vs non-blocking review items distinctly.
- `T5` Blocker summary state identifies unresolved required fields and the
  fields or sections the user must visit next.
- `T6` Grouping and labels do not change underlying field editability or save
  validation outcomes.

## Integration Tests

- `IT1` Clean draft review renders logical labeled sections rather than one flat
  unlabeled list.
- `IT2` Review with multiple blockers shows a visible blocked-save summary and
  makes the blocked fields easy to locate.
- `IT3` Review with only optional unresolved fields keeps those fields visible
  for follow-up without presenting them as save blockers.
- `IT4` Missing screenshot preview still leaves grouped field data fully usable.
- `IT5` All fields remain editable after grouping and label changes.

## What Not To Test

- OCR extraction logic.
- Save persistence threading or Room behavior.
- Advanced visual styling choices outside grouping and labeling.

## Coverage Map

- Grouping and user-facing labels covered by `T1`, `T1a`, `T2`, `IT1`
- Required vs optional and blocking vs non-blocking semantics covered by `T3`,
  `T4`, `IT2`, `IT3`
- Blocker discoverability covered by `T5`, `IT2`
- Validation preservation covered by `T6`, `IT5`
- Missing-preview fallback covered by `IT4`

## Not Directly Testable

- Visual taste beyond stable grouping, visible labels, and explicit blocker
  semantics.
