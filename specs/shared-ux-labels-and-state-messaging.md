# Shared UX Labels And State Messaging Spec

## Goal and Context

Define the shared user-facing labels and state messaging rules used across the
Android app so import, review, history, dashboard, and detail screens stop
exposing internal field keys or inconsistent technical wording.

This spec covers shared presentation language, not business-rule changes.

## Concrete Examples

### Example 1: Review Screen Field Labels

Input:
- Review screen renders extracted fields for a supported draft.

Expected behavior:
- The screen shows user-facing field labels.
- The screen MUST NOT show raw enum-style identifiers such as `DAMAGE_SHARE`
  or `PARTICIPATION_RATE` as the main user-visible label.

### Example 2: Import Failure Messaging

Input:
- User selects an unsupported screenshot.

Expected behavior:
- The app shows a clear retry-oriented message that explains the screenshot is
  unsupported.
- The app MUST NOT expose internal exception-like wording or implementation
  jargon.

### Example 3: Empty History And Dashboard States

Input:
- The user has no saved records.

Expected behavior:
- History and dashboard screens use consistent empty-state wording style.
- The wording explains the user situation and next useful action where
  appropriate.

## Requirements

- The app MUST provide user-facing labels for the structured match fields shown
  in review, history, dashboard, and detail screens.
- The import screen MUST use user-facing action labels and state messages rather
  than technical or implementation-oriented wording.
- The app MUST use shared wording conventions across screens for common states
  such as empty, loading, retryable failure, review required, blocked save, and
  successful save.
- The app MUST NOT use raw enum key names as the primary user-visible label.
- The app MUST keep required vs optional and blocking vs non-blocking meanings
  clear in user-facing copy.
- The app MUST keep repeated state messages and field labels consistent through
  shared mapping or another centralized source of truth.
- A single build MUST NOT mix user-facing copy styles in a way that makes the
  app feel partially technical and partially user-oriented for the same kind of
  state.
- The app MUST preserve existing business semantics while changing the wording.

## Interface Expectations

- User-facing messages must describe the current situation in plain language.
- Repeated labels for the same field must remain consistent across screens.
- Shared copy may vary by screen context, but the meaning MUST remain
  consistent.

## Error-State Expectations

- Unsupported screenshot messaging MUST tell the user the screenshot cannot be
  used and that retrying with another screenshot is the next step.
- Save failure messaging MUST distinguish between blocked review state and true
  local persistence failure.
- Missing screenshot preview messaging MUST explain that field data is still
  available.

## Edge Cases

- One screen still uses a raw internal field identifier while others use
  user-facing labels.
- Empty state wording differs across history and dashboard for the same
  "no saved data" condition.
- A blocker message becomes too vague and no longer explains what the user must
  fix.

## Non-Goals

- Changing OCR, parsing, or validation rules.
- Full localization platform design beyond the current v1 UX wording needs.
- Marketing copy or brand voice work unrelated to clarity.

## Acceptance Criteria

- User-facing screens no longer rely on raw field-key identifiers as primary
  labels.
- Import, review, history, dashboard, and detail use consistent messaging style
  for shared states.
- Copy changes do not change any review, validation, or persistence rules.

## Gotchas

- 2026-04-14: Keep raw workflow and repository error strings stable in the
  business layer, then translate them through the shared UX copy mapping at
  the Android runtime boundary. That keeps wording changes from mutating
  business semantics while still preventing screen-to-screen copy drift.
