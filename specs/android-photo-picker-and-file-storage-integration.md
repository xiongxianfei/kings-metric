# Android Photo Picker And File Storage Integration Spec

## Goal and Context

Define the real Android intake path that replaces the current fake screenshot source with Photo Picker selection plus app-managed file persistence.

This spec refines the existing screenshot-import-local-file-intake feature for Android framework integration.

## Concrete Examples

### Example 1: User Selects One Screenshot

Input:
- User taps import and picks one image from Android Photo Picker.

Expected behavior:
- The app copies the selected image into app-managed storage.
- The import workflow receives a stable local reference for downstream recognition.

### Example 2: Picker Cancelled

Input:
- User opens Photo Picker and cancels.

Expected behavior:
- No import starts.
- The app stays in a safe idle state.

## Requirements

- The app MUST use `ActivityResultContracts.PickVisualMedia` for the v1 screenshot selection path.
- The app MUST support selecting one image per import attempt.
- The app MUST copy the chosen image into app-managed local storage before recognition continues.
- The app MUST assign and preserve a stable screenshot identifier for the copied file.
- The app MUST retain enough metadata to keep record-to-screenshot linkage stable after save.
- The app MUST surface picker cancellation as a non-error idle result.
- The app MUST surface unreadable-source and local-copy failures distinctly.
- The app SHOULD isolate Android `Uri` handling from the pure import workflow types.

## Error-State Expectations

- If the selected `Uri` cannot be opened, the app MUST report an unreadable-source failure.
- If local file copy fails, the app MUST report a storage failure and MUST NOT continue recognition.

## Edge Cases

- Picker returns no selection.
- Picker returns a readable `Uri` but local copy fails.
- Source file metadata is incomplete.

## Non-Goals

- Camera capture.
- Multi-select import.
- Duplicate detection policy changes.

## Acceptance Criteria

- Android selection uses Photo Picker.
- Imported screenshots are copied into app-managed storage before recognition.
- Cancellation and storage errors are handled distinctly.

## Gotchas

- 2026-04-14: On real Android, `ContentResolver.openInputStream()` may throw
  for a missing `file://` `Uri` instead of returning `null`. Treat that as an
  unreadable-source failure and cover it with an instrumented test rather than
  assuming JVM/fake storage behavior matches device behavior.
- 2026-04-14: A successful picker import must leave the user with an explicit
  way to enter review. Do not rely only on an automatic navigation side effect
  after the activity result returns; keep a visible continue/review action when
  a draft is already ready on the import screen.
