# Diagnostics Scope And Privacy Contract Test Spec

## Scope

This test spec covers the contract for local-first diagnostics capture,
privacy-safe export, bounded retention, and in-app user access on the
supported alpha flow.

## Unit Tests

- `T1` Diagnostics configuration is local-only and does not require upload,
  account, or server participation.
- `T2` Diagnostics event model distinguishes supported-path failures,
  unsupported screenshots, save outcomes, and successful save.
- `T3` Diagnostics event formatter produces bounded structured entries with
  timestamp, stage, outcome category, and user-visible summary.
- `T4` Export redaction excludes screenshot payloads, raw full OCR text, full
  saved-record payloads, and unrelated secret material.
- `T5` Retention policy keeps a deterministic bounded recent set when the
  buffer limit is exceeded.
- `T6` Diagnostics export failure is modeled as retryable and does not require
  the main app flow to terminate.
- `T7` Empty diagnostics state is explicit and export-disabled or clearly
  non-actionable according to the feature contract.

## Integration Tests

- `IT1` Import failure on the supported path records a diagnostics entry while
  preserving the existing retryable import UI behavior.
- `IT2` Unsupported screenshot and supported recognition failure produce
  distinguishable diagnostics entries.
- `IT3` Save failure after review records a diagnostics entry that is
  distinguishable from import/recognition failure and leaves review usable.
- `IT4` Successful save records a success-category diagnostics entry without
  exposing full saved-record payload content.
- `IT5` Diagnostics viewer shows recent entries in readable language and keeps
  empty state explicit when no entries exist.
- `IT6` Exported diagnostics artifact excludes screenshot binaries and raw full
  OCR dumps while still including enough bounded context for support.
- `IT7` If diagnostics capture/export throws internally, the main user-visible
  failure path remains usable and the app does not terminate.

## What Not To Test

- Remote upload behavior, because the feature explicitly forbids it.
- OCR/template expansion or hero-recognition improvements.
- Full release workflow publication logic beyond diagnostics-specific support
  contract checks.

## Coverage Map

- `R1` covered by `T1`
- `R2` covered by `T2`, `IT1`, `IT2`, `IT3`, `IT4`
- `R3` covered by `T2`, `T3`, `IT2`
- `R4` covered by `T4`, `IT6`
- `R5` covered by `T5`
- `R6` covered by `T6`, `T7`, `IT5`, `IT6`
- `R7` covered by `T6`, `IT1`, `IT3`, `IT7`
- `R8` covered by future docs/support verification tied to milestone 4; no code
  test yet because the support docs are not implemented in this milestone

## Not Directly Testable

- Overall user-perceived usefulness of the exported diagnostics beyond the
  concrete contract that the artifact contains bounded stage/outcome context and
  excludes the named sensitive payloads.

## Gaps To Resolve During Implementation

- `R8` will need doc/release-contract coverage once the README/support guidance
  milestone is implemented.
