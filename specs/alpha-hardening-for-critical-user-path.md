# Alpha Hardening For Critical User Path Spec

## Goal and Context

Harden the supported alpha user journey so early GitHub users can reliably
complete the existing core flow:

`import screenshot -> review extracted data -> save locally -> confirm in history`

This feature is release-facing runtime hardening, not a scope-expansion
feature. It exists to remove dead ends, misleading failures, and fragile
state loss on the current supported path before the first GitHub prerelease is
treated as usable by outside testers.

## Concrete Examples

### Example 1: Supported Screenshot Needs Manual Hero Entry

Input:
- The user imports a supported screenshot.
- OCR extracts the supported fields, but hero is still unresolved.

Expected behavior:
- The app reaches a usable review state instead of leaving the user stuck on
  the import screen.
- The review screen makes it clear that hero still requires user attention.
- The user can enter the missing hero manually and continue to save.

### Example 2: Local Save Fails After Review

Input:
- The user finishes review and taps save.
- Local persistence fails.

Expected behavior:
- The app shows an actionable save failure instead of silently losing work.
- Reviewed field edits remain intact.
- The user can retry from the same review session after the failure.

### Example 3: User Returns After Temporary Interruption

Input:
- The user is in the supported import or review flow.
- The activity is recreated or the app is temporarily interrupted.

Expected behavior:
- The app preserves enough in-progress state for the user to continue or
  clearly retry.
- The app does not silently reset to an unrelated screen while leaving the
  user unsure whether progress was saved.

### Example 4: Unsupported Screenshot Is Imported

Input:
- The user imports a screenshot outside the supported template or language.

Expected behavior:
- The app shows a clear unsupported result on the import path.
- The app does not create a misleading partial saved record.
- The user can start another import attempt without stale review state
  blocking the flow.

## Requirements

### Critical Path Continuity

- The supported alpha path MUST always end in one of these explicit outcomes:
  - review-ready
  - blocked-for-review with actionable required fields
  - unsupported screenshot
  - actionable local failure with a retry path
- The app MUST NOT leave the user on a dead-end screen after a successful
  screenshot intake on the supported path.
- The app MUST preserve the current supported scope rather than widening OCR
  or template support as part of this hardening feature.

### Review And Save Resilience

- If required fields remain unresolved, the app MUST keep the user in a usable
  review state and MUST make the blocking fields visible in user-facing terms.
- If local save fails after review, the app MUST preserve the current reviewed
  data and MUST keep the user on a retryable review path.
- The app MUST NOT report a successful local save before the reviewed record
  actually reaches the local persistence path.
- If save succeeds, the app MUST clear stale in-progress review/import state so
  the user does not return to a misleading old draft by accident.

### Recovery And State Ownership

- If the app is interrupted during the supported import or review path, it
  MUST preserve or explicitly recover enough user progress to avoid silent data
  loss.
- If full continuation is not possible for a specific failure, the app MUST
  return the user to a clear retryable state instead of an ambiguous blank or
  stale screen.
- The app MUST NOT imply that a record was saved if interruption happened
  before confirmed local persistence.

### User-Facing Failure Behavior

- User-facing release-path failures SHOULD use action-oriented, non-technical
  wording rather than raw implementation details.
- Unsupported screenshot feedback MUST remain distinct from local save failure
  feedback.
- The supported alpha path SHOULD make the next user action obvious after a
  failure, such as retry import, continue review, or retry save.

## Interface Expectations

- On the supported alpha path, the user should always be able to tell:
  - whether the screenshot was accepted
  - whether review is ready or still blocked
  - whether local save succeeded or failed
  - what the next action is after a failure
- History should reflect a successful save without requiring the user to guess
  whether the record actually persisted.

## Error-State Expectations

- If screenshot intake succeeds but recognition cannot produce a usable
  supported draft, the user MUST receive a clear import-path result rather than
  a silent no-op.
- If local save fails, the app MUST NOT discard the user’s reviewed edits.
- If activity recreation happens mid-flow, the app MUST NOT resume in a way
  that falsely implies completion of the critical path.

## Edge Cases

- The user imports a supported screenshot, reaches review, and save fails on
  the first attempt.
- The user imports a supported screenshot, leaves the app, and returns before
  save succeeds.
- The user imports an unsupported screenshot immediately after a previous
  successful review-ready draft existed.
- The user successfully saves a record and then re-enters the shell flow.
- Hero remains unresolved, but all other supported fields are ready.

## Non-Goals

- Improving hero extraction reliability itself.
- Adding support for new templates, non-Chinese screenshots, or server-side
  recognition.
- Defining the device-verification release gate or GitHub publication logic.
- Broad UI redesign outside what is required to keep the critical path usable.

## Acceptance Criteria

- A supported screenshot can reliably reach a usable review state in the alpha
  app without leaving the user stuck.
- Manual hero completion remains possible when hero is unresolved, and that
  state is presented as review-required rather than as a hidden failure.
- Save failure keeps reviewed data intact and retryable.
- Save success clears stale draft state and leaves the saved record visible
  through the normal history path.
- Temporary interruption does not silently lose progress or falsely imply that
  save completed.
- Unsupported screenshots remain clearly rejected without contaminating the
  supported save flow.

## Gotchas

- Alpha hardening is not permission to widen product scope. If the current
  supported OCR path still cannot resolve hero automatically, preserve manual
  review instead of guessing.
- 2026-04-15: If the shell relies on a saveable draft for recovery, do not let
  review edits live only inside the route-scoped `ViewModel`. Mirror edited
  drafts back to the shell owner as the user types, or interruption recovery
  will restore stale pre-edit data.
- 2026-04-15: Do not clear the shell-owned review draft from inside the review
  `ViewModel` success path before the shell handles navigation. Emit save
  success first, then let the shell clear stale draft state after its own
  success handling begins.
