# Personal Stats Screenshot Import Spec

## Goal and Context

Define the first-release behavior for importing one supported Honor of Kings post-match personal-stats screenshot, extracting a fixed set of stats, letting the user review the extracted record, and saving both the original screenshot and the confirmed structured data locally.

This spec covers one screenshot template only. It does not cover other result screens, matchup comparison screens, or multi-screenshot merge flows.

## Concrete Examples

### Example 1: Supported Screenshot, Clean Extraction

Input:
- User selects a screenshot that shows the post-match result header, hero/player summary card, selected `数据` tab, and the visible personal stat sections.

Expected behavior:
- The system accepts the screenshot as supported.
- The system creates a draft record with extracted values for all required fields.
- The review screen shows the original screenshot and the parsed fields.
- The user confirms without edits.
- The system saves the original screenshot and the confirmed match record locally.

### Example 2: Supported Screenshot, Partial OCR Failure

Input:
- User selects a screenshot matching the supported template, but OCR misreads one numeric field such as `补刀数`.

Expected behavior:
- The system accepts the screenshot as supported.
- The system marks the unreadable or low-confidence field as needing review.
- The review screen highlights that field and allows the user to correct it.
- The user corrects the field and confirms.
- The system saves the corrected match record and the original screenshot locally.

### Example 3: Unsupported Screenshot Layout

Input:
- User selects a screenshot from a different game screen or a cropped image that does not show the required anchors.

Expected behavior:
- The system rejects the screenshot before creating a saveable record.
- The system explains that the image does not match the supported personal-stats screen.
- The user can retry with another screenshot.

### Example 4: Supported Screenshot, Missing Optional Fields

Input:
- User selects a supported screenshot where one optional stat is obscured or unreadable, but required anchors and required fields are present.

Expected behavior:
- The system accepts the screenshot.
- The system creates a draft record with the missing optional field left empty and flagged for review.
- The user may save the record after reviewing required fields.

## Supported Interface

### User Input

- The user MUST be able to select one screenshot from local device storage.

### Supported Screenshot Definition

The screenshot MUST be treated as supported only when all of the following anchors are present:

- a visible match result header
- a hero and player summary card
- the selected `数据` tab
- visible personal stat sections for at least damage, damage taken, economy, and team participation

The system MUST reject screenshots that do not satisfy the supported screenshot definition.

## Normalized Record Fields

### Required Fields

- `result`
  Source: result header
  Type: enum
  Allowed values: `victory`, `defeat`
- `hero`
  Source: hero summary card
  Type: string
  Validity: MUST be a readable hero label, not a numeric-only placeholder token
- `player_name`
  Source: hero/player summary card
  Type: string
- `lane`
  Source: lane label shown in the summary area
  Type: string
- `score`
  Source: top score line
  Type: string in `team_kills-opponent_kills` form
- `kda`
  Source: summary area
  Type: string in `kills/deaths/assists` form
- `damage_dealt`
  Source: damage section
  Type: numeric value
- `damage_share`
  Source: damage section
  Type: percentage
- `damage_taken`
  Source: damage taken section
  Type: numeric value
- `damage_taken_share`
  Source: damage taken section
  Type: percentage
- `total_gold`
  Source: economy section
  Type: numeric value
- `gold_share`
  Source: economy section
  Type: percentage
- `participation_rate`
  Source: team participation section
  Type: percentage

### Optional Fields

- `gold_from_farming`
  Source: economy section
  Type: numeric value
- `last_hits`
  Source: economy section
  Type: integer
- `kill_participation_count`
  Source: team participation section
  Type: numeric value
- `control_duration`
  Source: team participation section
  Type: duration
- `damage_dealt_to_opponents`
  Source: damage section
  Type: numeric value

### Field Semantics

- `score` MUST represent the team score text shown on the screenshot and MUST preserve left-to-right order as displayed.
- `lane` MUST preserve the lane text shown on the screenshot.
- `kill_participation_count` MUST represent the numeric count shown next to the participation-related label in the team section, not a derived value.
- `damage_dealt_to_opponents` MUST represent the direct damage-to-opponents field shown in the damage section, not total output damage if the UI distinguishes them.

## Requirements

### Import and Validation

- The system MUST store the original screenshot locally when import begins.
- The system MUST validate the screenshot against the supported template before presenting a saveable draft.
- The system MUST reject unsupported screenshots with a clear reason.
- The system MUST NOT silently coerce an unsupported screenshot into a supported record.

### Extraction and Draft Creation

- The system MUST create a draft record from a supported screenshot before final save.
- The system MUST populate the draft using only fields visible on the supported screenshot.
- The system MUST distinguish required fields from optional fields.
- The system MUST mark fields as missing or ambiguous when extraction is incomplete or unreliable.
- The system MUST NOT invent or derive values that are not explicitly visible on the screenshot, except for normalization of format.
- The system MUST treat unreadable placeholder values for required fields, such
  as a numeric-only `hero` token, as unresolved rather than saveable values.

### Review and Editing

- The system MUST show the original screenshot during review.
- The system MUST allow the user to edit extracted fields before save.
- The system MUST highlight fields that are missing, invalid, or low-confidence.
- The system MUST require user confirmation before saving a draft as a final record.

### Saving

- The system MUST save the confirmed structured record locally.
- The system MUST save the original screenshot locally alongside the record.
- The system MUST allow saving a reviewed partial record when all required fields are valid and one or more optional fields remain empty.
- The system MUST NOT save a final record when one or more required fields are missing and unresolved.
- The system MUST NOT save a final record when a required field contains an
  unreadable placeholder value, such as a numeric-only `hero`.

### Reprocessing

- The system SHOULD retain enough linkage between the saved record and the original screenshot for future re-checking by the user.
- Automatic reprocessing of previously saved screenshots after parser updates is out of scope for this release.

## Error-State Expectations

- For unsupported screenshots, the system MUST show that the image does not match the supported personal-stats template.
- For supported screenshots with extraction failures, the system MUST create a reviewable draft when required fields can still be validated.
- For supported screenshots where required fields cannot be validated, the system MUST prevent final save until the user corrects the missing or invalid fields.
- For local storage failure, the system MUST inform the user that the screenshot or record could not be saved.

## Edge Cases

- The screenshot is from the correct screen but one stat row is partially cropped.
- The screenshot matches the template, but OCR confuses similar numeric characters such as `0` and `O` or `1` and `I`.
- The screenshot contains the required anchors but one optional field is hidden by compression artifacts or blur.
- The screenshot shows the correct layout but uses a different unsupported language or regional variant.
- The screenshot is supported and valid, but the user changes extracted values during review before saving.
- The screenshot or user edit produces a numeric-only placeholder in the
  required `hero` field.

## Non-Goals

- Support for any screenshot other than the one personal-stats template defined in this spec.
- Extraction of fields not visible on the supported screenshot.
- Automatic reprocessing of already saved records after extraction logic changes.
- Cloud backup, account sync, or server-side OCR.

## Acceptance Criteria

- A supported screenshot can be imported, reviewed, and saved as one local record with its original image attached.
- An unsupported screenshot is rejected with a clear explanation and no final record is created.
- A supported screenshot with one unreadable optional field can still be saved after review.
- A supported screenshot with a missing required field cannot be saved until the user resolves it.
- A numeric-only placeholder in the required `hero` field is treated as
  unresolved and blocks save until corrected.
- Review always shows the original screenshot and the extracted fields together.

## Gotchas

- None yet.
