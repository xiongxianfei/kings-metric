# Android Test Harness And Release Readiness Spec

## Goal and Context

Define the Android-specific verification bar required before the
repository can claim a runnable app target has been achieved.

This spec covers the test harness and release-blocking checks that sit on
top of the feature specs.

## Concrete Examples

### Example 1: Feature PR Touches Room And Compose

Input:
- A pull request changes persistence and UI bindings.

Expected behavior:
- The relevant JVM, instrumented, and Compose tests run in CI.

### Example 2: Candidate Internal Release

Input:
- Team wants to cut an internal APK build.

Expected behavior:
- Required Android tests and assembly tasks have already passed.

## Requirements

- The repository MUST define separate verification for JVM tests,
  Android instrumented tests, and Compose UI tests once the Android app
  module exists.
- CI MUST be able to assemble the debug app.
- CI SHOULD be able to run emulator-backed tests for critical flows.
- Release readiness MUST require passing verification for:
  - import intake and storage
  - recognition integration
  - review/save flow
  - Room persistence
  - history and dashboard rendering
- The harness MUST make failures attributable to a specific layer when
  possible.

## Error-State Expectations

- If emulator tests are unavailable in one environment, that limitation
  MUST be stated clearly rather than treated as green by omission.
- A release candidate MUST NOT be treated as ready if critical Android
  verification is skipped.

## Edge Cases

- JVM tests pass but emulator tests fail.
- Debug build assembles but instrumented tests regress.
- CI environment lacks emulator capacity temporarily.

## Non-Goals

- Play Store release automation.
- Crash analytics rollout.
- Performance benchmarking.

## Acceptance Criteria

- The repo has an explicit Android test matrix.
- CI can validate runnable-app readiness rather than only JVM logic.
- Skipped Android verification is visible and actionable.
