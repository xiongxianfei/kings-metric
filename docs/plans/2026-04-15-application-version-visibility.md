# Application Version Visibility

## Metadata

- Status: completed
- Created: 2026-04-15
- Updated: 2026-04-15
- Owner: Codex
- Related spec(s):
  - [application-version-visibility](../../specs/application-version-visibility.md)
  - [application-version-visibility.test](../../specs/application-version-visibility.test.md)
- Supersedes / Superseded by: none
- Branch / PR: TBD
- Last verified commands:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.DiagnosticsScreenIntegrationTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DiagnosticsScreenComposeTest"`

## Purpose / Big picture

Let a real user see which build of Kings Metric is currently installed so they
can report issues against the correct version without adb, GitHub, or external
release notes.

Success means:

- the app exposes its current version information in a user-reachable surface
- the shown version matches the actual installed APK metadata
- the same version string is available in the diagnostics/support path so bug
  reports can identify the running build clearly

## Context and orientation

- The repository now uses [docs/plan.md](/D:/Data/20260413-kings-metric/docs/plan.md) as a plan index and
  stores concrete work in [docs/plans](/D:/Data/20260413-kings-metric/docs/plans).
- The app is already released as a GitHub alpha and uses tracked release
  metadata:
  - [app/build.gradle.kts](/D:/Data/20260413-kings-metric/app/build.gradle.kts) loads
    `releaseVersionCode` and `releaseVersionName` from
    [.github/release-artifact.properties](/D:/Data/20260413-kings-metric/.github/release-artifact.properties)
  - [.github/release-metadata.properties](/D:/Data/20260413-kings-metric/.github/release-metadata.properties)
    tracks the release tag and GitHub-facing positioning
- There is no current user-facing version display in the app shell:
  - shell routing/chrome lives in
    [AppShell.kt](/D:/Data/20260413-kings-metric/core/src/main/kotlin/com/kingsmetric/app/AppShell.kt)
    and
    [AppShellChrome.kt](/D:/Data/20260413-kings-metric/core/src/main/kotlin/com/kingsmetric/app/AppShellChrome.kt)
  - top-level Compose wiring lives in
    [HistoryDashboardScreens.kt](/D:/Data/20260413-kings-metric/app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt)
  - the existing support-oriented user surface is
    [DiagnosticsScreenRoute.kt](/D:/Data/20260413-kings-metric/app/src/main/java/com/kingsmetric/app/DiagnosticsScreenRoute.kt)
    backed by
    [DiagnosticsScreenIntegration.kt](/D:/Data/20260413-kings-metric/core/src/main/kotlin/com/kingsmetric/app/DiagnosticsScreenIntegration.kt)
- Diagnostics exports already carry an `appVersion` metadata field in some
  paths, but the screen does not explicitly present the current installed build
  as a first-class “About this build” element.

## Constraints

- Keep the change local-first and Android-only. No remote release lookup, Play
  Services, network fetch, or GitHub API call from the app.
- Use one runtime source of truth for the displayed version. Do not hardcode a
  duplicate app version string in UI code.
- The displayed value must match the actually installed APK metadata, not just
  the intended release tag in docs.
- Keep scope small: this is version visibility, not a full settings/about
  feature.
- Keep business and formatting logic out of Compose where practical.
- Non-goals for this initiative:
  - full in-app changelog browsing
  - release-note rendering inside the app
  - Play Store upgrade prompts
  - account/device identifiers

## Done when

- A feature spec and matching test spec define where version info appears, what
  it contains, and how it stays accurate.
- The app shows a readable current-version string in a user-reachable screen.
- The diagnostics/support surface shows the current app version without forcing
  the user to trigger a failure first.
- The implementation reads from a single runtime source of truth tied to the
  installed build metadata.
- Focused JVM and Android verification prove the visible version matches the
  configured build.

## Milestones

### Milestone 1: Version Visibility Contract

Scope:
- define where users can find current version info
- define the formatting contract, source-of-truth expectations, and
  compatibility with diagnostics/support flows
- define test coverage for visible version info and build-metadata alignment

Files/components:
- `specs/application-version-visibility.md`
- `specs/application-version-visibility.test.md`
- `docs/workflows.md` only if the support/release handoff contract changes

Dependencies: none
Risk: low
Validation commands:
- none beyond spec review
Expected observable result:
- there is a clear contract for what version/build info the app shows and where

### Milestone 2: Runtime Version Source And Diagnostics Surface

Scope:
- add a single runtime provider or model for current app version/build info
- surface that information in the Diagnostics screen, which is already a
- user-reachable primary destination
- keep the UI readable and clearly labeled so users can report the current app
  version without exporting diagnostics first

Files/components:
- `app/src/main/java/com/kingsmetric/**`
- `core/src/main/kotlin/com/kingsmetric/app/DiagnosticsScreenIntegration.kt`
- `app/src/main/java/com/kingsmetric/app/DiagnosticsScreenRoute.kt`
- Hilt/app wiring files if a provider needs injection
- focused JVM and Compose tests

Dependencies: milestone 1
Risk: low to medium
Validation commands:
- `./gradlew.bat --no-daemon :app:assembleDebug`
- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.DiagnosticsScreenIntegrationTest"`
- `./gradlew.bat --no-daemon :app:assembleDebugAndroidTest :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DiagnosticsScreenComposeTest"`
Expected observable result:
- a user can open Diagnostics and read the current app version/build directly

### Milestone 3: Release And Support Alignment

Scope:
- ensure diagnostics export and visible version info stay aligned
- document the intended support workflow if visible version info changes how
  users report bugs
- verify the displayed version continues to match tracked release metadata and
  built APK metadata

Files/components:
- `README.md` only if support instructions need a visible-version note
- release-contract tests under `core/src/test/kotlin/com/kingsmetric/release/`
- diagnostics tests if export wording changes

Dependencies: milestones 1-2
Risk: low
Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.release.ReleaseMetadataAndPositioningTest" --tests "com.kingsmetric.release.ReadmeAndInstallGuidanceTest"`
- `./gradlew.bat --no-daemon :app:assembleRelease` with signing inputs when cutting a release
Expected observable result:
- user-visible version info, diagnostics support, and release metadata stay in sync

## Progress

- [x] 2026-04-15: Confirmed there is no current in-app version display despite
  tracked release metadata and diagnostics support.
- [x] 2026-04-15: Created this concrete active plan for visible app version
  info.
- [x] Milestone 1: write the feature spec and test spec.
- [x] Milestone 2: implement a runtime version source and Diagnostics display.
- [x] Milestone 3: align release/support verification and docs if needed.
- [x] 2026-04-15: Added a package-metadata-backed runtime version provider and
  surfaced the current app version in the Diagnostics screen and copied export.
- [x] 2026-04-15: Replaced raw diagnostics epoch millis with readable formatted
  timestamps in the visible Diagnostics UI and export artifact.
- [x] 2026-04-15: Updated README support guidance so users are told to report
  the visible Diagnostics `Current Version`, and aligned release-contract tests
  with the tracked release metadata/version mapping.

## Surprises & Discoveries

- The app already injects `appVersion` into some diagnostics metadata, but that
  value is not exposed as a first-class visible “current build” field in the UI.
- The Diagnostics screen is already a primary shell destination, which makes it
  the smallest user-reachable surface for version visibility without adding a
  whole new Settings/About route.
- Release metadata lives in tracked repo files, but user-visible correctness
  still has to come from the installed app runtime metadata, not docs alone.
- The same support surface also benefited from readable timestamp formatting:
  raw epoch millis were technically present but not realistically useful for
  user bug reports.

## Decision Log

- Decision: treat version visibility as a new approved initiative instead of
  folding it into the closed diagnostics plan.
  - Rationale: this is a new user-visible behavior request after the previous
    diagnostics initiative completed.
  - Date/Author: 2026-04-15 / Codex

- Decision: plan around the Diagnostics screen as the first user-facing version
  surface.
  - Rationale: it is already reachable from the primary navigation and is the
    support-oriented place where users need to identify the current build.
  - Date/Author: 2026-04-15 / Codex

- Decision: require a single runtime source of truth tied to the installed app
  metadata.
  - Rationale: docs and tracked release properties can drift; the UI must show
    the version of the APK the user is actually running.
  - Date/Author: 2026-04-15 / Codex

- Decision: use Android package metadata as the runtime version source.
  - Rationale: it reflects the installed APK directly for both debug and
    release builds without duplicating a UI-only version constant.
  - Date/Author: 2026-04-15 / Codex

- Decision: format diagnostics timestamps into readable local date/time strings
  while touching the Diagnostics surface for version visibility.
  - Rationale: users need to report when a failure happened without converting
    raw epoch millis outside the app.
  - Date/Author: 2026-04-15 / Codex

- Decision: keep milestone 3 scoped to README support guidance plus release
  contract verification instead of widening into a separate About/settings
  surface.
  - Rationale: the Diagnostics screen is already the support-oriented runtime
    version surface, so the remaining work was alignment, not another UI entry
    point.
  - Date/Author: 2026-04-15 / Codex

## Validation and Acceptance

Planning-stage acceptance:

- [docs/plan.md](/D:/Data/20260413-kings-metric/docs/plan.md) lists this file as the active plan.
- The closed diagnostics and repository-restructure plans remain preserved in
  the index.
- The plan names concrete files, milestones, and commands for future
  implementation.

Implementation-stage acceptance includes:

- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.DiagnosticsScreenIntegrationTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`
- `./gradlew.bat --no-daemon :app:assembleDebugAndroidTest :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DiagnosticsScreenComposeTest"`
- release-contract checks if milestone 3 changes support/release wording

## Validation Notes

- 2026-04-15: `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.DiagnosticsScreenIntegrationTest"` passed after wiring version visibility and readable diagnostics time formatting.
- 2026-04-15: `./gradlew.bat --no-daemon :app:assembleDebug` passed with the Android package-metadata version provider in place.
- 2026-04-15: `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DiagnosticsScreenComposeTest"` passed. One earlier emulator teardown failure reproduced as a harness-only flake and passed on immediate rerun without code changes.
- 2026-04-15: `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.release.ReleaseMetadataAndPositioningTest" --tests "com.kingsmetric.release.ReadmeAndInstallGuidanceTest"` passed after updating README support guidance and removing the stale hardcoded release-tag expectation from the release metadata tests.

## Idempotence and Recovery

- Re-running planning is safe: update this file and `docs/plan.md` instead of
  overwriting closed plans.
- If implementation shows that Diagnostics is the wrong surface, keep this plan
  and record the routing decision in the Decision Log instead of starting over.
- If a later broader Settings/About initiative supersedes this work, preserve
  this plan and mark it superseded rather than deleting it.

## Outcomes & Retrospective

- This plan turns the user request into a concrete execution path instead of a
  one-line TODO.
- The smallest viable direction is clear:
  - define the contract
  - expose runtime version info from the installed app
  - render it in the Diagnostics surface users can already reach
- The support contract now matches the shipped runtime behavior: users can read
  `Current Version` inside Diagnostics, include it in support reports, and the
  release-contract tests verify that tracked release metadata stays aligned with
  the versioned artifact.
- Future contributors should remember that release metadata and visible app
  version are related but not interchangeable; the app UI should reflect the
  installed build, not just the tracked docs.
