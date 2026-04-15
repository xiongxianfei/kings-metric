# Hero Extraction Reliability Test Spec

## Scope

This test spec covers supported-template hero resolution, unresolved-hero
fallback behavior, and regression safety around unsupported screenshots and
non-hero fields.

## Unit Tests

- `T1` Supported-template hero mapping resolves `hero` when a reliable hero
  signal is present in the recognition input.
- `T2` Hero mapping leaves `hero` unresolved when the supported screenshot does
  not provide a reliable hero signal.
- `T3` Resolved hero removes `hero` from the blocking review state while
  preserving the rest of the draft.
- `T4` Unresolved hero keeps `hero` in the blocking review state while still
  allowing a reviewable draft if the other required fields are valid.
- `T5` Hero-reliability changes do not alter existing non-hero required field
  behavior.

## Integration Tests

- `IT1` Real supported screenshot fixture produces a draft where `hero` is
  populated and not review-blocking.
- `IT2` Supported screenshot or synthetic supported-analysis case with missing
  hero signal still produces a reviewable draft and keeps `hero` blocked for
  manual correction.
- `IT3` Unsupported cropped or unsupported-language screenshot remains rejected
  after the hero-reliability change.
- `IT4` Supported screenshot that resolves `hero` but still has another
  unresolved required field continues to block save for that other field only.

## What Not To Test

- New template support.
- Cloud OCR fallback.
- Hero suggestions derived from saved history or dashboard data.

## Coverage Map

- Reliable hero resolution covered by `T1`, `T3`, `IT1`
- Safe unresolved-hero fallback covered by `T2`, `T4`, `IT2`
- Non-hero behavior preservation covered by `T5`, `IT4`
- Supported-template safety covered by `IT3`

## Not Directly Testable

- "Meaningfully better" hero extraction quality is represented here by the
  real supported screenshot fixture no longer blocking on `hero`, not by a
  broad OCR accuracy benchmark.
