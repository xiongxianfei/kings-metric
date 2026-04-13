# Recognition Pipeline Template Validation OCR And Parsing Test Spec

## Scope

This test spec covers supported-template validation, OCR result mapping, field parsing, unresolved-field flagging, and draft creation for the v1 recognition pipeline.

## Unit Tests

- `T1` Accept supported screenshot with all required anchors.
- `T2` Reject screenshot missing result header.
- `T3` Reject screenshot missing selected data tab.
- `T4` Reject screenshot missing one required stat section.
- `T5` Reject unsupported language or regional variant.
- `T6` Normalize visible winning result into `victory`.
- `T7` Preserve score left-to-right exactly as displayed.
- `T8` Preserve lane text exactly as displayed.
- `T9` Parse KDA only in `kills/deaths/assists` form.
- `T10` Preserve displayed `kill_participation_count`.
- `T11` Preserve displayed `damage_dealt_to_opponents`.
- `T12` Leave non-visible fields empty instead of deriving them.
- `T13` Create a draft for a supported screenshot.
- `T14` Mark optional unreadable field as missing.
- `T15` Mark required unreadable field unresolved.
- `T16` Mark ambiguous OCR field as low-confidence.

## Integration Tests

- `IT1` Supported screenshot produces a reviewable draft with required fields populated.
- `IT2` Supported screenshot with optional low-confidence field still proceeds to review.
- `IT3` Supported screenshot with unresolved required field still creates a draft for correction.
- `IT4` Unsupported layout is rejected before saveable output.
- `IT5` Unsupported language variant is rejected before saveable output.

## Edge Case Coverage

- Numeric OCR confusion covered by `T16`, `IT2`
- Optional blurred field covered by `T14`, `IT2`
- Cropped required section covered by `T4`, `IT4`
- Unsupported language covered by `T5`, `IT5`

## What Not To Test

- Room persistence behavior
- Review UI layout details
- Cloud or remote OCR behavior

## Coverage Map

- Validation of supported template covered by `T1` to `T5`
- Use of only visible values covered by `T12`
- Required vs optional distinction covered by `T14`, `T15`
- Missing/invalid/low-confidence flagging covered by `T14` to `T16`
- Early rejection of unsupported screenshots covered by `IT4`, `IT5`
