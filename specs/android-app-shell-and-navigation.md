# Android App Shell And Navigation Spec

## Goal and Context

Define the minimum Android app shell needed to wire the existing import, history, and dashboard logic into a real Jetpack Compose application.

This spec refines the existing v1 feature set for actual Android delivery without changing the current business rules.

## Concrete Examples

### Example 1: Cold App Launch

Input:
- User opens the app for the first time.

Expected behavior:
- The app launches into a stable root screen.
- Navigation destinations for import, history, dashboard, and record detail are available.

### Example 2: Save Completes From Review Flow

Input:
- User confirms a valid reviewed draft and the save succeeds.

Expected behavior:
- The app leaves the review flow and lands in a safe post-save destination defined by navigation state.

## Requirements

- The app MUST provide a Compose root with Navigation Compose.
- The app MUST define explicit routes for import, review, history, dashboard, and saved-record detail.
- The app MUST keep navigation state outside composables that only render leaf UI.
- The app MUST preserve the existing v1 flow order: import -> review -> save -> history or confirmation destination.
- The app MUST use Hilt-based dependency injection for Android wiring.
- The app MUST allow deep-link-free in-app navigation for the first release.
- The app SHOULD keep route arguments explicit and type-safe at the app boundary.

## Error-State Expectations

- If a destination cannot load its required state, the app MUST return to a safe screen and surface an error message.
- Navigation failures MUST NOT leave the user on a blank screen or crash the app.

## Edge Cases

- App launch with no saved records.
- Review destination opened without a valid draft state.
- Detail destination requested with a missing record id.

## Non-Goals

- External deep links.
- Multi-activity architecture.
- Tablet-specific navigation patterns.

## Acceptance Criteria

- A single Android app shell can reach import, review, history, dashboard, and detail screens.
- Navigation failures degrade to a safe state.
- Android wiring does not move business rules into composables.

## Gotchas

- 2026-04-14: Pure JVM coordinator tests were not enough once the shell moved
  to a real `NavHost`. Keep Android Compose coverage for review-without-draft,
  detail-without-record-id, and post-save navigation because runtime route
  state can fail even when the pure coordinator still passes.
- 2026-04-14: Treat Hilt plugin application failures such as `Android
  BaseExtension not found` as Android build-wiring/toolchain problems first.
  Verify the app shell with at least `:app:assembleDebug` in addition to the
  pure app-shell test suite.
- 2026-04-14: In the real review route, changing in-memory draft state in the
  same callback as a post-save navigation can race the destination change.
  Keep the navigation path and the route-state cleanup order covered by an
  Android test.
- 2026-04-14: Review-route draft progress should not live only in plain
  `remember` state. Persist it with `rememberSaveable`, `SavedStateHandle`, or
  another explicit shell-owned state mechanism, and cover the save/restore
  codec with a deterministic test.
