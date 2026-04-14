# Honor of Kings Match Tracker Runnable Android Plan

## Objective

Turn this repository into a runnable Android app that can:

- launch on device
- import one supported screenshot
- run on-device recognition
- show review/edit UI
- save the confirmed record plus screenshot locally
- browse history and metrics

v1 scope remains fixed:

- one supported Simplified Chinese personal-stats screenshot template
- on-device only OCR
- local-only persistence
- required user review before final save

## How To Read This Plan

Use this file for:

- current delivery status
- build order
- feature dependencies
- next highest-value gap

Use other docs for detail:

- runtime/system flow: [workflows.md](./workflows.md)
- feature contracts: [specs/](../specs/)

## Status

Status markers:

- `Done`: merged and verified for current scope
- `In Progress`: active branch or open PR
- `Todo`: planned but not finished end-to-end

Current feature status:

1. `Done` Android Project Bootstrap And Build Setup
   Spec: [android-project-bootstrap-and-build-setup.md](../specs/android-project-bootstrap-and-build-setup.md)
2. `In Progress` Android App Shell And Navigation
   Spec: [android-app-shell-and-navigation.md](../specs/android-app-shell-and-navigation.md)
3. `Done` Android Photo Picker And File Storage Integration
   Spec: [android-photo-picker-and-file-storage-integration.md](../specs/android-photo-picker-and-file-storage-integration.md)
4. `Done` ML Kit Recognition Adapter And Image Region Integration
   Spec: [ml-kit-recognition-adapter-and-image-region-integration.md](../specs/ml-kit-recognition-adapter-and-image-region-integration.md)
5. `Done` Compose Review Screen And ViewModel Integration
   Spec: [compose-review-screen-and-viewmodel-integration.md](../specs/compose-review-screen-and-viewmodel-integration.md)
6. `Done` Room Repositories And Observed History Dashboard Integration
   Spec: [room-repositories-and-observed-history-dashboard-integration.md](../specs/room-repositories-and-observed-history-dashboard-integration.md)
7. `Done` History And Dashboard Screen Binding
   Spec: [history-and-dashboard-screen-binding.md](../specs/history-and-dashboard-screen-binding.md)
8. `Done` Android Test Harness And Release Readiness
   Spec: [android-test-harness-and-release-readiness.md](../specs/android-test-harness-and-release-readiness.md)

## Current State

What is real now:

- Android app module, manifest, resources, wrapper, and CI exist
- Room code generation and Android build wiring are fixed
- photo picker file copy path exists
- real ML Kit recognition from stored screenshot into draft generation exists
- automatic import -> recognition -> review handoff exists in the Android runtime
- review save can return the user to history in the runtime flow
- Room-backed history/dashboard runtime exists
- review runtime UI exists and is exercised by Android tests
- emulator-backed Android verification is available and in use

What is still missing for the core user journey:

- merge the Hilt + Navigation Compose shell that replaces the current tab-only root
- harden route-scoped state ownership for review data after navigation and process recreation
- broaden end-to-end navigation and failure-path verification around the real app shell

## Delivery Rules

- keep pure Kotlin business rules as the source of truth
- add Android wiring around those rules instead of rewriting them
- prefer replacing fake adapters with real implementations one slice at a time
- verify generated-code Android features with `:app:assembleDebug`
- verify Android `Uri` and storage behavior on emulator/device early because framework behavior can differ from JVM or fake-adapter assumptions

## Build Order

1. Android Project Bootstrap And Build Setup
2. Android App Shell And Navigation
3. Android Photo Picker And File Storage Integration
4. ML Kit Recognition Adapter And Image Region Integration
5. Compose Review Screen And ViewModel Integration
6. Room Repositories And Observed History Dashboard Integration
7. History And Dashboard Screen Binding
8. Android Test Harness And Release Readiness

## Feature Summary

1. Android bootstrap
   Outcome: app builds and launches
   Spec: [android-project-bootstrap-and-build-setup.md](../specs/android-project-bootstrap-and-build-setup.md)
2. App shell/navigation
   Outcome: app can route safely between import, review, history, dashboard, and detail
   Spec: [android-app-shell-and-navigation.md](../specs/android-app-shell-and-navigation.md)
3. Photo picker/storage
   Outcome: one selected screenshot is copied into app-managed storage with stable linkage
   Spec: [android-photo-picker-and-file-storage-integration.md](../specs/android-photo-picker-and-file-storage-integration.md)
4. Recognition integration
   Outcome: stored screenshot becomes a real draft via ML Kit and template mapping
   Spec: [ml-kit-recognition-adapter-and-image-region-integration.md](../specs/ml-kit-recognition-adapter-and-image-region-integration.md)
5. Review runtime
   Outcome: user can review, edit, and attempt save in real Android UI
   Spec: [compose-review-screen-and-viewmodel-integration.md](../specs/compose-review-screen-and-viewmodel-integration.md)
6. Room repositories
   Outcome: confirmed records persist locally and expose observable data
   Spec: [room-repositories-and-observed-history-dashboard-integration.md](../specs/room-repositories-and-observed-history-dashboard-integration.md)
7. History/dashboard binding
   Outcome: saved records and metrics render from Room-backed flows
   Spec: [history-and-dashboard-screen-binding.md](../specs/history-and-dashboard-screen-binding.md)
8. Android test harness
   Outcome: runnable-app verification is explicit and release-gated
   Spec: [android-test-harness-and-release-readiness.md](../specs/android-test-harness-and-release-readiness.md)

## Dependencies

- 2 depends on 1
- 3 depends on 1 and 2
- 4 depends on 1, 2, and 3
- 5 depends on 1, 2, 3, and 4 for full end-to-end value
- 6 depends on 1 and 5
- 7 depends on 2, 5, and 6
- 8 depends on 1 through 7

## Next Gap

Highest-value remaining product gap:

- finish landing the spec-compliant Hilt + Navigation Compose app shell
- then harden review-route state persistence and fallback behavior in the real runtime
- then expand end-to-end Android verification around import -> review -> save -> history

This means the current implementation focus is Feature 2:

- [android-app-shell-and-navigation.md](../specs/android-app-shell-and-navigation.md)

## Key Risks

- Android framework behavior can differ from fake/JVM assumptions, especially around `Uri`, storage, and image decode failures
- ML Kit output quality may not match current fake adapter assumptions
- Room/entity evolution may reshape some saved-record boundaries
- Compose/navigation wiring can leak business rules into UI if not kept disciplined
- emulator/device verification can expose issues that assembly and JVM tests miss

## Done When

The runnable-app plan is complete when the repository can show all of the following concretely:

- a user can import one supported screenshot on device
- the app performs on-device recognition without cloud fallback
- the user reviews and edits the extracted draft in Android UI
- a confirmed record and screenshot linkage are saved locally
- history and dashboard reflect saved data
- required Android JVM, build, emulator, and UI verification are explicit and passing
