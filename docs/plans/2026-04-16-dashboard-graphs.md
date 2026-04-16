# Add Bounded Graphs To The Dashboard

## Metadata

- Status: active
- Created: 2026-04-16
- Updated: 2026-04-16
- Owner: Codex
- Related spec(s):
  - [metrics-dashboard](../../specs/metrics-dashboard.md)
  - [dashboard-clarity-upgrade](../../specs/dashboard-clarity-upgrade.md)
  - [history-and-dashboard-screen-binding](../../specs/history-and-dashboard-screen-binding.md)
- Supersedes / Superseded by: none
- Branch / PR: TBD
- Last verified commands:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest,com.kingsmetric.SharedUxLabelsAndStateMessagingComposeTest,com.kingsmetric.DashboardGraphComponentsComposeTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`

## Purpose / Big picture

The dashboard currently shows useful numbers, but it still reads like a small
list of cards. The user now wants graphs so trends are easier to understand at
a glance.

This initiative adds bounded, local-first dashboard graphs that make saved
match trends more obvious without turning the app into a generic analytics
tool. Success is visible when:

- the user can open `Dashboard` and see trend shape, not only isolated numbers
- graphs remain understandable on a phone-sized portrait screen
- graphs stay honest about small sample sizes and missing data
- the existing dashboard cards remain clear instead of being replaced by dense
  charting

This is a new feature initiative, not a silent extension of the old dashboard
clarity work.

## Context and orientation

The current dashboard stack is simple and deliberately card-based:

- data calculation lives in
  `core/src/main/kotlin/com/kingsmetric/dashboard/MetricsDashboard.kt`
- dashboard state shaping lives in
  `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- the Room-backed dashboard flow is exposed from
  `core/src/main/kotlin/com/kingsmetric/data/local/RoomObservedMatchRepository.kt`
- the actual Compose dashboard screen is rendered in
  `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`

Current dashboard behavior:

- `DashboardMetricsCalculator` computes:
  - win rate
  - average KDA
  - hero usage
  - recent performance summary
- `DashboardScreenUiState` currently exposes:
  - `primaryCards`
  - `contextText`
  - `sparseDataText`
  - `secondaryNotes`
- `DashboardScreen` currently renders:
  - one context block
  - one primary metrics card
  - one sparse-data block when needed
  - one secondary notes card

Current test coverage already pins those surfaces:

- `core/src/test/kotlin/com/kingsmetric/dashboard/MetricsDashboardTest.kt`
- `core/src/test/kotlin/com/kingsmetric/app/HistoryDashboardScreenBindingTest.kt`
- `core/src/test/kotlin/com/kingsmetric/app/HistoryDetailDashboardUxTest.kt`
- `core/src/test/kotlin/com/kingsmetric/data/local/RoomRepositoriesObservedIntegrationTest.kt`
- `app/src/androidTest/java/com/kingsmetric/HistoryDetailDashboardUxComposeTest.kt`
- `app/src/androidTest/java/com/kingsmetric/SharedUxLabelsAndStateMessagingComposeTest.kt`

Important contract boundary:

- `specs/metrics-dashboard.md` currently treats advanced charts as a non-goal
- `specs/dashboard-clarity-upgrade.md` explicitly says clarity work should not
  change formulas or expand into advanced analytics

That means graph work needs a new feature spec and test spec before any
behavior change.

Important technical discovery:

- `app/build.gradle.kts` currently has no chart library dependency
- the app already uses Jetpack Compose + Material 3 successfully for all other
  surfaces
- the current graph need is bounded and local to one dashboard screen

So the current best-practice default is:

- keep chart data shaping in `core`
- render charts with small custom Compose primitives in `app`
- avoid a third-party chart library unless the spec later proves the bounded
  Compose approach is insufficient

## Constraints

- Graphs MUST use locally saved match records only.
- Graphs MUST stay additive to the current dashboard, not replace the existing
  card-based summary hierarchy.
- Metric formulas MUST remain outside Compose UI code.
- The first graph release MUST stay bounded to non-interactive graphs:
  - no arbitrary date filtering
  - no pinch/zoom
  - no drag tooltips
  - no export/share flow
- Graphs MUST degrade safely for:
  - no saved records
  - one or two saved records
  - partial metric availability
- Graphs MUST remain readable on a phone-sized portrait layout without
  horizontal scrolling.
- Graph surfaces MUST keep accessible text/semantics so the dashboard is still
  understandable when a chart is visually dense.
- Do not add server analytics, remote sync, or inferred data beyond what the
  current saved records support.
- Do not add a chart dependency casually. If custom Compose primitives stop
  being sufficient, that dependency decision must be explicit and separately
  justified in the plan/spec flow.

Non-goals for this initiative:

- generic analytics tooling
- multi-range time filtering
- interactive drill-down charts
- coaching overlays or recommendation engines inside dashboard graphs
- replacing the existing detail or history surfaces

## Done when

This initiative is done when all of the following are true:

- the dashboard shows a bounded graph section derived from saved local records
- the first graph release stays within an approved small set of graph types
- primary dashboard cards remain visible and understandable
- sparse/empty/error states remain explicit instead of showing blank or
  misleading graphs
- graph data is shaped in testable Kotlin code outside Compose
- Compose rendering remains readable on a phone-sized portrait screen
- the new graph behavior is covered by focused JVM tests, repository/binding
  tests, Compose tests, and a manual narrow-phone review checklist

## Milestones

### Milestone 1: Define The Graph Contract

Scope:

- create the new graph-specific feature spec and test spec
- choose the exact first-release graph set and the explicit non-goals
- define sparse-data, empty, and partial-data behavior before code changes

Files or components touched:

- `specs/dashboard-graphs.md`
- `specs/dashboard-graphs.test.md`
- possibly narrow updates to:
  - `specs/metrics-dashboard.md`
  - `specs/dashboard-clarity-upgrade.md`

Dependencies:

- current dashboard contracts in `specs/metrics-dashboard.md`
- current dashboard clarity contract in `specs/dashboard-clarity-upgrade.md`

Risk:

- silently stretching the old “clarity only” dashboard spec
- choosing too many graphs for a first release
- introducing graphs that require new formulas before the contract says so

Validation commands:

- planning/spec review only in this milestone

Expected observable result:

- one approved graph contract defines:
  - exact graph types
  - graph ordering
  - graph density bounds
  - graph-state behavior for empty/sparse/partial/error
  - semantics/text fallback expectations

### Milestone 2: Add Graph-Ready Dashboard Models In Core

Scope:

- extend dashboard calculation output with graph-ready series models
- keep graph data derived from existing saved records and current formulas
- preserve current dashboard cards while adding graph-specific state

Files or components touched:

- `core/src/main/kotlin/com/kingsmetric/dashboard/MetricsDashboard.kt`
- `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- `core/src/main/kotlin/com/kingsmetric/data/local/RoomObservedMatchRepository.kt`
- `core/src/test/kotlin/com/kingsmetric/dashboard/MetricsDashboardTest.kt`
- `core/src/test/kotlin/com/kingsmetric/app/HistoryDashboardScreenBindingTest.kt`
- `core/src/test/kotlin/com/kingsmetric/app/HistoryDetailDashboardUxTest.kt`
- `core/src/test/kotlin/com/kingsmetric/data/local/RoomRepositoriesObservedIntegrationTest.kt`

Dependencies:

- milestone-1 spec pair

Risk:

- accidentally changing existing metric formulas
- graph series depending on data that is not actually saved
- breaking current dashboard card behavior while adding graph state

Validation commands:

- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.dashboard.MetricsDashboardTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest" --tests "com.kingsmetric.data.local.RoomRepositoriesObservedIntegrationTest"`

Expected observable result:

- loaded dashboard state now includes graph-ready data structures with explicit
  empty/sparse/partial handling, while the existing cards still map correctly

### Milestone 3: Build Shared Compose Graph Primitives

Scope:

- create the small reusable chart components needed for the approved graph set
- keep them dashboard-specific or small shared UI primitives, not a generic
  chart framework
- expose explicit test tags and semantics for Compose assertions

Files or components touched:

- `app/src/main/java/com/kingsmetric/ui/components/`
  - likely a new dashboard graph component file
- optional narrow theme/token adjustments in
  `app/src/main/java/com/kingsmetric/ui/theme/`
- graph-focused Compose tests:
  - either extend `app/src/androidTest/java/com/kingsmetric/HistoryDetailDashboardUxComposeTest.kt`
  - or add a new focused dashboard-graphs Compose test file

Dependencies:

- milestone-2 graph models
- existing shell visual foundation from the UI polish work

Risk:

- charts that are visually dense but semantically empty
- custom drawing that looks good on desktop widths but clips on phones
- over-engineering a reusable chart system before the first bounded release

Validation commands:

- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Expected observable result:

- the app has reusable, bounded graph composables that can render the approved
  dashboard series with readable labels, semantics, and sparse-data treatment

### Milestone 4: Integrate Graphs Into The Dashboard Screen

Scope:

- place the graph section into `DashboardScreen`
- keep the current card hierarchy understandable
- ensure graph placement does not make the dashboard feel cluttered or
  ambiguous

Files or components touched:

- `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- `app/src/androidTest/java/com/kingsmetric/HistoryDetailDashboardUxComposeTest.kt`
- `app/src/androidTest/java/com/kingsmetric/SharedUxLabelsAndStateMessagingComposeTest.kt`
- optionally `app/src/androidTest/java/com/kingsmetric/UxRegressionGapFillComposeTest.kt`

Dependencies:

- milestone-3 graph primitives

Risk:

- graphs pushing primary cards too low on the screen
- sparse-data notes conflicting with graph visuals
- blank or misleading graph sections on small datasets

Validation commands:

- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest,com.kingsmetric.SharedUxLabelsAndStateMessagingComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Expected observable result:

- the dashboard shows graphs in a clear section without losing current cards,
  empty/sparse/error states still read clearly, and the graph section behaves
  like part of one coherent screen

### Milestone 5: Final Phone-Sized Review And Release Readiness

Scope:

- close the residual layout/accessibility gaps
- run the final focused validation set
- update the living plan with real outcomes

Files or components touched:

- active plan file
- any final narrow UI/test adjustments found during manual review
- `docs/workflows.md` only if a durable chart-related runtime/testing rule is
  discovered

Dependencies:

- milestone 4

Risk:

- graph labels clipping on narrow phones
- semantics/tests depending on brittle visual text rather than stable tags
- unintentionally introducing a graph that looks meaningful with only one match

Validation commands:

- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.dashboard.MetricsDashboardTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest" --tests "com.kingsmetric.data.local.RoomRepositoriesObservedIntegrationTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest,com.kingsmetric.SharedUxLabelsAndStateMessagingComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Manual acceptance checklist:

- dashboard graphs remain readable in portrait on a narrow phone-sized screen
- no graph requires horizontal scrolling
- primary metric cards are still easy to find
- one or two saved matches do not produce a misleading “full analytics” look
- empty/error states stay explicit instead of showing a blank chart container
- graph labels and state messages remain user-facing rather than technical

Expected observable result:

- the graph feature is releaseable without crowding the dashboard or
  overstating what the saved data can prove

## Progress

- [x] 2026-04-16: Reviewed the current dashboard implementation, repository
  flow, existing specs, and current tests to scope the graph initiative.
- [x] 2026-04-16: Decided the best-practice default is Compose-native bounded
  graph components, not a third-party chart dependency.
- [x] 2026-04-16: Milestone 1 complete with:
  - `specs/dashboard-graphs.md`
  - `specs/dashboard-graphs.test.md`
- [x] 2026-04-16: Milestone 2 complete.
  - added graph-ready dashboard series in `core/src/main/kotlin/com/kingsmetric/dashboard/MetricsDashboard.kt`
  - added dashboard graph section state in `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
  - updated focused JVM and repository/binding tests first, then implemented the minimum state mapping to satisfy them
- [x] 2026-04-16: Milestone 3 complete.
  - added bounded dashboard graph primitives in `app/src/main/java/com/kingsmetric/ui/components/DashboardGraphComponents.kt`
  - added focused Compose coverage in `app/src/androidTest/java/com/kingsmetric/DashboardGraphComponentsComposeTest.kt`
  - kept the graph primitives dashboard-bounded and non-interactive without wiring them into `DashboardScreen` yet
- [x] 2026-04-16: Milestone 4 complete.
  - integrated `graphSection` rendering into `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
  - extended dashboard screen Compose coverage in `app/src/androidTest/java/com/kingsmetric/HistoryDetailDashboardUxComposeTest.kt`
  - aligned one stale shared-label Compose assertion in `app/src/androidTest/java/com/kingsmetric/SharedUxLabelsAndStateMessagingComposeTest.kt` with the current detail-screen preview-status contract
- [ ] 2026-04-16: Milestone 5 in progress.
  - final focused JVM and Android validation passed
  - manual narrow-phone review checklist is still pending

## Surprises & Discoveries

- The current dashboard feature spec explicitly names advanced charts as a
  non-goal, so graph work cannot safely start as “just UI polish.”
- The repository already exposes enough saved-record data for bounded graphing
  of current dashboard metrics, but it does not expose richer telemetry such as
  minute-by-minute trends or event timelines.
- There is no existing chart dependency in `app/build.gradle.kts`, which makes
  a Compose-native first release the lower-risk option for this repo.
- Milestone 2 did not require a dedicated repository-flow rewrite. Once
  `DashboardMetricsCalculator` exposed graph-ready series, the existing
  `observeDashboard()` flow could emit the richer loaded state unchanged.
- Milestone 3 did not require theme-token changes. The existing shell surface
  foundation was sufficient for bounded dashboard graph cards.
- Milestone 4 surfaced one stale Compose test assumption: `RecordDetailScreen`
  now renders preview-unavailable wording from `DetailScreenUiState` rather
  than deriving it internally, so the shared-label fixture needed to provide
  that state explicitly.

## Decision Log

- Decision: keep graphs additive to the current dashboard cards instead of
  replacing the primary card section.
  - Rationale: the current dashboard already has understandable top-level
    cards. Graphs should explain trends, not hide the headline metrics.
  - Date/Author: 2026-04-16 / Codex

- Decision: prefer small custom Compose graph primitives over adding a
  third-party chart library in the first release.
  - Rationale: the graph scope is bounded, the app already uses Compose
    successfully, and adding a new dependency would increase maintenance and
    review surface before proving the need.
  - Date/Author: 2026-04-16 / Codex

- Decision: first-release graphs must be based on already approved dashboard
  metric families rather than new analytics formulas.
  - Rationale: this keeps the feature honest, keeps formulas in `core`, and
    avoids silently changing the meaning of dashboard data.
  - Date/Author: 2026-04-16 / Codex

- Decision: keep graph-ready series models in `core` and chart drawing in
  `app`.
  - Rationale: this matches the repo architecture rule that metric
    calculations stay out of Compose UI code.
  - Date/Author: 2026-04-16 / Codex

- Decision: loaded dashboard state should keep a fixed graph-panel order of
  `Recent Results` then `Hero Usage`, with per-panel unavailable messaging
  instead of silently dropping missing graph families.
  - Rationale: this satisfies the bounded graph contract, preserves one stable
    dashboard reading order, and lets partial data degrade independently.
  - Date/Author: 2026-04-16 / Codex

- Decision: the first reusable `Recent Results` primitive uses fixed-width
  point columns instead of a weighted row layout.
  - Rationale: the panel is contract-bounded to at most five points, and fixed
    widths avoid scope-specific layout coupling while still fitting the
    phone-sized portrait target for this milestone.
  - Date/Author: 2026-04-16 / Codex

- Decision: `DashboardScreen` should render the graph section after the
  existing primary cards and before sparse-data or secondary-note blocks.
  - Rationale: this keeps the headline metrics easy to find while making the
    graph section feel additive instead of replacing the dashboard summary.
  - Date/Author: 2026-04-16 / Codex

## Validation and Acceptance

Planning-time validation completed in this turn:

- read `AGENTS.md`
- read `docs/workflows.md`
- read `docs/plan.md`
- read the current dashboard implementation in:
  - `core/src/main/kotlin/com/kingsmetric/dashboard/MetricsDashboard.kt`
  - `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
  - `core/src/main/kotlin/com/kingsmetric/data/local/RoomObservedMatchRepository.kt`
  - `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- read the current dashboard contracts in:
  - `specs/metrics-dashboard.md`
  - `specs/dashboard-clarity-upgrade.md`
  - `specs/dashboard-clarity-upgrade.test.md`
- read the current dashboard-related tests in:
  - `core/src/test/kotlin/com/kingsmetric/dashboard/MetricsDashboardTest.kt`
  - `core/src/test/kotlin/com/kingsmetric/app/HistoryDashboardScreenBindingTest.kt`
  - `app/src/androidTest/java/com/kingsmetric/HistoryDetailDashboardUxComposeTest.kt`

Final implementation acceptance for this initiative should require:

- focused JVM validation of dashboard calculations and state mapping
- focused repository/binding validation for dashboard emissions
- focused Compose validation of graph rendering and empty/sparse/error states
- `./gradlew.bat --no-daemon :app:assembleDebug`
- a manual narrow-phone review pass

## Validation Notes

- Focused milestone-2 validation passed:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.dashboard.MetricsDashboardTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest" --tests "com.kingsmetric.data.local.RoomRepositoriesObservedIntegrationTest"`
- The new graph-ready state remained additive:
  - existing primary dashboard cards still map from the current metric family
  - empty and error dashboard states still expose no graph section
  - loaded dashboard state now carries a stable graph section with explicit
    per-panel unavailable messaging when only one graph has usable data
- Focused milestone-3 validation passed:
  - `./gradlew.bat --no-daemon :app:assembleDebugAndroidTest`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DashboardGraphComponentsComposeTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`
- The new graph primitives remained bounded:
  - graph panels stay non-interactive
  - visible titles and labels remain user-facing without color-only meaning
  - unavailable graph messaging is part of the same reusable graph section
- Focused milestone-4 validation passed:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest,com.kingsmetric.SharedUxLabelsAndStateMessagingComposeTest,com.kingsmetric.DashboardGraphComponentsComposeTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`
- The integrated dashboard remained additive:
  - primary cards still render ahead of the graph section
  - partial graph availability keeps the usable panel visible and the missing
    panel explicit
  - empty and error dashboard states still render without graph placeholders
- Final focused milestone-5 automation passed:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.dashboard.MetricsDashboardTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest" --tests "com.kingsmetric.data.local.RoomRepositoriesObservedIntegrationTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest,com.kingsmetric.SharedUxLabelsAndStateMessagingComposeTest,com.kingsmetric.DashboardGraphComponentsComposeTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`
- Remaining milestone-5 gap:
  - the manual narrow-phone review checklist from this plan has not been
    completed in this turn

## Idempotence and Recovery

- Re-running planning for this initiative is safe because it only adds a new
  concrete plan file and an index entry.
- Milestone 2 should remain idempotent by keeping graph series as pure derived
  data from saved records.
- If milestone 3 shows that Compose-native graph primitives cannot satisfy the
  approved contract cleanly, stop before adding a dependency and create an
  explicit follow-up plan/spec decision for chart-library adoption.
- If milestone 4 makes the dashboard too dense on a phone-sized screen, the
  safe rollback is to keep the existing card dashboard intact and back out the
  graph section rather than degrading the whole screen.

## Outcomes & Retrospective

This plan establishes the best-practice path for dashboard graphs in this
project:

- start with a new graph-specific contract
- keep formulas and graph-ready data in `core`
- keep drawing and layout in `app`
- keep the first graph release small, local, and phone-readable

Implementation is in progress through milestone 4. The next correct step is
milestone 5: final phone-sized review and the remaining release-readiness
checks.
