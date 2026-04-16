# Expose more useful saved-match detail on the history page

- Status: active
- Created: 2026-04-16
- Updated: 2026-04-16
- Owner: Codex
- Related spec(s): `specs/history-list-readability-upgrade.md`, `specs/record-detail-usability-upgrade.md`, `specs/history-page-detail-expansion.md`
- Supersedes / Superseded by: none
- Branch / PR: `feature/history-page-detail-expansion` / draft PR pending
- Last verified commands:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.history.MatchHistoryUiTest" --tests "com.kingsmetric.data.local.RoomRepositoriesObservedIntegrationTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`

## Purpose / Big picture

The history page currently tells the user that a saved match exists, but it
does not expose enough match context for the user to choose the right record
quickly. Today each row is effectively:

- hero
- result
- saved date
- preview availability

That is readable, but it still forces the user into record detail too often.

This initiative adds a bounded quick-summary layer to the history page so the
user can understand more of a saved match before tapping into detail. Success
is visible when a user can browse the history list and answer "which saved
match is this?" more often without opening the detail screen, while the list
still feels scannable on a phone and does not turn into a full inline detail
view.

## Context and orientation

The current history UI and data shaping live in:

- `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- `core/src/main/kotlin/com/kingsmetric/history/MatchHistory.kt`
- `core/src/main/kotlin/com/kingsmetric/data/local/RoomObservedMatchRepository.kt`

The current history list row model is intentionally sparse:

- `MatchHistoryListItem` currently carries only `recordId`, `savedAt`, `hero`,
  `result`, and `screenshotAvailable`
- `HistoryRowUiState` currently exposes only category, primary text, result,
  recency, preview status, and selection state
- `HistoryScreen` renders one simple card per saved record using that state

The current data store is not the limiting factor. `SavedMatchEntity` already
stores richer saved-record fields such as:

- `lane`
- `score`
- `kda`
- `participationRate`
- `damageShare`
- `goldShare`

But `observeHistory()` currently discards most of that information before it
reaches the list UI.

Existing contracts are close but not sufficient:

- `specs/history-list-readability-upgrade.md` requires scanability and graceful
  fallback, but it does not define a richer in-row quick-summary contract
- `specs/record-detail-usability-upgrade.md` still assumes full grouped detail
  belongs on the detail screen, not in the list

Existing tests already pin the current sparse behavior:

- `core/src/test/kotlin/com/kingsmetric/app/HistoryDetailDashboardUxTest.kt`
- `core/src/test/kotlin/com/kingsmetric/app/HistoryDashboardScreenBindingTest.kt`
- `core/src/test/kotlin/com/kingsmetric/history/MatchHistoryUiTest.kt`
- `core/src/test/kotlin/com/kingsmetric/data/local/RoomRepositoriesObservedIntegrationTest.kt`
- `app/src/androidTest/java/com/kingsmetric/HistoryDetailDashboardUxComposeTest.kt`

This means the next safe step is not implementation-first. It is to define a
new feature contract for how much detail belongs on the history page, in what
order, and with what fallback behavior.

## Constraints

- The history page must only show values that are already saved in the local
  record. No new OCR, inference, or derived "best guess" data is allowed.
- Keep the page local-first and offline. No sync, no server fetch, and no
  support upload flow is part of this work.
- Preserve phone-sized scanability. The history page should reveal more detail,
  not become a dense mini-detail screen.
- Record selection must remain obvious and stable. A richer card must still
  read as one tappable saved match.
- Missing or empty saved fields must degrade gracefully instead of making the
  row look broken.
- Detail remains the full-fidelity surface for grouped field data and preview
  status. This initiative improves the list, not by deleting or replacing the
  detail route.
- Prefer using already-saved Room columns over schema changes. If the desired
  quick-summary set can be satisfied from existing fields, do not widen the
  database schema.
- Avoid per-row heavy image work. The history page should not fully decode
  screenshots just to look more detailed.
- Explicit non-goals:
  - inline editing on the history page
  - list search, filtering, or sort customization
  - expandable accordion-style full detail inside each row
  - screenshot gallery behavior in history rows
  - schema redesign unless a later approved spec proves it is required

## Done when

This initiative is done when all of the following are true:

- the history page shows a bounded quick-summary set that gives more match
  context than `hero + result + recency` alone
- the approved quick-summary fields are visible, bounded, and still readable
  inside one phone-sized history card without obscuring that the row is one
  tappable saved match
- the quick-summary contract is explicitly defined in a feature spec and test
  spec before implementation
- richer history rows are built from existing saved-record data without
  inventing fields
- missing quick-summary fields still degrade gracefully and keep rows readable
- record selection and navigation to detail remain stable
- targeted JVM, repository, and Compose coverage pass for the richer-history
  contract
- a manual phone-sized review confirms the list is more informative without
  becoming visually noisy or hard to tap

## Milestones

### Milestone 1 - Define the richer history-row contract

Scope:
Create a feature spec and matching test spec for "more detail on the history
page." The contract should pin:

- which additional saved-match details belong directly in the history row
- the ordering and grouping of those details
- the exact maximum quick-summary field set and visible density per row
- fallback behavior when one or more quick-summary fields are empty
- what stays exclusive to the detail screen

Files or components:

- `specs/history-page-detail-expansion.md`
- `specs/history-page-detail-expansion.test.md`
- optional narrow updates to:
  - `specs/history-list-readability-upgrade.md`
  - `specs/record-detail-usability-upgrade.md`

Dependencies:

- this plan
- current history/detail specs

Risk:

- "more details" stays too vague and turns into scope creep
- the spec accidentally turns the history page into an inline detail screen

Validation commands:

- planning/spec-only milestone; no Gradle commands required

Expected observable result:

- the repository has an approved contract for a bounded richer-history row
- the spec defines the exact quick-summary field set, ordering, and row-density
  guardrails for later implementation
- future implementation work no longer depends on ad hoc UX interpretation

### Milestone 2 - Expand history data shaping for richer summaries

Scope:
Carry the approved quick-summary fields from saved-record storage into the
history list state without changing save semantics.

Files or components:

- `core/src/main/kotlin/com/kingsmetric/history/MatchHistory.kt`
- `core/src/main/kotlin/com/kingsmetric/data/local/RoomObservedMatchRepository.kt`
- `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- `core/src/test/kotlin/com/kingsmetric/history/MatchHistoryUiTest.kt`
- `core/src/test/kotlin/com/kingsmetric/data/local/RoomRepositoriesObservedIntegrationTest.kt`
- `core/src/test/kotlin/com/kingsmetric/app/HistoryDashboardScreenBindingTest.kt`
- `core/src/test/kotlin/com/kingsmetric/app/HistoryDetailDashboardUxTest.kt`

Dependencies:

- milestone 1 approved contract
- existing saved fields in `SavedMatchEntity`
- alignment between:
  - `core/src/main/kotlin/com/kingsmetric/history/MatchHistory.kt`
  - `core/src/main/kotlin/com/kingsmetric/data/local/RoomObservedMatchRepository.kt`

Risk:

- widening the row model in a way that leaks too many raw fields into the UI
- accidentally requiring a schema change when existing saved fields are enough
- breaking current repository and binder tests while reshaping the row model

Validation commands:

- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.history.MatchHistoryUiTest" --tests "com.kingsmetric.data.local.RoomRepositoriesObservedIntegrationTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`

Expected observable result:

- history row state contains the approved richer quick-summary information
- repository and binder layers expose that information with explicit fallbacks
- both history-shaping paths expose the same richer row contract instead of
  drifting into separate field sets or fallback rules

### Milestone 3 - Render richer history cards without losing scanability

Scope:
Update the Compose history screen to render the approved quick-summary set in a
clear, phone-friendly structure while keeping each row obviously tappable.

Files or components:

- `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- `app/src/androidTest/java/com/kingsmetric/HistoryDetailDashboardUxComposeTest.kt`
- optional targeted updates to shared shell components in:
  - `app/src/main/java/com/kingsmetric/ui/components/`

Dependencies:

- milestone 2 richer row state
- current shell visual foundation from the completed UI polish initiative

Risk:

- dense cards become slower to scan than the current sparse rows
- row styling makes tap/select behavior less obvious
- fallback states become visually noisy when optional details are missing
- richer rows become hard to read on narrow phones or under larger text sizes

Validation commands:

- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Expected observable result:

- each history row shows more useful saved-match detail at first glance
- the list still feels like a list of saved matches, not a stack of full
  record dumps

### Milestone 4 - Verify history-to-detail continuity and polish pass

Scope:
Close any remaining UX gaps after the richer row contract lands, then run the
final phone-sized verification for the new history experience.

Files or components:

- affected history/detail tests from milestones 2 and 3
- `docs/plans/2026-04-16-history-page-detail-expansion.md`
- `docs/workflows.md` only if a new durable history/detail workflow lesson is
  discovered during implementation

Dependencies:

- milestones 2 and 3

Risk:

- richer history rows look better in isolation but still fail the real browse
  flow on a phone
- navigation or fallback continuity regresses while the row content expands

Validation commands:

- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.history.MatchHistoryUiTest" --tests "com.kingsmetric.data.local.RoomRepositoriesObservedIntegrationTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest,com.kingsmetric.AppShellNavigationComposeTest#detailRoute_has_back_behavior_and_returns_to_history"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Manual review checklist:

- adjacent saved matches can be distinguished without opening detail
- one history row still reads as one tappable record instead of several loose
  text fragments
- missing optional quick-summary fields do not make the row look broken
- open detail and return to history without losing orientation
- the richer row remains readable on a narrow phone layout

Expected observable result:

- the user can browse richer history rows, open detail, and return to history
  without confusion
- the final history experience is more informative while staying calm and
  tappable on a phone-sized screen

## Progress

- [x] 2026-04-16: Reviewed the current history/detail specs, history data
  models, repository mapping, binder state, UI rendering, and existing tests.
- [x] 2026-04-16: Drafted and activated this plan in `docs/plan.md`.
- [x] 2026-04-16: Milestone 1 completed with
  `specs/history-page-detail-expansion.md` and
  `specs/history-page-detail-expansion.test.md`.
- [x] 2026-04-16: Milestone 2 completed by expanding both history-shaping
  paths and `HistoryRowUiState` to carry the approved `result/lane/KDA/score`
  quick-summary contract from saved data into row state.
- [x] 2026-04-16: Milestone 3 completed by rendering ordered quick-summary
  items in the history row as a wrapped summary surface above recency and
  preview metadata, while keeping each row as one tappable card.
- [ ] Milestone 4: Verify history-to-detail continuity and final polish.
  Done: targeted JVM/history Compose/detail-route continuity verification
  passed with no additional code changes required. Remaining: manual
  phone-sized checklist from this plan.

## Surprises & Discoveries

- 2026-04-16: The current history-page limitation is not a Room or persistence
  limitation. `SavedMatchEntity` already stores several candidate fields for a
  richer quick summary, but `observeHistory()` and `MatchHistoryListItem`
  intentionally discard them before the list UI.
- 2026-04-16: The existing history spec only guarantees scanability and
  graceful fallback. It does not define which "more details" should appear in
  the row, so this request needs a new spec before implementation.
- 2026-04-16: The current history/detail Compose and JVM tests are stable entry
  points for this work, which means the initiative can stay incremental rather
  than reopening the whole UI polish plan.
- 2026-04-16: Existing saved fields were sufficient for milestone 2. `lane`,
  `score`, and `KDA` were already present in both `SavedMatchEntity` and saved
  history records, so the richer quick-summary contract did not require any
  persistence or schema work.
- 2026-04-16: The richer history summary tags were present in the UI but not
  stable in the default merged semantics tree. The correct Compose-test fix was
  to assert those summary tags with `useUnmergedTree = true` instead of adding
  test-only UI semantics or flattening the summary layout.
- 2026-04-16: The milestone-4 automated continuity pass did not reveal any
  additional code gap after the richer-row rendering landed. History rows,
  history/detail UI, and the detail-route back behavior all held without
  further implementation changes.

## Decision Log

- Decision: Treat this as a new initiative rather than reopening the completed
  UI polish plan.
  Rationale: The old plan is closed, and this request changes history-page
  behavior rather than continuing the broad polish milestone stream.
  Date/Author: 2026-04-16 / Codex

- Decision: Start with a new feature spec instead of implementing directly
  against the old history readability contract.
  Rationale: "User needs to see more details" is a behavior change, and the
  current history spec does not define the bounded quick-summary set.
  Date/Author: 2026-04-16 / Codex

- Decision: Prefer enriching history rows from existing saved fields before
  considering schema changes.
  Rationale: The repository already stores candidate summary fields such as
  lane, score, and KDA. That is the lowest-risk path for exposing more detail.
  Date/Author: 2026-04-16 / Codex

- Decision: Treat schema expansion as out of scope for this initiative unless
  the approved quick-summary contract cannot be satisfied from current saved
  fields.
  Rationale: The safest path is to expose already-saved data first. If that
  proves insufficient, the persistence impact should be reviewed as a separate
  approved plan instead of being stretched into milestone 2.
  Date/Author: 2026-04-16 / Codex

- Decision: Keep the history page bounded and scannable instead of turning it
  into an inline detail surface.
  Rationale: The user asked for more details on the history page, not for the
  removal of the detail route or a dense field dump in the list.
  Date/Author: 2026-04-16 / Codex

- Decision: Carry the richer row contract through state as an ordered
  `quickSummaryItems` list in `HistoryRowUiState`.
  Rationale: That keeps the approved `result/lane/KDA/score` order explicit in
  one place, avoids ad hoc top-level row fields for every summary item, and
  leaves milestone 3 free to change rendering without reinterpreting the data
  contract.
  Date/Author: 2026-04-16 / Codex

- Decision: Render quick-summary items in their own wrapped summary region
  above recency and preview metadata.
  Rationale: That keeps the approved summary set visually separable from
  secondary metadata, allows the row to stay phone-friendly without horizontal
  scrolling, and preserves one obvious tap target for the whole saved match.
  Date/Author: 2026-04-16 / Codex

## Validation and Acceptance

Planning acceptance:

- `docs/plan.md` points to this file as the active plan
- the plan is self-contained and names the current history-page limitation
- the plan identifies the spec gap before implementation
- milestones are small enough for reviewable PRs

Implementation acceptance for the future milestones:

- milestone 1 creates a spec and test spec that explicitly define the richer
  history-row contract, including exact field set, ordering, and density
  limits
- milestone 2 updates state shaping without inventing or inferring data
- milestone 3 keeps richer rows readable and obviously selectable
- milestone 4 verifies history-to-detail continuity and phone-sized usability

## Validation Notes

- 2026-04-16: Planning-only update. No code or test execution was required for
  this turn.
- 2026-04-16: Added `specs/history-page-detail-expansion.md`. No code or test
  execution was required for this spec-writing turn.
- 2026-04-16: Added `specs/history-page-detail-expansion.test.md`. No code or
  test execution was required for this test-spec turn.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.history.MatchHistoryUiTest" --tests "com.kingsmetric.data.local.RoomRepositoriesObservedIntegrationTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"` passed after adding richer history-row fields and ordered `quickSummaryItems` to both history-shaping paths.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed to confirm the app module still compiled against the widened history row state.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"` passed after rendering quick-summary items as a wrapped summary region and updating the summary-tag assertions to use the unmerged semantics tree.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed on the post-UI-rendering state.
- 2026-04-16:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.history.MatchHistoryUiTest" --tests "com.kingsmetric.data.local.RoomRepositoriesObservedIntegrationTest" --tests "com.kingsmetric.app.HistoryDashboardScreenBindingTest" --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"` passed again during milestone-4 continuity verification.
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest,com.kingsmetric.AppShellNavigationComposeTest#detailRoute_has_back_behavior_and_returns_to_history"` passed.
  - `./gradlew.bat --no-daemon :app:assembleDebug` passed.
  - Manual phone-sized checklist remains pending; this plan is not yet marked completed.

## Idempotence and Recovery

- Re-running this planning task should update this plan in place until
  implementation begins.
- If the eventual spec decides the user actually wants expandable inline
  detail, stop and review scope before implementation. That would materially
  change the risk and milestone shape.
- If implementation reveals that the approved quick-summary set cannot be
  satisfied from current saved fields, stop this initiative and decide
  explicitly whether the spec should narrow or a separate persistence/schema
  plan is needed. Do not widen milestone 2 ad hoc.
- If a later plan supersedes this work, keep this file and mark it as
  superseded in both metadata and `docs/plan.md`.

## Outcomes & Retrospective

- Not started yet. Expected outcome is a history page that communicates more of
  each saved match directly in the list while preserving current detail-route
  safety and scanability.
