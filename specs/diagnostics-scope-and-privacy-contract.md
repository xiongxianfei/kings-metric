# Diagnostics Scope And Privacy Contract

## Goal and Context

Define the first in-app diagnostics contract for the shipped Android alpha so a
real user can collect a bounded, privacy-safe support artifact when the
supported flow fails.

This spec follows the concrete plan:
[2026-04-15-user-diagnostics-and-error-logs](../docs/plans/2026-04-15-user-diagnostics-and-error-logs.md)

The diagnostics feature is local-first. It is not remote telemetry, automatic
crash upload, or a replacement for the app's existing user-visible error
messages.

## Concrete Examples

### Example 1: Import Fails On A Supported Screenshot

Input:
- The user imports a screenshot.
- Recognition fails or the app cannot produce a usable supported draft.

Expected behavior:
- The app still shows the normal retryable import failure.
- The app records a diagnostics entry describing the stage, outcome category,
  and a user-shareable summary.
- The diagnostics export does not include the screenshot itself or a raw OCR
  dump.

### Example 2: Save Fails After Review

Input:
- The user reaches review.
- Local save fails.

Expected behavior:
- The user remains on the retryable review path.
- Diagnostics capture that save failed after review, including the failure
  category and time.
- The exported diagnostics are sufficient to distinguish this from an import or
  unsupported-screenshot problem.

### Example 3: User Opens Diagnostics Screen

Input:
- The user opens the in-app diagnostics surface after a failure.

Expected behavior:
- The screen shows recent bounded diagnostics entries in readable language.
- The user can export the diagnostics artifact without adb or logcat.
- The user can see what the export does and does not include.

### Example 4: Sensitive Data Must Stay Out Of The Export

Input:
- The app has recently processed a screenshot and extracted review data.
- The user exports diagnostics.

Expected behavior:
- The export excludes the image binary, raw full OCR text, and full saved-match
  payloads.
- The export may include privacy-bounded identifiers or summaries only when
  needed to explain the failure category and stage.

## Inputs and Outputs

### Inputs

- Runtime failures and state transitions on the supported alpha path:
  - import intake
  - recognition
  - review/save
  - history/detail/dashboard load when relevant to a visible user problem
- User action to open diagnostics and export the current diagnostics artifact

### Outputs

- A bounded local diagnostics record set
- A user-shareable diagnostics export artifact produced on demand
- User-visible diagnostics copy that explains what is included and excluded

## Requirements

### R1. Local-Only Diagnostics Ownership

- Diagnostics MUST be captured and stored locally on the device.
- Diagnostics MUST NOT be uploaded automatically to any remote service.
- Diagnostics MUST NOT require account sign-in, server infrastructure, or
  cloud sync.

### R2. Supported-Path Coverage

- Diagnostics MUST cover failures and key outcomes on the current supported
  alpha path:
  - import failure
  - unsupported screenshot
  - recognition failure
  - blocked review state when relevant to the user-visible outcome
  - save failure
  - successful save
- Diagnostics SHOULD cover user-visible history/detail/dashboard load failures
  when they block normal browsing of saved data.

### R3. Structured Event Shape

- Each diagnostics entry MUST record, at minimum:
  - timestamp
  - stage or surface
  - outcome category
  - user-visible summary
- Diagnostics entries MUST use bounded structured fields rather than raw
  logcat lines as the primary contract.
- Diagnostics entries MUST distinguish unsupported-screenshot outcomes from
  supported-path processing failures.

### R4. Privacy And Redaction

- Diagnostics export MUST NOT include:
  - screenshot image binaries
  - raw full OCR text output
  - full saved record payloads
  - secret values such as signing keys, tokens, or local absolute paths not
    needed for support
- Diagnostics MAY include privacy-bounded summaries or identifiers only when
  they help explain stage, outcome category, or app/build context.
- The diagnostics UI and export flow MUST clearly state that the export is
  limited and does not include the original screenshot.

### R5. Bounded Retention

- Diagnostics retention MUST be bounded so local records do not grow without
  limit.
- When older diagnostics are evicted, the app MUST keep the most recent
  entries according to a deterministic retention rule.
- Export MUST only include the bounded current diagnostics set, not hidden
  historical overflow.

### R6. User-Visible Access And Export

- The user MUST be able to access diagnostics from inside the app without adb.
- The user MUST be able to export diagnostics on demand.
- The export flow MUST use readable, support-oriented wording rather than raw
  internal exception text alone.
- If export fails, the app MUST show a retryable, user-visible export failure.

### R7. Failure-Path Independence

- Diagnostics capture MUST NOT replace or hide the normal user-facing failure
  state on the import, review, or save path.
- If diagnostics capture itself fails, the original app flow MUST still follow
  the existing feature spec for that user-visible failure.
- Diagnostics failure MUST degrade safely rather than causing app termination
  on the supported path.

### R8. Release/Support Contract

- README or equivalent support guidance MUST explain how a user can collect and
  share diagnostics for support.
- Future release/support verification MUST treat broken diagnostics export or
  privacy regression as a supportability issue rather than a hidden internal
  defect.

## Interface Expectations

- The diagnostics surface should always let the user tell:
  - what happened
  - when it happened
  - which stage failed
  - how to export/share the diagnostics artifact
  - that the screenshot itself is not included

## Error-State Expectations

- If diagnostics export cannot be created, the app MUST keep the diagnostics
  viewer usable and show a retryable export failure.
- If no diagnostics entries exist yet, the diagnostics surface MUST present an
  explicit empty state rather than a blank screen.
- If the app records both unsupported and supported-path failures, the
  diagnostics surface MUST keep those categories distinguishable.

## Edge Cases

- The user imports an unsupported screenshot and then a supported screenshot
  that fails during recognition.
- The user reaches review, save fails, and then diagnostics export is invoked.
- The diagnostics buffer reaches its retention limit and a new failure is
  recorded.
- The user opens diagnostics before any failure has occurred.
- Diagnostics capture throws internally while the main user flow is already
  handling a failure.

## Non-Goals

- Automatic telemetry or crash upload.
- Server-side log aggregation or analytics.
- Exporting the original screenshot for support by default.
- Expanding OCR/template/language support.
- Replacing normal user-facing failure messages with a diagnostics-only flow.

## Acceptance Criteria

- A real alpha user can collect a bounded diagnostics artifact from inside the
  app without adb.
- The export is useful for support while remaining local-first and
  privacy-bounded.
- Unsupported screenshot, recognition failure, review/save failure, and
  successful save are distinguishable in diagnostics.
- Diagnostics do not include screenshot binaries or raw full OCR dumps.
- Diagnostics failure does not crash the supported app flow.
