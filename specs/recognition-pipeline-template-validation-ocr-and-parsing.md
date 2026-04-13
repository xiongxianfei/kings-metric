# Recognition Pipeline Template Validation OCR And Parsing Spec

## Goal and Context

Define the first-release recognition pipeline that validates one supported Chinese post-match personal-stats screenshot, runs on-device text extraction, maps visible values into the fixed v1 field set, and produces a structured draft with unresolved fields flagged for review.

This spec refines Feature 3 from `docs/plan.md` and inherits the record contract from `specs/personal-stats-screenshot-import.md`.

## Concrete Examples

### Example 1: Supported Screenshot With Clean OCR

Input:
- Stored screenshot matches the supported template and all required fields are readable.

Expected behavior:
- The system validates the screenshot as supported.
- The system extracts visible values into the v1 field set.
- The system produces a reviewable draft with all required fields resolved.

### Example 2: Supported Screenshot With Ambiguous Numeric OCR

Input:
- Stored screenshot matches the template, but one optional numeric field is low confidence.

Expected behavior:
- The screenshot remains supported.
- The ambiguous field is flagged for review.
- The draft remains reviewable when required fields are still valid.

### Example 3: Unsupported Layout Or Language

Input:
- Stored screenshot is cropped, missing anchors, or uses an unsupported language variant.

Expected behavior:
- The system rejects the screenshot before creating a saveable draft.
- The rejection explains that the screenshot does not match the supported template.

## Requirements

- The system MUST validate required anchors before a screenshot is treated as supported.
- The system MUST reject screenshots whose anchors do not satisfy the supported template definition.
- The system MUST reject unsupported language or regional variants.
- The system MUST NOT coerce unsupported screenshots into partial supported records.
- The system MUST use only values visible on the screenshot.
- The system MUST distinguish required fields from optional fields.
- The system MUST preserve displayed values when the spec requires exact preservation, including score order and lane text.
- The system MUST preserve direct labeled values for fields such as `kill_participation_count` and `damage_dealt_to_opponents`.
- The system MUST create a draft record for supported screenshots even when one or more optional fields are unresolved.
- The system MUST mark missing, invalid, or low-confidence fields for review.
- OCR and parsing MUST remain on-device.
- The pipeline MUST NOT invent values that are not explicitly visible, except for allowed format normalization.
- The pipeline MUST NOT broaden template support beyond the approved v1 screenshot layout.

## Error-State Expectations

- Unsupported screenshots MUST produce a clear template-mismatch result.
- Supported screenshots with complete OCR failure SHOULD return an extraction failure result rather than a misleading draft full of invented values.
- Supported screenshots with unresolved required fields MUST still produce a reviewable draft when the screenshot is otherwise supported.

## Edge Cases

- OCR confuses `0` and `O` or `1` and `I`.
- One optional field is blurred while required anchors remain visible.
- Correct screen is partially cropped and loses one required stat section.
- Screenshot uses a similar non-Chinese language variant.

## Non-Goals

- Support for additional screenshot templates.
- Server-side OCR or cloud recognition fallback.
- Derivation of hidden stats from visible stats.

## Acceptance Criteria

- Supported screenshots produce reviewable drafts with required fields resolved when readable.
- Optional unreadable fields remain empty and flagged for review.
- Unsupported screenshots are rejected before saveable output.
- Required unresolved fields remain flagged for later manual correction.

## Gotchas

- None yet.
