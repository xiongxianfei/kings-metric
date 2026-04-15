# User Diagnostics And Error Logs

## Metadata

- Status: completed
- Created: 2026-04-15
- Updated: 2026-04-15
- Owner: Codex
- Related spec(s):
  - [diagnostics-scope-and-privacy-contract](../../specs/diagnostics-scope-and-privacy-contract.md)
  - [diagnostics-scope-and-privacy-contract.test](../../specs/diagnostics-scope-and-privacy-contract.test.md)
- Supersedes / Superseded by: none
- Branch / PR: TBD
- Last verified commands:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.diagnostics.DiagnosticsCaptureTest" --tests "com.kingsmetric.app.AndroidPhotoPickerRuntimeWiringTest" --tests "com.kingsmetric.app.ComposeReviewScreenAndViewModelIntegrationTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.DiagnosticsScreenIntegrationTest" --tests "com.kingsmetric.app.AndroidAppShellNavigationTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.release.ReadmeAndInstallGuidanceTest" --tests "com.kingsmetric.release.DeviceVerificationAndReleaseGateTest" --tests "com.kingsmetric.release.GitHubReleaseExecutionTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DiagnosticsScreenComposeTest,com.kingsmetric.AppShellNavigationComposeTest"`
  - `./gradlew.bat --no-daemon :core:test`
  - `./gradlew.bat --no-daemon :app:assembleDebug`

## Purpose / Big picture

Make the shipped Android alpha supportable when a real user hits a failure the
developer cannot reproduce locally. The app should provide a local-first way to
capture structured diagnostics, let the user review/export that information,
and avoid exposing sensitive screenshot or match data by default.

Success means:

- the app can record enough bounded context to explain failures on the supported
  import -> review -> save path
- the user can export that context from inside the app without adb or logcat
- exported diagnostics stay local-first and privacy-bounded
- release/support documentation tells the user how to provide the exported file

## Context and orientation

- The repository now uses a plan index at [docs/plan.md](/D:/Data/20260413-kings-metric/docs/plan.md)
  and concrete plan files under [docs/plans](/D:/Data/20260413-kings-metric/docs/plans).
- The first alpha release is already published, so diagnostics are a
  post-release supportability feature rather than a pre-release blocker.
- The current app is local-first:
  - Android runtime/UI lives in [app](/D:/Data/20260413-kings-metric/app)
  - shared Kotlin logic lives in [core](/D:/Data/20260413-kings-metric/core)
- Existing runtime-flow and release guidance live in:
  - [docs/workflows.md](/D:/Data/20260413-kings-metric/docs/workflows.md)
  - [README.md](/D:/Data/20260413-kings-metric/README.md)
  - [CHANGELOG.md](/D:/Data/20260413-kings-metric/CHANGELOG.md)
- The app already has critical-path support surfaces where users can hit
  real-world failures:
  - import/photo picker/runtime state in
    [AndroidPhotoPickerRuntime.kt](/D:/Data/20260413-kings-metric/core/src/main/kotlin/com/kingsmetric/app/AndroidPhotoPickerRuntime.kt)
  - recognition in
    [AndroidMlKitTextRecognition.kt](/D:/Data/20260413-kings-metric/app/src/main/java/com/kingsmetric/app/AndroidMlKitTextRecognition.kt)
  - review/save in
    [ReviewScreenIntegration.kt](/D:/Data/20260413-kings-metric/core/src/main/kotlin/com/kingsmetric/app/ReviewScreenIntegration.kt)
    and
    [ReviewScreenRoute.kt](/D:/Data/20260413-kings-metric/app/src/main/java/com/kingsmetric/app/ReviewScreenRoute.kt)
  - app shell/navigation in
    [HistoryDashboardScreens.kt](/D:/Data/20260413-kings-metric/app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt)
- There is no current in-app diagnostics surface, bounded event store, or
  exportable support artifact.

## Constraints

- Keep diagnostics local-first. No automatic upload, remote telemetry, account
  integration, or cloud sync.
- Do not export raw screenshots, OCR text dumps, or full saved match content by
  default.
- Diagnostics must be bounded in size and retention so they do not grow
  without limit.
- Diagnostics must help with real-user failures on the supported scope first:
  import, recognition, review, save, and history/detail/dashboard load.
- Keep business rules out of Compose UI. Diagnostics capture, filtering,
  redaction, and export formatting belong in testable Kotlin classes.
- Preserve the current supported screenshot/product scope. This plan is about
  supportability, not wider OCR/template support.
- Non-goals for this initiative:
  - automatic bug-report upload
  - account-linked support tickets
  - analytics dashboards
  - continuous remote crash collection

## Done when

- The diagnostics/privacy contract is defined by spec and test spec.
- Runtime failures on the supported path can be captured as bounded structured
  diagnostics instead of requiring raw logcat access.
- The app exposes an in-app diagnostics screen or equivalent export path that a
  real alpha user can reach without adb.
- Exported diagnostics exclude sensitive screenshot payloads and clearly label
  what is included.
- README/support guidance explains how a user can collect and share the export
  when reporting a problem.
- Diagnostics behavior has focused JVM coverage plus the smallest necessary
  Android/Compose verification.

## Milestones

### Milestone 1: Diagnostics Scope And Privacy Contract

Scope:
- create the diagnostics feature spec and test spec
- define what events are captured, how long they are retained, what fields are
  redacted/excluded, and what the export artifact looks like

Files/components:
- `specs/diagnostics-scope-and-privacy-contract.md`
- `specs/diagnostics-scope-and-privacy-contract.test.md`
- `docs/workflows.md`
- `README.md` if needed for user-facing privacy wording

Dependencies: none
Risk: medium
Validation commands: none beyond spec review
Expected observable result: a clear contract exists before code is written

### Milestone 2: Runtime Error Event Capture

Scope:
- add a bounded diagnostics event model and local storage path
- capture supported-path failures and major state transitions with structured
  fields and privacy-safe messages

Files/components:
- `core/src/main/kotlin/com/kingsmetric/**`
- `app/src/main/java/com/kingsmetric/**`
- possible local persistence/config files under `data/local` or DataStore-owned
  app state
- matching JVM and Android tests

Dependencies: milestone 1
Risk: medium
Validation commands:
- `./gradlew.bat --no-daemon :core:test`
- targeted `:app:testDebugUnitTest` or `:app:connectedDebugAndroidTest`
Expected observable result: failures produce bounded, readable local
  diagnostics entries

### Milestone 3: In-App Diagnostics Viewer And Export

Scope:
- add a user-reachable diagnostics UI
- show recent entries with understandable labels
- allow export/share/copy of the bounded diagnostics artifact

Files/components:
- `app/src/main/java/com/kingsmetric/**`
- Compose screen/navigation files
- export helper/storage files
- Compose/instrumented tests

Dependencies: milestones 1-2
Risk: medium
Validation commands:
- `./gradlew.bat --no-daemon :app:assembleDebug`
- targeted `:app:connectedDebugAndroidTest`
Expected observable result: a user can reach diagnostics and export a support
  artifact without adb

### Milestone 4: Support Guidance And Repository Docs

Scope:
- document how users collect and share diagnostics
- align README, release notes, and workflow docs with the new support path

Files/components:
- `README.md`
- `docs/workflows.md`
- `CHANGELOG.md`
- release note file for the next prerelease if needed

Dependencies: milestones 1-3
Risk: low
Validation commands:
- doc-focused release/README contract tests if added
Expected observable result: the repo and release surfaces explain the support
  workflow clearly

### Milestone 5: Diagnostics Verification And Release Gate

Scope:
- define the minimum diagnostics verification bar for future releases
- ensure the next prerelease cannot claim supportability if diagnostics are
  broken or privacy bounds regress

Files/components:
- diagnostics verification/release contract files
- release workflow docs/specs if affected
- focused tests and possibly a scoped CI workflow

Dependencies: milestones 1-4
Risk: medium
Validation commands:
- `./gradlew.bat --no-daemon :core:test`
- release-contract validation commands as needed
Expected observable result: diagnostics become part of the release/support
  quality gate instead of an undocumented extra

## Progress

- [x] 2026-04-15: Confirmed the repository restructure plan is merged and the
  new plan index/workspace layout are live on `main`.
- [x] 2026-04-15: Promoted diagnostics/error-log support from prior approved
  product direction into this concrete execution plan.
- [x] 2026-04-15: Wrote the diagnostics scope/privacy spec and matching test
  spec for milestone 1.
- [x] 2026-04-15: Implemented bounded runtime diagnostics capture with a
  file-backed local recorder wired into import and save flows.
- [x] 2026-04-15: Implemented an in-app diagnostics route with bounded entry
  viewing and clipboard-based export.
- [x] 2026-04-15: Updated support docs and workflow guidance for diagnostics
  collection and sharing.
- [x] 2026-04-15: Added diagnostics support verification to the release gate
  and manual release workflow confirmation inputs.

## Surprises & Discoveries

- The repository restructure changed the planning model but did not yet promote
  the previously discussed diagnostics initiative into a concrete active plan,
  so post-release work would otherwise have no authoritative execution path.
- The alpha is already published, which raises the priority of supportability:
  diagnostics now help real users, not just internal QA.
- A simple file-backed recorder in `core` is easier to reuse across app and
  JVM tests than introducing Room or DataStore before the diagnostics viewer
  exists. That keeps milestone 2 focused on capture rather than presentation.
- A clipboard-based export action is enough for milestone 3 to make the
  diagnostics artifact user-shareable without adding FileProvider, document
  picker, or remote-upload scope.
- The already-published `v0.1.0-alpha.3` release notes should stay truthful to
  that shipped artifact, so diagnostics support guidance belongs in README and
  workflow docs now, then in the next release notes when a new prerelease is
  actually cut.

## Decision Log

- Decision: treat diagnostics as the next active initiative immediately after
  repository restructure.
  - Rationale: this was the most recent approved product direction and it
    addresses real-user support needs on the shipped alpha.
  - Date/Author: 2026-04-15 / Codex

- Decision: keep diagnostics local-first and export-oriented rather than adding
  remote telemetry.
  - Rationale: this matches the app's privacy/product constraints and avoids
    widening scope into backend/support infrastructure.
  - Date/Author: 2026-04-15 / Codex

- Decision: implement milestone 2 with a bounded file-backed recorder owned by
  the app process and injected into import/review flows.
  - Rationale: this satisfies local capture and retention requirements now,
    keeps the recorder testable in `core`, and gives milestone 3 a real store
    to read/export without committing yet to a heavier persistence schema.
  - Date/Author: 2026-04-15 / Codex

- Decision: expose diagnostics as a primary shell destination and use clipboard
  copy as the first export mechanism.
  - Rationale: this gives users a reachable in-app surface and a genuinely
    shareable artifact without adb, while keeping milestone 3 smaller than a
    file-sharing or document-provider implementation.
  - Date/Author: 2026-04-15 / Codex

- Decision: add diagnostics support as an explicit release-gate check and a
  manual release workflow confirmation input.
  - Rationale: a supportability feature is not real unless the next prerelease
    can be blocked when diagnostics viewer/export or privacy bounds regress.
  - Date/Author: 2026-04-15 / Codex

## Validation and Acceptance

Planning-stage acceptance:

- [docs/plan.md](/D:/Data/20260413-kings-metric/docs/plan.md) lists this file as the active plan.
- The previous restructure plan remains preserved and is marked closed in the
  index.
- This plan names concrete milestones, paths, constraints, and validation
  commands for a contributor to continue without chat context.

Implementation-stage validation includes:

- `./gradlew.bat --no-daemon :core:test`
- targeted `./gradlew.bat --no-daemon :app:testDebugUnitTest`
- targeted `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest`
- `./gradlew.bat --no-daemon :app:assembleDebug`

## Validation Notes

- Milestone 2 focused tests passed:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.diagnostics.DiagnosticsCaptureTest" --tests "com.kingsmetric.app.AndroidPhotoPickerRuntimeWiringTest" --tests "com.kingsmetric.app.ComposeReviewScreenAndViewModelIntegrationTest"`
- Milestone 3 focused tests passed:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.DiagnosticsScreenIntegrationTest" --tests "com.kingsmetric.app.AndroidAppShellNavigationTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DiagnosticsScreenComposeTest,com.kingsmetric.AppShellNavigationComposeTest"`
- Milestones 4 and 5 focused release/docs tests passed:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.release.ReadmeAndInstallGuidanceTest" --tests "com.kingsmetric.release.DeviceVerificationAndReleaseGateTest" --tests "com.kingsmetric.release.GitHubReleaseExecutionTest"`
- Full shared-module validation passed:
  - `./gradlew.bat --no-daemon :core:test`
- Android build wiring validation passed after injecting the diagnostics
  recorder through Hilt and the shell:
  - `./gradlew.bat --no-daemon :app:assembleDebug`

## Idempotence and Recovery

- Recreating this plan is safe: keep the file, update the metadata/progress,
  and avoid overwriting prior completed plans.
- If diagnostics scope changes materially, create follow-on specs and update
  this plan instead of replacing it with a one-line summary.
- If future work reveals that diagnostics need a separate subsystem or module,
  add a new milestone or superseding plan rather than retroactively hiding that
  complexity.

## Outcomes & Retrospective

- This plan establishes the first post-restructure active initiative and gives
  the repository a concrete path from shipped alpha to supportable alpha.
- Milestone 1 and milestone 2 are complete:
  - diagnostics contract exists
  - bounded local diagnostics capture exists for import/review/save flows
- Milestone 3 is complete:
  - users can open a diagnostics screen from inside the app
  - recent bounded entries are visible in readable language
  - the export artifact can be copied to the clipboard for support sharing
- Milestone 4 is complete:
  - README explains how users collect and share diagnostics
  - workflow docs explain the bounded local-first support flow
- Milestone 5 is complete:
  - the release gate now treats diagnostics support as an explicit required
    check
  - the manual release workflow requires diagnostics confirmation before the
    next prerelease can publish
