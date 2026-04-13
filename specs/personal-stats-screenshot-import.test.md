# Personal Stats Screenshot Import Test Spec

## Scope

This test spec covers the first-release flow for importing one supported post-match personal-stats screenshot, creating a draft, reviewing extracted fields, and saving the confirmed record plus original screenshot locally.

## Test Data Fixtures

- `fixture_supported_full`
  A supported screenshot with all required and optional fields visible and readable.
- `fixture_supported_optional_missing`
  A supported screenshot with one optional field obscured.
- `fixture_supported_required_missing`
  A supported screenshot with one required field unreadable or absent.
- `fixture_unsupported_wrong_screen`
  A screenshot from a different game page.
- `fixture_unsupported_cropped`
  A cropped screenshot missing one or more required anchors.
- `fixture_supported_low_confidence_numeric`
  A supported screenshot where one numeric field is OCR-ambiguous.
- `fixture_supported_other_language`
  A screenshot with similar layout but an unsupported language or regional variant.
- `fixture_storage_failure`
  A supported screenshot used in flows where local screenshot or record persistence fails.

## Unit Tests

### Template Validation

- `T1` Accept screenshot when all required anchors are present.
  Input: `fixture_supported_full`
  Assert: validator returns supported.

- `T2` Reject screenshot when result header anchor is missing.
  Input: `fixture_unsupported_cropped`
  Assert: validator returns unsupported with a rejection reason.

- `T3` Reject screenshot when selected data-tab anchor is missing.
  Input: derived fixture missing the selected data tab.
  Assert: validator returns unsupported with a rejection reason.

- `T4` Reject screenshot when required stat sections are not all visible.
  Input: derived fixture missing one required stat section.
  Assert: validator returns unsupported with a rejection reason.

- `T5` Reject screenshot from unsupported language or regional variant when anchor matching does not satisfy the supported template.
  Input: `fixture_supported_other_language`
  Assert: validator returns unsupported.

### Field Normalization and Parsing

- `T6` Normalize `result` into `victory` for a supported winning screenshot.
  Input: OCR/parser output from `fixture_supported_full`
  Assert: parsed `result == victory`.

- `T7` Preserve `score` left-to-right exactly as displayed.
  Input: OCR/parser output where the top line shows a concrete score such as `20-10`
  Assert: parsed `score == 20-10`.

- `T8` Preserve `lane` exactly as displayed on the screenshot.
  Input: OCR/parser output from a screenshot showing a specific lane label.
  Assert: parsed `lane` matches the displayed label without remapping.

- `T9` Parse `kda` only in `kills/deaths/assists` form.
  Input: OCR/parser output with a valid `11/1/5`
  Assert: parsed `kda == 11/1/5`.

- `T10` Distinguish required fields from optional fields in the draft schema.
  Input: parser metadata definition
  Assert: required field set and optional field set match the spec.

- `T11` Mark optional field missing when the screenshot is supported but that field cannot be read.
  Input: `fixture_supported_optional_missing`
  Assert: missing optional field is empty and flagged for review.

- `T12` Mark required field invalid when extraction cannot validate it.
  Input: `fixture_supported_required_missing`
  Assert: required field is flagged unresolved.

- `T13` Do not derive values not explicitly visible on the screenshot.
  Input: screenshot/OCR output lacking one field not shown in the image
  Assert: parser leaves the field empty instead of generating a derived value.

- `T14` Preserve `kill_participation_count` as the displayed numeric field rather than calculating it from other values.
  Input: OCR/parser output containing both participation rate and participation count
  Assert: stored value equals the displayed count text.

- `T15` Preserve `damage_dealt_to_opponents` as the direct labeled field rather than substituting total damage.
  Input: OCR/parser output containing both total damage and damage-to-opponents values
  Assert: stored value equals the direct damage-to-opponents field.

### Draft Status and Review Flags

- `T16` Create a draft record for a supported screenshot before final save.
  Input: `fixture_supported_full`
  Assert: draft object exists and is not marked final.

- `T17` Flag low-confidence OCR fields for review.
  Input: `fixture_supported_low_confidence_numeric`
  Assert: ambiguous field is marked low-confidence.

- `T18` Flag missing fields for review.
  Input: `fixture_supported_optional_missing`
  Assert: missing field is highlighted in draft status metadata.

### Save Validation

- `T19` Allow final save when all required fields are valid and optional fields are complete.
  Input: reviewed draft from `fixture_supported_full`
  Assert: save validation passes.

- `T20` Allow final save when all required fields are valid and one optional field remains empty.
  Input: reviewed draft from `fixture_supported_optional_missing`
  Assert: save validation passes.

- `T21` Reject final save when any required field remains missing or invalid.
  Input: unresolved reviewed draft from `fixture_supported_required_missing`
  Assert: save validation fails with a required-field reason.

### Record Linkage and Reprocessing Metadata

- `T22` Persist linkage between saved record and original screenshot reference.
  Input: saved record from a supported screenshot
  Assert: record contains stable reference to the stored original screenshot.

## Integration Tests

### Import to Draft Flow

- `IT1` Supported screenshot imports into a reviewable draft with all required fields populated.
  Input: `fixture_supported_full`
  Assert:
  - screenshot is stored locally at import start
  - screenshot passes template validation
  - draft is created
  - required fields are populated
  - review state includes the original screenshot and extracted fields

- `IT2` Supported screenshot with low-confidence optional field imports into a reviewable draft and highlights the field.
  Input: `fixture_supported_low_confidence_numeric`
  Assert:
  - screenshot is accepted
  - draft is created
  - ambiguous field is highlighted
  - user can proceed to review instead of being hard-blocked

- `IT3` Unsupported screenshot is rejected before a saveable draft is created.
  Input: `fixture_unsupported_wrong_screen`
  Assert:
  - screenshot fails template validation
  - user sees unsupported-template message
  - no final record is created

### Review and Confirmation Flow

- `IT4` User edits a low-confidence field during review and then confirms successfully.
  Input: draft from `fixture_supported_low_confidence_numeric`
  Steps:
  1. Import screenshot.
  2. Open review.
  3. Edit the flagged field to a valid value.
  4. Confirm save.
  Assert:
  - edited value is persisted
  - final record is saved locally
  - original screenshot remains linked to the record

- `IT5` User attempts to save a draft with unresolved required fields and is blocked.
  Input: draft from `fixture_supported_required_missing`
  Assert:
  - review shows missing required field
  - save action is rejected
  - record is not finalized until the field is corrected

- `IT6` User saves a reviewed partial record with unresolved optional fields.
  Input: draft from `fixture_supported_optional_missing`
  Assert:
  - review highlights the optional missing field
  - save succeeds once required fields are valid
  - saved record preserves the optional field as empty

### Persistence and Failure Handling

- `IT7` Import flow informs the user when local screenshot storage fails at import start.
  Input: `fixture_storage_failure`
  Assert:
  - user sees a save/storage failure message
  - no final record is created

- `IT8` Save flow informs the user when final record persistence fails.
  Input: reviewed draft with simulated record-write failure
  Assert:
  - user sees a save failure message
  - record is not marked saved
  - original screenshot state remains consistent with product decision

- `IT9` Review screen always shows the original screenshot beside or alongside extracted fields.
  Input: any supported draft
  Assert:
  - screenshot reference is available in review UI state
  - extracted fields are shown in the same review flow

## Edge Case Coverage

- `E1` Correct screen but partially cropped stat row.
  Covered by: `T4`, `T12`, `IT5`

- `E2` OCR confuses similar numeric characters.
  Covered by: `T17`, `IT2`, `IT4`

- `E3` Optional field hidden by blur or compression.
  Covered by: `T11`, `T18`, `T20`, `IT6`

- `E4` Unsupported language or regional variant.
  Covered by: `T5`, `IT3`

- `E5` User changes extracted values during review before saving.
  Covered by: `IT4`

## What Not To Test

- Recognition accuracy for unsupported screenshot templates.
- Automatic reprocessing of already saved screenshots after parser updates.
- Cloud sync, account features, or server-side OCR behavior.
- Metrics dashboard calculations outside the import/save flow.

## Coverage Map

### Examples

- Example 1 covered by `T1`, `T16`, `T19`, `IT1`, `IT9`
- Example 2 covered by `T17`, `IT2`, `IT4`
- Example 3 covered by `T2`, `T3`, `T4`, `IT3`
- Example 4 covered by `T11`, `T20`, `IT6`

### Normative Requirements

- User MUST be able to select one screenshot from local storage.
  Covered by: `IT1`, `IT3`

- Screenshot MUST be supported only when all required anchors are present.
  Covered by: `T1`, `T2`, `T3`, `T4`

- System MUST reject screenshots that do not satisfy the supported definition.
  Covered by: `T2`, `T3`, `T4`, `T5`, `IT3`

- `score` MUST preserve left-to-right order.
  Covered by: `T7`

- `lane` MUST preserve displayed lane text.
  Covered by: `T8`

- `kill_participation_count` MUST use displayed numeric field, not derived value.
  Covered by: `T14`

- `damage_dealt_to_opponents` MUST use the direct labeled field.
  Covered by: `T15`

- System MUST store original screenshot locally when import begins.
  Covered by: `IT1`, `IT7`

- System MUST validate screenshot before presenting a saveable draft.
  Covered by: `IT1`, `IT3`

- System MUST reject unsupported screenshots with a clear reason.
  Covered by: `T2`, `T3`, `T4`, `T5`, `IT3`

- System MUST NOT silently coerce unsupported screenshot into a supported record.
  Covered by: `IT3`

- System MUST create draft record from supported screenshot before final save.
  Covered by: `T16`, `IT1`

- System MUST populate draft using only visible fields.
  Covered by: `T13`, `IT1`

- System MUST distinguish required from optional fields.
  Covered by: `T10`

- System MUST mark missing or ambiguous fields.
  Covered by: `T11`, `T12`, `T17`, `T18`, `IT2`, `IT5`

- System MUST NOT invent or derive non-visible values except normalization.
  Covered by: `T13`

- System MUST show original screenshot during review.
  Covered by: `IT1`, `IT9`

- System MUST allow user to edit extracted fields before save.
  Covered by: `IT4`

- System MUST highlight missing, invalid, or low-confidence fields.
  Covered by: `T17`, `T18`, `IT2`, `IT5`, `IT6`

- System MUST require user confirmation before final save.
  Covered by: `IT4`, `IT6`
  Note: this is primarily an integration/UI workflow assertion.

- System MUST save confirmed structured record locally.
  Covered by: `IT4`, `IT6`

- System MUST save original screenshot locally alongside the record.
  Covered by: `IT4`, `T22`

- System MUST allow reviewed partial save when required fields are valid and optional fields are empty.
  Covered by: `T20`, `IT6`

- System MUST NOT save final record when required fields remain unresolved.
  Covered by: `T21`, `IT5`

- System SHOULD retain enough linkage for future re-checking.
  Covered by: `T22`
  Note: “enough linkage” is partially design-dependent; this test assumes a stable screenshot reference on the saved record.

- Unsupported screenshot error MUST mention supported personal-stats template mismatch.
  Covered by: `IT3`

- Supported screenshot with extraction failures MUST create a reviewable draft when required fields can still be validated.
  Covered by: `IT2`, `IT6`

- Supported screenshot with invalid required fields MUST prevent final save until corrected.
  Covered by: `T21`, `IT5`

- Local storage failure MUST inform the user.
  Covered by: `IT7`, `IT8`

## Untestable or Spec-Dependent Areas

- The exact user-facing wording of rejection and storage failure messages is not specified and should be tested by intent rather than exact string match unless the UI spec later freezes copy.
- The spec says review must show the screenshot and extracted fields together, but it does not define exact layout. Tests should verify presence in the same review flow, not visual placement.
