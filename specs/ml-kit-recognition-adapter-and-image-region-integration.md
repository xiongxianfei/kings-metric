# ML Kit Recognition Adapter And Image Region Integration Spec

## Goal and Context

Define the Android-side adapter that turns a stored screenshot file into the existing pure recognition inputs using ML Kit Text Recognition v2 and template-specific image region handling.

This spec refines the recognition pipeline feature for real on-device OCR integration.

## Concrete Examples

### Example 1: Supported Stored Screenshot

Input:
- App has a locally stored supported screenshot file.

Expected behavior:
- The adapter loads the bitmap, extracts the required regions, runs ML Kit on-device, and returns the existing structured analysis needed by the parser.

### Example 2: OCR Engine Returns Incomplete Numeric Text

Input:
- One required numeric region is low-confidence or unreadable.

Expected behavior:
- The adapter returns unresolved or low-confidence field information rather than inventing a confident value.

## Requirements

- The app MUST use ML Kit Text Recognition v2 on-device only.
- The adapter MUST accept app-managed local screenshot files as input.
- The adapter MUST keep template validation separate from persistence and UI concerns.
- The adapter MUST map ML Kit output into the existing supported-template field set only.
- The adapter MUST preserve low-confidence and unreadable-field information for downstream review.
- The adapter MUST reject unsupported screenshots before producing a misleading partial supported draft.
- The adapter SHOULD keep Android image loading and ML Kit invocation behind an interface that the pure workflow layer can consume.

## Error-State Expectations

- If the image file cannot be decoded, the adapter MUST report recognition failure clearly.
- If OCR fails entirely, the adapter MUST return an import failure and MUST NOT fabricate draft values.

## Edge Cases

- Bitmap decode failure.
- Cropped screenshot missing one required section.
- Low-confidence numeric OCR ambiguity.

## Non-Goals

- Cloud OCR fallback.
- Multi-template recognition.
- Automatic fixture generation from production screenshots.

## Acceptance Criteria

- Stored screenshots can be processed by an Android ML Kit adapter into the current recognition pipeline.
- Low-confidence and unsupported cases remain explicit.
- Android OCR wiring does not change the pure parser contract.

## Gotchas

- 2026-04-14: Synthetic generated text images were not enough to validate the
  real adapter. The live ML Kit output from the supported screenshot fixture
  reordered some labels and values, inserted punctuation such as `:`, and mixed
  simplified and traditional Chinese variants for the same label. The Android
  mapper must be verified against a real supported screenshot fixture, not only
  synthetic text-image tests.
