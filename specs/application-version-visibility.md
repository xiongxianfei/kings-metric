# Application Version Visibility

## Goal and Context

Expose the currently installed Kings Metric build version inside the app so a
user can report exactly which APK they are running without adb, GitHub, or
external release notes.

This spec follows the concrete plan:
[2026-04-15-application-version-visibility](../docs/plans/2026-04-15-application-version-visibility.md)

The first version-visibility surface is intentionally small. It is not a full
settings/about feature or an in-app changelog.

## Concrete Examples

### Example 1: User Opens Diagnostics

Input:
- The user opens the existing in-app `Diagnostics` screen.

Expected behavior:
- The screen shows a clearly labeled current app version value.
- The value is readable without exporting diagnostics first.

Example:
```text
Current Version
0.1.0-alpha.8
```

### Example 2: User Copies Diagnostics

Input:
- The user opens `Diagnostics`.
- The user taps `Copy Diagnostics`.

Expected behavior:
- The copied diagnostics export still includes the app version.
- The visible version on screen and the exported version agree.

### Example 3: New APK Installed Over Old One

Input:
- The user upgrades from one prerelease APK to a newer prerelease APK.

Expected behavior:
- The app shows the new installed version after launch.
- The app does not continue showing the old tracked value from a stale cached
  UI state.

## Inputs and Outputs

### Inputs

- Installed Android app package metadata for the running build
- User opening the Diagnostics screen
- User exporting diagnostics

### Outputs

- A visible current-version field in the app
- Matching version information in the diagnostics export

## Requirements

### R1. User-Reachable Version Visibility

- The app MUST expose the current version in a user-reachable in-app surface.
- For v1 of this feature, that surface MUST be the `Diagnostics` screen.
- The version field MUST be visible without requiring the user to trigger an
  import, recognition, or save failure first.

### R2. Runtime Source Of Truth

- The displayed version MUST come from the installed app runtime metadata.
- The app MUST NOT hardcode a separate UI-only version string.
- The diagnostics screen and diagnostics export MUST use the same app-version
  source of truth.

### R3. Readable Presentation

- The Diagnostics screen MUST label the value clearly, such as `Current
  Version`.
- The displayed value MUST be readable and copy-safe for user bug reports.
- The version field SHOULD appear near the top-level diagnostics context rather
  than being buried inside failure-specific entries.

### R4. Diagnostics Alignment

- If the diagnostics export includes an app version field, it MUST match the
  version shown in the Diagnostics screen.
- The app MUST keep the visible version available even when the diagnostics
  entry list is empty.

### R5. Failure Independence

- If version lookup fails unexpectedly, the Diagnostics screen MUST remain
  usable.
- The app MUST degrade safely with a readable fallback such as `Unknown` rather
  than crashing the Diagnostics screen.

## Invariants

- Version visibility remains local-only.
- The feature does not fetch release data from the network.
- Visible version info does not widen product scope into changelog browsing,
  settings screens, or update prompts.

## Error Handling and Boundary Behavior

- Empty diagnostics history MUST still show the current app version.
- Export failure MUST remain a retryable export problem and MUST NOT hide or
  clear the visible version field.
- Activity recreation or normal recomposition MUST NOT duplicate, remove, or
  stale-cache the visible version value.

## Compatibility Expectations

- The feature must work for both debug and release builds.
- The displayed version should reflect the installed APK metadata for whichever
  build variant the user is currently running.

## Observability Expectations

- Support staff should be able to confirm the user-reported app version from:
  - the visible Diagnostics screen
  - the copied diagnostics export

## Edge Cases

- User opens Diagnostics before any diagnostics entry exists.
- User exports diagnostics when the entry list is empty or very small.
- User upgrades from one prerelease APK to another and reopens Diagnostics.
- Runtime version lookup fails and the screen must show a safe fallback.

## Non-Goals

- Full About screen
- In-app release notes or changelog viewer
- Network-based release lookup
- Store update prompts
- Device/account identifiers

## Acceptance Criteria

- A user can open Diagnostics and read the current app version immediately.
- The copied diagnostics export includes the same app version value.
- The visible version matches the installed build metadata.
- Empty-state and export-failure paths preserve visible version info.
