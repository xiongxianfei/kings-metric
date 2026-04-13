# Unsupported Case And Failure Handling Spec

## Goal and Context

Define the first-release user-facing failure handling for unsupported screenshots, blocked saves, storage failures, and retryable recovery paths.

This spec defines Feature 8 from `docs/plan.md` and refines the failure behaviors already referenced by the import flow.

## Concrete Examples

### Example 1: Unsupported Screenshot

Input:
- User selects a screenshot from the wrong game screen.

Expected behavior:
- The app clearly explains that the image does not match the supported personal-stats template.
- The user can retry with another screenshot.

### Example 2: Save Blocked By Missing Required Field

Input:
- Review draft still has one unresolved required field.

Expected behavior:
- Save is blocked.
- The unresolved field remains visible and actionable.

### Example 3: Local Storage Failure

Input:
- Screenshot copy or final record write fails.

Expected behavior:
- The app reports what category of save failed.
- The user stays in a consistent retryable state.

## Requirements

- Unsupported screenshots MUST report that they do not match the supported personal-stats template.
- The system MUST provide a clean retry path after unsupported screenshot rejection.
- Save validation failures MUST keep the current draft and user edits visible.
- Screenshot storage failure MUST stop downstream processing and report the failure.
- Record persistence failure MUST report that the record was not saved.
- Failure handling MUST NOT silently discard valid stored screenshots or valid user edits unless a future spec explicitly defines that cleanup.
- User-visible failure messages MUST emphasize what action the user can take next when a retry is possible.

## Error-State Expectations

- Unsupported screenshot: retry import.
- Missing required field: remain in review and correct fields.
- Screenshot storage failure: retry intake.
- Record persistence failure: retry save without losing valid review data.

## Edge Cases

- Wrong screenshot screen.
- Correct screen but unsupported language variant.
- Record save fails after long review edits.
- Screenshot storage succeeds but record persistence fails.

## Non-Goals

- Fully localized error-copy system.
- Automated remediation or silent recovery.
- Background retry queues.

## Acceptance Criteria

- Unsupported screenshots are rejected clearly with retry path.
- Blocked saves preserve review context.
- Storage and persistence failures are distinct and understandable.
- Failure handling does not silently destroy user work.

## Gotchas

- None yet.
