# Android Project Bootstrap And Build Setup Spec

## Goal and Context

Define the work required to turn the repository into a real Android
application project while preserving the current pure Kotlin logic and
tests.

This spec is the entry gate for all other Android integration work.

## Concrete Examples

### Example 1: Clean Checkout

Input:
- A developer clones the repository and opens it in Android Studio.

Expected behavior:
- The project syncs as an Android app project.
- The app module is discoverable and buildable.

### Example 2: Basic Launch

Input:
- Developer runs the debug app on an emulator.

Expected behavior:
- The app installs and launches into `MainActivity`.
- The app shows a stable placeholder shell instead of crashing.

## Requirements

- The repository MUST define an Android application module.
- The build MUST apply Android and Kotlin plugins required for a Compose app.
- The project MUST include an `AndroidManifest.xml`, application id,
  namespace, SDK versions, and debug/release build types.
- The project MUST add dependencies for Compose, Navigation Compose,
  Hilt, Room, DataStore, and ML Kit.
- The bootstrap MUST preserve the existing JVM-testable pure logic source
  set rather than rewriting it into Android-only code.
- The app MUST define `Application` and `MainActivity` entry points.
- The build SHOULD remain compatible with local Gradle and CI execution.

## Error-State Expectations

- Gradle sync failures due to missing Android configuration MUST be treated
  as blockers.
- Bootstrap work MUST NOT silently break the existing unit-test target.

## Edge Cases

- Existing JVM tests coexist with Android module configuration.
- Android Studio sync succeeds but app launch fails due to missing manifest
  wiring.

## Non-Goals

- Final UI polish.
- Full feature completion in this step.
- Emulator CI setup.

## Acceptance Criteria

- The repo builds as an Android app project.
- A debug app installs and launches.
- Existing pure logic remains reusable from Android code.
