# Screenshot Import Local File Intake Spec

## Goal and Context

Define the first-release behavior for selecting one screenshot from device storage, copying it into app-managed local storage, assigning a stable local screenshot reference, and surfacing intake failures clearly.

This spec refines Feature 2 from `docs/plan.md` and inherits the shared import contract from `specs/personal-stats-screenshot-import.md`.

## Concrete Examples

### Example 1: User Selects One Valid Image

Input:
- User opens the import flow and selects one image from local device storage.

Expected behavior:
- The system accepts exactly one image.
- The system copies the original image into app-managed local storage.
- The system creates a stable local screenshot reference before downstream validation begins.

### Example 2: Storage Copy Fails

Input:
- User selects an image, but the app cannot copy it into local storage.

Expected behavior:
- The system reports that the screenshot could not be saved locally.
- The system does not proceed into validation, OCR, or review.

### Example 3: Same Screenshot Imported Again

Input:
- User selects the same image file again later.

Expected behavior:
- The system treats the second selection as a new import attempt.
- The system creates a new local screenshot reference unless a future duplicate policy explicitly changes this behavior.

## Requirements

- The user MUST be able to select exactly one screenshot from local device storage.
- The system MUST copy the selected image into app-managed local storage before downstream processing.
- The system MUST preserve the original image content as imported.
- The system MUST assign a stable local identifier to each successfully stored screenshot.
- The system MUST stop the import flow if local screenshot storage fails.
- The system MUST report local screenshot storage failure clearly.
- The system MUST NOT create a saveable draft if the screenshot has not been stored successfully.
- The stored screenshot reference MUST remain usable by later validation, review, and persistence stages.
- The system MUST NOT delete or replace the stored screenshot during intake unless a later spec explicitly defines cleanup behavior.
- Duplicate detection is out of scope for this release; repeated imports MUST remain valid separate attempts.

## Error-State Expectations

- If the user cancels image selection, the system SHOULD return to the import entry state without an error banner.
- If the selected source cannot be read, the system MUST show that the screenshot could not be imported.
- If local storage fails, the system MUST show that the screenshot could not be saved locally and MUST NOT continue.

## Edge Cases

- The selected image is extremely large but still readable.
- The selected source URI becomes unavailable before copy begins.
- The same screenshot is imported more than once.
- The selected file is not a valid readable image.

## Non-Goals

- Duplicate import detection or deduplication policy.
- Cloud backup or remote screenshot storage.
- Batch selection of multiple screenshots.

## Acceptance Criteria

- A user can select one screenshot and the app stores it locally before downstream processing.
- A storage failure blocks the import flow and shows a clear error.
- A successful intake yields a stable screenshot reference for later linkage.

## Gotchas

- None yet.
