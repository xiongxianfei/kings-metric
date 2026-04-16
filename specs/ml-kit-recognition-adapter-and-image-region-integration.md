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
- 2026-04-15: Avoid a redundant full-size `BitmapFactory.decodeFile()` just to
  confirm the stored screenshot exists before ML Kit runs. Large screenshots can
  crash the import path on real devices if the adapter decodes the image twice.
  Use a bounds-only decode to validate readability, then let the recognizer own
  the real image load.
- 2026-04-15: The Android recognition adapter must fail closed on unexpected
  OCR-mapper/runtime exceptions, not only explicit ML Kit failures. Novel or
  partially supported screenshot text can still trigger parser or mapper
  exceptions; those cases must surface as a retryable import failure instead of
  crashing the import flow.
- 2026-04-15: If the adapter already has OCR text when a mapper-stage failure
  happens, preserve that OCR text on the returned import failure so diagnostics
  can explain what ML Kit actually read instead of collapsing to only a generic
  failure summary.
- 2026-04-15: Real supported screenshots can flatten ML Kit text in a
  cross-column order that does not match the on-screen card layout. For
  required first-value metrics such as damage dealt, avoid broad full-text
  scans that can skip the intended value or steal a nearby one. Keep those
  extractions label-local, then use wider last-occurrence scans only for
  metrics whose values intentionally trail their labels in the OCR stream.
- 2026-04-15: Real-device OCR for the supported screenshot can still produce
  clean readable labels such as `对英雄輸出`, `输出占比`, `经济占比`, and `参团率`,
  but some of those labels may be split from their numeric values across later
  OCR lines. Keep canonical readable Chinese labels in the mapper, and use a
  later bounded scan for trailing metrics such as `打野经济`, `补刀数`, `控制时长`,
  and `对塔伤害` instead of assuming the value sits on the same line as the
  label.
- 2026-04-15: Real-device and share-sized supported screenshots can produce
  traditional-Chinese or truncated label variants such as `对英雄输出`,
  `对英雄輸出`, truncated `对英雄出`, or abbreviated `团率` even though the
  template is still the same supported screenshot. Keep alias handling narrow
  to the supported template, but prefer reaching a reviewable draft with
  highlighted missing fields over rejecting the whole screenshot when
  section-level evidence is still present.
- 2026-04-15: Real readable-Chinese OCR can also place the share percentage on
  the same line as the section label such as `输出伤害 35.3%` or `承伤 20.3%`
  even when the explicit share label line is not nearby. For supported
  screenshots, treat those section-line percentages as a valid fallback for
  `DAMAGE_SHARE` and `DAMAGE_TAKEN_SHARE` instead of collapsing to a generic
  recognition failure.
- 2026-04-15: Do not let one helper-level parser exception abort the whole
  supported-template analysis. If anchors and other visible values still prove
  the screenshot is on the supported path, degrade to a reviewable partial
  draft with unresolved fields instead of collapsing into a generic
  recognition failure.
- 2026-04-16: Do not infer the player lane from any whole-text substring hit.
  Real OCR dumps can contain metric labels such as `鎵撻噹缁忔祹`, which would
  falsely classify the match as jungle if the lane detector scans the whole OCR
  text. Lane extraction must prefer summary or badge-local context and treat
  degraded badge variants such as `鍙戞湁璺?as the canonical marksman lane
  instead of letting nearby metric labels win.
