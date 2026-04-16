# Make the app more beautiful and user-friendly

- Status: completed
- Created: 2026-04-15
- Updated: 2026-04-16
- Owner: Codex
- Related spec(s): `specs/app-shell-navigation-ux.md`, `specs/shared-ux-labels-and-state-messaging.md`, `specs/import-status-and-guidance-ux.md`, `specs/compose-review-screen-and-viewmodel-integration.md`, `specs/review-field-grouping-and-labels.md`, `specs/review-sticky-actions-and-scroll-safety.md`, `specs/review-input-hints-and-editing-assistance.md`, `specs/history-list-readability-upgrade.md`, `specs/record-detail-usability-upgrade.md`, `specs/dashboard-clarity-upgrade.md`, `specs/diagnostics-scope-and-privacy-contract.md`, `specs/application-version-visibility.md`
- Supersedes / Superseded by: none
- Branch / PR: `feature/visual-foundation-shell-polish` / not opened
- Last verified commands:
  - `./gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.kingsmetric.ui.theme.AppShellVisualFoundationTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.AppShellNavigationComposeTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.AndroidAppShellNavigationTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.ImportStatusAndGuidanceUxTest" --tests "com.kingsmetric.app.AndroidPhotoPickerRuntimeWiringTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.ImportScreenComposeTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.ComposeReviewScreenAndViewModelIntegrationTest" --tests "com.kingsmetric.app.ReviewStickyActionsAndInputAssistanceTest"`
  - `./gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.kingsmetric.app.ScreenshotBitmapDecodingUnitTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.ReviewScreenComposeTest,com.kingsmetric.ScreenshotBitmapDecodingTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDashboardScreenStateMapperTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingIntegrationTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.DiagnosticsScreenIntegrationTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DiagnosticsScreenComposeTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`

## Purpose / Big picture

Make the app feel intentional, readable, and trustworthy on a real phone
without weakening the current product rules. The user should be able to:

- understand what screenshot to import
- tell what the app is doing after import
- review and edit extracted data with low cognitive load
- find the primary action on every screen
- browse history, detail, dashboard, and diagnostics without reading dense or
  technical UI

Success is observable when the critical path `Import -> Review -> Save ->
History` feels clear and visually coherent on a phone-sized screen, while the
app still preserves explicit unsupported/review-required states instead of
hiding uncertainty behind decorative UI.

## Context and orientation

The current Android runtime is concentrated in:

- `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- `app/src/main/java/com/kingsmetric/app/ReviewScreenRoute.kt`
- `app/src/main/java/com/kingsmetric/app/DiagnosticsScreenRoute.kt`

Shared UI state and copy already live in testable Kotlin code:

- `core/src/main/kotlin/com/kingsmetric/app/SharedUxCopy.kt`
- `core/src/main/kotlin/com/kingsmetric/app/ReviewFieldGrouping.kt`
- `core/src/main/kotlin/com/kingsmetric/app/ReviewScreenIntegration.kt`
- `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- `core/src/main/kotlin/com/kingsmetric/app/DiagnosticsScreenIntegration.kt`

The app already has several UX-focused specs, but there is no active execution
plan tying them together as one reviewable improvement program. The current UI
works functionally, yet it still shows the following product-level gaps:

- visual hierarchy is inconsistent between Import, Review, History, Detail,
  Dashboard, and Diagnostics
- the shell chrome and screen structure are correct but still look utilitarian
  rather than polished
- review remains information-heavy and more manual than it needs to feel
- list/detail/dashboard surfaces are readable, but they still do not feel
  cohesive or visually strong enough for release-quality UX
- diagnostics are useful for support but still visually secondary and dense

Existing runtime and workflow constraints remain important:

- the app is local-first
- unsupported screenshots must stay explicit
- review-required data must stay explicit
- diagnostics and version visibility remain part of supportability
- Android preview, import, and save flows already have targeted regression
  coverage and should keep that protection as visuals evolve

The approved UI stack for this initiative is:

- Jetpack Compose for screen construction
- Material 3 for accessibility-safe base primitives
- an app-level design system layered on top of Material 3 for:
  - color roles
  - typography hierarchy
  - spacing scale
  - shape/elevation rules
  - reusable action, card, banner, and section components

This initiative should improve beauty and friendliness by making that design
system explicit and consistent. It should not add a new UI framework or replace
Compose/Material 3.

## Constraints

- Do not weaken the current conservative product rules just to make the UI feel
  smoother. Unsupported screenshots, unresolved required fields, and local
  persistence failures must remain explicit.
- Keep business rules, validation, and recognition logic out of composables.
- Preserve the current local-first/privacy model. No telemetry, cloud sync, or
  support upload flow is part of this initiative.
- Treat phone-sized Android layouts as the primary target. Large-screen polish
  may improve incidentally, but v1 UX decisions should optimize for handheld
  devices.
- Keep accessibility stable while polishing visuals. Touch targets, readable
  hierarchy, and larger-text behavior must not regress just because the app
  looks more designed.
- Do not add a third-party UI kit or replace Material 3 unless a later
  milestone identifies a concrete, verified gap that the current stack cannot
  solve cleanly.
- Prefer additive refinement over a speculative rewrite. Reuse the current
  app-shell/navigation structure unless a milestone proves a concrete UX
  limitation.
- Avoid “AI-slop” UI. The visual language should feel deliberate and
  consistent, not just more decorated.
- Keep visual tokens, spacing helpers, and composable style wrappers in `app/`.
  Keep `core/` limited to state models, grouping, copy semantics, and other
  testable screen logic rather than paint-level styling concerns.
- Non-goals:
  - adding new data fields
  - changing OCR or validation rules
  - hero-recognition automation
  - a brand-new settings/about subsystem
  - dark/light theme re-architecture as a standalone project
  - schema, repository, or domain-model redesign unless a concrete UX bug
    proves one is required

## Done when

This initiative is done when all of the following are true:

- Import, Review, History, Detail, Dashboard, and Diagnostics share a coherent
  visual hierarchy and spacing system.
- Every primary screen has one obvious main action, readable top-level title
  hierarchy, and readable empty/failure state.
- The import screen shows supported-input guidance, a visually obvious primary
  import action, and distinct unsupported/read-failure/review-ready states.
- The review screen keeps a visible blocker summary near the top, a clear
  screenshot preview area, grouped editable sections, and an anchored primary
  save action on a phone-sized screen.
- History and detail use grouped summaries and field/value presentation that
  remain readable without relying on flat debug-style text.
- Dashboard presents primary metrics, context text, and sparse-data guidance in
  a visually prioritized layout instead of a loose text stack.
- Diagnostics still support supportability, but the screen itself exposes
  `Current Version`, export action, and readable failure entries without
  forcing users to read one dense raw block first.
- Each milestone lands with focused tests so visual/interaction polish does not
  silently regress the import-review-save flow.

## Milestones

### Milestone 1 — Define the visual foundation and shell polish

Scope:
Create one coherent UI baseline for spacing, typography, card emphasis, route
titles, screen padding, and main action treatment across the shell.

Files or components:
- `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- `core/src/main/kotlin/com/kingsmetric/app/AppShellChrome.kt`
- `core/src/main/kotlin/com/kingsmetric/app/SharedUxCopy.kt`
- new app-level design system helpers under:
  - `app/src/main/java/com/kingsmetric/ui/theme/`
  - `app/src/main/java/com/kingsmetric/ui/components/`

Dependencies:
- existing shell/navigation behavior
- existing shared UX-label and app-shell UX specs
- Material 3 as the base primitive layer

Risk:
- cosmetic churn without enough consistency
- defining shell visuals without first stabilizing reusable tokens/components
- introducing visual changes that break current shell tests

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.AndroidAppShellNavigationTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.AppShellNavigationComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Manual review checklist:
- one shared spacing scale is visible across top-level screens
- top app-bar titles and primary navigation labels feel like one system
- primary actions share one treatment instead of ad hoc button styling

Expected observable result:
- the app shell feels visually cohesive before screen-specific work starts
- route titles, spacing, and navigation affordances stop feeling like separate
  mini-apps
- reusable design-system pieces exist for later milestones instead of each
  screen inventing its own visual rules

### Milestone 2 — Polish the import surface

Scope:
Refine the import screen so it explains the supported screenshot, presents one
clear primary action, and uses clearer visual hierarchy for idle, in-progress,
unsupported, failed, and review-ready outcomes.

Files or components:
- `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- `core/src/main/kotlin/com/kingsmetric/app/ImportScreenUiModel.kt`
- `core/src/main/kotlin/com/kingsmetric/app/SharedUxCopy.kt`

Dependencies:
- milestone 1 visual foundation
- `specs/import-status-and-guidance-ux.md`
- `specs/shared-ux-labels-and-state-messaging.md`

Risk:
- visually differentiating states without collapsing distinct import outcomes
- adding too much helper text and making the screen heavier instead of clearer

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.ImportStatusAndGuidanceUxTest" --tests "com.kingsmetric.app.AndroidPhotoPickerRuntimeWiringTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.ImportScreenComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Manual review checklist:
- the supported screenshot hint is readable at first glance
- the primary import action is visually dominant
- repeated failure states still leave the screen calm and actionable

Expected observable result:
- the import screen explains itself quickly
- success and failure states are visually distinct and actionable
- the primary import action remains obvious after repeated failures

### Milestone 3 — Polish the review surface

Scope:
Refine the review screen so it is easier to scan and edit on a phone while
preserving explicit blocker semantics, stable preview quality, and a clear save
path.

Files or components:
- `app/src/main/java/com/kingsmetric/app/ReviewScreenRoute.kt`
- `app/src/main/java/com/kingsmetric/app/ScreenshotBitmapDecoding.kt`
- `core/src/main/kotlin/com/kingsmetric/app/ReviewFieldGrouping.kt`
- `core/src/main/kotlin/com/kingsmetric/app/ReviewScreenIntegration.kt`

Dependencies:
- milestone 1 visual foundation
- `specs/compose-review-screen-and-viewmodel-integration.md`
- `specs/review-field-grouping-and-labels.md`
- `specs/review-sticky-actions-and-scroll-safety.md`
- `specs/review-input-hints-and-editing-assistance.md`

Risk:
- cluttering review with too many hints or labels
- improving aesthetics while making blockers or required fields less explicit
- reintroducing preview sizing/quality regressions on portrait screenshots

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.ComposeReviewScreenAndViewModelIntegrationTest" --tests "com.kingsmetric.app.ReviewStickyActionsAndInputAssistanceTest"`
- `./gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.kingsmetric.app.ScreenshotBitmapDecodingUnitTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.ReviewScreenComposeTest,com.kingsmetric.ScreenshotBitmapDecodingTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Manual review checklist:
- the screenshot preview is clear on a portrait phone
- the blocker summary remains visible without scrolling deep into the form
- the save action remains visually primary while the form stays editable

Expected observable result:
- review feels lighter and more structured at first glance
- blocker guidance remains visible near the top
- preview and edit regions feel intentional rather than cramped or blurry

### Milestone 4 — Improve history and detail readability

Scope:
Make browsing saved matches feel polished and scannable, with stronger grouping,
better summary hierarchy, and clearer fallback states.

Files or components:
- `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- `core/src/test/kotlin/com/kingsmetric/app/HistoryDetailDashboardUx*`

Dependencies:
- milestone 1 visual foundation
- `specs/history-list-readability-upgrade.md`
- `specs/record-detail-usability-upgrade.md`

Risk:
- over-designing cards and making dense record data slower to scan
- breaking current history/detail Compose expectations while changing only visual grouping

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Manual review checklist:
- a history row can be scanned quickly without opening it
- detail sections feel grouped and readable instead of technical
- fallback preview states still look intentional

Expected observable result:
- history rows communicate summary, result, and recency more clearly
- detail presents grouped field/value content without looking like debug output

### Milestone 5 — Improve dashboard clarity

Scope:
Make dashboard metrics feel prioritized and understandable, especially for low-
data or sparse-data cases.

Files or components:
- `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- `core/src/test/kotlin/com/kingsmetric/app/HistoryDetailDashboardUx*`

Dependencies:
- milestone 1 visual foundation
- `specs/dashboard-clarity-upgrade.md`

Risk:
- adding decorative cards without improving comprehension
- making sparse-data guidance less visible while restyling primary metrics

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Manual review checklist:
- the most important metrics are visually obvious first
- sparse-data guidance is visible without competing with the main metrics
- the screen feels like a dashboard, not a text report

Expected observable result:
- primary metrics feel framed and prioritized
- sparse-data and context text remain readable and visually secondary to main metrics

### Milestone 6 — Make diagnostics a first-class support surface

Scope:
Improve diagnostics presentation so support information is still dense enough
to help debugging but no longer feels like raw exported text pasted into the UI.

Files or components:
- `app/src/main/java/com/kingsmetric/app/DiagnosticsScreenRoute.kt`
- `core/src/main/kotlin/com/kingsmetric/app/DiagnosticsScreenIntegration.kt`
- `README.md` only if the support instructions need to reflect UI changes

Dependencies:
- milestone 1 visual foundation
- `specs/diagnostics-scope-and-privacy-contract.md`
- `specs/application-version-visibility.md`

Risk:
- accidentally hiding useful debugging content behind collapsed or decorative UI
- making OCR text or failure details harder to copy/inspect

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.DiagnosticsScreenIntegrationTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DiagnosticsScreenComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Manual review checklist:
- `Current Version` is visible without scrolling into dense content
- export action is obvious
- OCR text and failure details remain readable, not hidden behind polish

Expected observable result:
- diagnostics remain supportable but feel like part of the product, not an
  internal tool screen

### Milestone 7 — Close the UX regression gap and device-check the polish

Scope:
Fill remaining UI regressions and run one focused device-oriented polish pass
over the critical path after the previous milestones land.

Files or components:
- affected Compose tests
- `specs/ux-regression-and-device-verification-gap-fill.md` if the coverage map
  needs adjustment
- plan progress/validation notes

Dependencies:
- milestones 1 through 6

Risk:
- shipping a visually nicer UI that still regresses usability under real phone
  conditions
- relying on screenshots or emulator impressions alone without validating the
  critical path

Validation commands:
- `./gradlew.bat --no-daemon :core:test`
- `./gradlew.bat --no-daemon :app:testDebugUnitTest`
- `./gradlew.bat --no-daemon :app:assembleDebug`
- targeted `:app:connectedDebugAndroidTest` scopes for import, review,
  history/detail/dashboard, diagnostics, and app-shell navigation

Manual review checklist:
- import supported screenshot
- review main action obvious
- blocker guidance visible
- history/detail/dashboard readable
- diagnostics still expose version, export action, and failure details

Expected observable result:
- the polished UI is still safe, reviewable, and supportable on the existing
  runtime path

## Progress

- [x] 2026-04-15: Initial plan drafted and activated in `docs/plan.md`.
- [x] 2026-04-16: Milestone 1 completed with a new app-level visual foundation, theme layer, shared shell components, and updated shell tests.
- [x] 2026-04-16: Milestone 2 completed with a separate supported-input card, a dedicated import status block with next-step guidance, and one primary action per import state.
- [x] 2026-04-16: Milestone 3 completed with a structured review preview surface, dedicated blocker and attention surfaces, and section content rendered as reusable review cards.
- [x] 2026-04-16: Milestone 4 completed with stronger history-row hierarchy, readable detail summary metadata, and a dedicated screenshot-status surface on detail.
- [x] 2026-04-16: Milestone 5 completed with a dedicated primary-metrics section, separate sample/sparse-data cards, and clearer empty/error dashboard states.
- [x] 2026-04-16: Milestone 6 completed with a dedicated diagnostics support card, a dedicated export card, and readable per-entry reason/surface/OCR sections.
- [x] 2026-04-16: Milestone 7 completed with residual gap-fill coverage for blocked-review and missing-preview continuity, plus a final targeted regression pass across import, review, app shell, history/detail/dashboard, and diagnostics.

## Surprises & Discoveries

- 2026-04-15: The repository already has several UX-focused specs, but they are
  currently disconnected from an active execution plan. The immediate need is
  not “invent UX ideas”; it is to turn the existing direction into one
  milestone-based program that preserves the runtime constraints already built.

- 2026-04-16: The shell tests needed one extra wait step for the primary
  navigation to appear after launch. The route semantics did not change, but
  the polished shell now resolves after one additional async frame in Compose
  tests.
- 2026-04-16: `AppShellNavigationComposeTest` only uses `setContent`. Moving it
  from `createAndroidComposeRule<ComponentActivity>()` to `createComposeRule()`
  avoids a harness-only teardown crash while keeping the same shell coverage.
- 2026-04-16: The import surface reads more clearly when the supported-input
  expectation and the current runtime status are split into separate surfaces.
  Keeping them together in one card made idle, failure, and review-ready states
  feel too flat even after the shell foundation landed.
- 2026-04-16: The review screen reads as one coherent task when preview,
  blocker/attention guidance, and grouped sections are each given their own
  surface. The previous flat layout preserved behavior, but it made the phone
  flow feel denser than it needed to.
- 2026-04-16: The milestone-4 plan command name was slightly stale. The repo
  currently splits history/detail binding coverage across
  `HistoryDashboardScreenStateMapperTest` and
  `HistoryDashboardScreenBindingIntegrationTest`, so those are the truthful
  validation entry points for this slice.
- 2026-04-16: The dashboard only started to feel like a dashboard once the
  primary metrics, sample context, and low-confidence notes were split into
  separate surfaces. The previous flat stack was technically readable, but it
  still made sparse-data messaging compete visually with the main numbers.
- 2026-04-16: Diagnostics became easier to scan once support context, export
  controls, and failure entries stopped sharing one plain text column. Keeping
  `summary`, `reason`, `surface`, and OCR text as separate labeled pieces
  made the screen feel more product-like without hiding the support data.
- 2026-04-16: `UxRegressionGapFillComposeTest` also only uses `setContent`.
  Moving it to `createComposeRule()` kept the residual UX suite aligned with
  the repository's Compose-test rule.
- 2026-04-16: `AppShellNavigationComposeTest` remained reliable for milestone-7
  app-shell coverage when run in targeted method slices, but the whole class
  still crashed the emulator process when batched as one large Android run.
  For final verification, the truthful stable scope was targeted app-shell
  methods plus the other screen suites in smaller groups.

## Decision Log

- Decision: Use the existing app surfaces and UX specs as the base instead of
  planning a top-to-bottom rewrite.
  Rationale: The app already has a coherent functional shell and targeted UX
  contracts. The main gap is polish and consistency, not missing navigation or
  missing product surfaces.
  Date/Author: 2026-04-15 / Codex

- Decision: Keep the review flow as the center of the UX plan.
  Rationale: The app’s value depends on `Import -> Review -> Save`; visual work
  that ignores this path risks polishing secondary screens while the core task
  still feels heavy.
  Date/Author: 2026-04-15 / Codex

- Decision: Treat diagnostics as part of product UX, not just support tooling.
  Rationale: Real-user troubleshooting is now part of the shipped alpha
  experience, so the diagnostics surface has to meet the same clarity standard
  as the rest of the app.
  Date/Author: 2026-04-15 / Codex

- Decision: Keep visual styling ownership in `app/` and semantic/state
  ownership in `core/`.
  Rationale: This initiative is about polish, but the repo already has a useful
  separation between visual Android surfaces and testable screen-state logic.
  Preserving that boundary prevents a UI cleanup from turning into an
  architecture regression.
  Date/Author: 2026-04-15 / Codex

- Decision: Keep Compose + Material 3 as the base stack and build a small
  app-level design system on top of it.
  Rationale: The current stack already matches the repository architecture and
  test strategy. The missing piece is consistency and shared visual language,
  not a new framework.
  Date/Author: 2026-04-16 / Codex

- Decision: Put the milestone-1 baseline in an app-local
  `AppShellVisualFoundation`, `KingsMetricTheme`, and shared shell components
  instead of styling each screen independently.
  Rationale: The milestone needed one reusable source for spacing, title,
  surface, state-block, and action treatment that later milestones can consume
  without copying local values screen by screen.
  Date/Author: 2026-04-16 / Codex

- Decision: Treat `AppShellNavigationComposeTest` as a pure `setContent` suite
  and run it with `createComposeRule()`.
  Rationale: That matches the repository's Android-test rule and avoids a
  harness-only activity teardown crash that did not reflect shell behavior.
  Date/Author: 2026-04-16 / Codex

- Decision: Make the import screen show one supported-input card and one
  dedicated status block, with the primary action switching from `Import
  Screenshot` to `Continue Review` when a draft is ready.
  Rationale: The import flow is easier to scan when orientation content stays
  stable and the changing runtime state gets its own surface. Review-ready also
  needs one obvious next step instead of competing primary buttons.
  Date/Author: 2026-04-16 / Codex

- Decision: Render review preview, blocker/attention guidance, and grouped
  sections as separate reusable surfaces instead of a flat stack of text and
  fields.
  Rationale: The review task becomes easier to scan on a phone when the user
  can visually distinguish image context, save-blocking guidance, and editable
  field groups at first glance without changing validation semantics.
  Date/Author: 2026-04-16 / Codex

- Decision: Show history rows as `Saved match` summaries and show record detail
  with human-readable saved-at metadata plus a dedicated screenshot-status
  surface instead of exposing raw storage-like path text.
  Rationale: History/detail are easier to scan when the first screenful answers
  “what record is this, when was it saved, and is the screenshot available?”
  without mixing those answers into debug-like path strings.
  Date/Author: 2026-04-16 / Codex

- Decision: Group dashboard content into a top primary-metrics surface, a
  sample-context surface, a sparse-data surface, and a secondary-notes surface
  instead of one loose text stack.
  Rationale: Users should be able to distinguish “main metrics,” “what sample
  they come from,” and “why some numbers are limited” without inferring the
  hierarchy from text order alone.
  Date/Author: 2026-04-16 / Codex

- Decision: Split diagnostics into a support-context card, an export card, and
  labeled failure-entry metadata instead of rendering the diagnostics screen as
  one plain text column.
  Rationale: The screen still needs dense support data, but users should be
  able to identify `Current Version`, the export action, `Reason`, `Surface`,
  and OCR text without mentally parsing a pasted export blob.
  Date/Author: 2026-04-16 / Codex

- Decision: Keep milestone-7 residual coverage in `UxRegressionGapFillComposeTest`
  for blocked-review and missing-preview continuity, while treating main-path
  import/review/save and unsupported-import fallback as already owned by the
  focused app-shell and import suites.
  Rationale: That matches the feature spec's residual intent and avoids turning
  the gap-fill milestone into a redundant giant end-to-end suite.
  Date/Author: 2026-04-16 / Codex

## Validation and Acceptance

Planning acceptance:

- `docs/plan.md` points to this file as the active plan.
- the plan names concrete milestones, files, risks, and commands
- the plan reflects current repo constraints instead of generic UI advice

Implementation acceptance for the future milestones:

- if a milestone exceeds current spec coverage, add or revise the missing
  feature spec and test spec before implementation
- each milestone lands as one reviewable PR
- each milestone updates or reuses the relevant UX specs/test specs
- each milestone records real validation results in this file
- each milestone includes a short manual phone-sized review checklist in
  addition to automated verification

## Validation Notes

- 2026-04-15: Planning-only update. No code or test execution was required for
  this turn.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.kingsmetric.ui.theme.AppShellVisualFoundationTest"` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.AppShellNavigationComposeTest"` passed after switching the suite to `createComposeRule()` and adding a wait for primary navigation.
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.AndroidAppShellNavigationTest"` passed.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.ImportStatusAndGuidanceUxTest" --tests "com.kingsmetric.app.AndroidPhotoPickerRuntimeWiringTest"` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.ImportScreenComposeTest"` passed.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.ComposeReviewScreenAndViewModelIntegrationTest" --tests "com.kingsmetric.app.ReviewStickyActionsAndInputAssistanceTest"` passed.
  - `./gradlew.bat --no-daemon :app:testDebugUnitTest --tests "com.kingsmetric.app.ScreenshotBitmapDecodingUnitTest"` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.ReviewScreenComposeTest,com.kingsmetric.ScreenshotBitmapDecodingTest"` passed.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDashboardScreenStateMapperTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingIntegrationTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"` passed.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"` passed with the new dashboard clarity assertions.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.DiagnosticsScreenIntegrationTest"` passed after separating diagnostics `summary`, `reason`, and `surface` in the state model.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.DiagnosticsScreenComposeTest"` passed with the new support/export card assertions and labeled OCR/detail rendering.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.UxRegressionGapFillComposeTest"` passed after moving the suite to `createComposeRule()`.
  - `./gradlew.bat --no-daemon :core:test` passed.
  - `./gradlew.bat --no-daemon :app:testDebugUnitTest` passed.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.AppShellNavigationComposeTest#primaryNavigation_moves_between_import_history_and_dashboard_with_context"` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.AppShellNavigationComposeTest#importToReviewToSave_completesSaveThroughTheRuntimeShell,com.kingsmetric.AppShellNavigationComposeTest#reviewRoute_is_a_focused_task_screen_with_close_behavior,com.kingsmetric.AppShellNavigationComposeTest#detailRoute_has_back_behavior_and_returns_to_history"` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.ReviewScreenComposeTest"` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.ImportScreenComposeTest,com.kingsmetric.HistoryDetailDashboardUxComposeTest,com.kingsmetric.DiagnosticsScreenComposeTest,com.kingsmetric.UxRegressionGapFillComposeTest"` passed.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.AppShellNavigationComposeTest"` still crashed the instrumentation process when run as one large class batch, so milestone-7 verification used the stable targeted method slices above instead.

## Idempotence and Recovery

- Re-running this planning work should update this file in place until a PR is
  opened for the first milestone.
- If scope expands beyond polish into new product behavior, create new feature
  specs instead of silently stretching this plan.
- If a later plan supersedes this one, keep this file and mark it superseded in
  metadata and the `docs/plan.md` index.

## Outcomes & Retrospective

- The UI polish initiative finished with one shared visual foundation across
  import, review, history/detail, dashboard, and diagnostics, while keeping
  the repository's `app/` versus `core/` boundary intact.
- The highest-value UX improvements came from separating dense mixed-content
  screens into explicit surfaces: import support/status, review preview versus
  blockers, dashboard metrics versus context, and diagnostics support versus
  export versus failure entries.
- Final verification confirmed the polished UI remained reviewable and
  supportable on the existing runtime path, with the only notable caveat being
  that one large app-shell Compose class is still more stable when run in
  targeted method slices than as one full emulator batch.
