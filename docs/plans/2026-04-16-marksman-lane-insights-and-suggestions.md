# Marksman Lane Insights And Suggestions

## Metadata

- Status: completed
- Created: 2026-04-16
- Updated: 2026-04-16
- Owner: Codex
- Related spec(s):
  - [metrics-dashboard](../../specs/metrics-dashboard.md)
  - [history-and-dashboard-screen-binding](../../specs/history-and-dashboard-screen-binding.md)
  - [dashboard-clarity-upgrade](../../specs/dashboard-clarity-upgrade.md)
  - [record-detail-usability-upgrade](../../specs/record-detail-usability-upgrade.md)
- Supersedes / Superseded by: none
- Branch / PR: TBD
- Last verified commands:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.marksman.MarksmanLaneAnalysisInputTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.marksman.MarksmanLaneDetailedMetricsTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.marksman.MarksmanLaneSuggestionsTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`
  - `./gradlew.bat --no-daemon :core:test --tests "*Marksman*"`

## Purpose / Big picture

After the app extracts and saves one match, the user wants more than raw fields.
They want detailed metrics and useful suggestions that explain the match in
marksman-lane terms.

This initiative creates the base plan for marksman lane first. It is not a
hero-specific handbook yet, and it is not a generic AI tips feature. It is a
local, deterministic, role-specific analysis layer grounded in the marksman
playbook source the user provided.

Success means:

- a saved marksman-lane match can produce richer role-specific metrics than the
  current raw grouped record-detail fields alone
- the app can show useful, bounded suggestions that are traceable to explicit
  marksman-lane rules
- the app stays honest about what the current post-match screenshot data can
  and cannot prove
- the marksman-lane foundation is reusable later for hero-specific handbooks
  and other lanes without pretending that work already exists

## Context and orientation

The current app already has the raw data needed for a first role-analysis pass,
but not the role-analysis layer itself.

Current data and surfaces:

- post-match data is saved locally from one supported screenshot template
- saved fields already include:
  - `lane`
  - `score`
  - `kda`
  - `damageDealt`
  - `damageShare`
  - `damageTaken`
  - `damageTakenShare`
  - `totalGold`
  - `goldShare`
  - `participationRate`
  - `goldFromFarming`
  - `lastHits`
  - `killParticipationCount`
  - `controlDuration`
  - `damageDealtToOpponents`
- grouped raw record detail already exists in:
  - `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
  - `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- aggregate dashboard metrics currently live in:
  - `core/src/main/kotlin/com/kingsmetric/dashboard/MetricsDashboard.kt`

Current limitation:

- the detail screen shows raw grouped stats, but it does not interpret them in
  role terms
- the dashboard only computes win rate, average KDA, hero usage, and recent
  performance
- the current dashboard spec explicitly lists coaching features as a non-goal
  in `specs/metrics-dashboard.md`

That means this work must start as a new approved feature contract. It must not
silently stretch the old dashboard metric spec.

External design source:

- the user provided `xiongxianfei/wzry-marksman-playbook`
- its reusable role-level guidance comes primarily from:
  - `核心原则.md`
  - `通用技巧.md`

Those source documents emphasize repeatable marksman-lane rules such as:

- clear lane first and keep economy rhythm
- avoid isolated fights and follow the strongest teammate, usually the jungler
- treat low health as lost initiative
- fight front-to-back instead of from multi-angle danger
- convert kills into towers and map pressure
- group to expand advantage instead of continuing isolated farm
- use objective/resource decisions only when health, numbers, and enemy-core
  visibility are favorable

Important constraint discovered during planning:

- many playbook rules are timing-sensitive or event-sensitive
- the current app only stores static post-match fields from one screenshot
- the current app does **not** store minute-by-minute economy, objective
  history, tower events, vision events, rotation paths, or pre-fight decision
  states

So the base plan must explicitly separate:

- rules that are directly observable from current saved match data
- rules that are only partially inferable through bounded proxies
- rules that are not observable yet and require future telemetry/schema work

## Constraints

- Marksman lane first. The base plan applies only to matches that can be
  classified as marksman-lane matches from saved local data.
- Keep the feature local-first and deterministic. No server analytics, cloud
  sync, generative AI, or opaque recommendation engine.
- Every surfaced suggestion must be traceable to:
  - a bounded metric trigger from saved data
  - a named marksman-lane rule category from the approved feature spec
- Do not invent early-game timings, objective calls, vision behavior, or tower
  conversion events that the current saved screenshot data cannot prove.
- Prefer using the fields already saved in local records before considering OCR
  expansion or schema changes.
- If a desired playbook rule cannot be supported by current saved data, stop
  and sequence that into a later telemetry/schema initiative instead of
  hand-waving it into the first implementation.
- Keep role-analysis logic in testable core Kotlin code, not in Compose leaf
  UI.
- Start with per-match analysis first because the user asked for guidance after
  getting one match's data. Aggregate role analytics can come after the
  single-match foundation is real.
- Non-goals for this base plan:
  - hero-specific playbooks and build guides
  - non-marksman lanes
  - in-match/live coaching
  - automated "best play" reconstruction
  - timeline-dependent coaching that current saved data cannot justify
  - silently overriding existing dashboard behavior without a new feature spec

## Done when

This initiative is done when all of the following are true:

- a new feature spec and matching test spec define the marksman-lane analysis
  contract
- the contract includes an explicit observability map from playbook rules to
  current saved-match data
- the app can identify whether a saved match is eligible for marksman-lane
  analysis or should degrade explicitly
- the first release of marksman-lane analysis defines a bounded detailed-metric
  set instead of a vague “more data” promise
- the first release of suggestions defines a bounded suggestion set with
  explainable triggers instead of generic tips
- the first user-facing surface for this work is the per-match detail flow
  after data has already been extracted and saved
- optional aggregate marksman-lane profile work is clearly sequenced after the
  per-match foundation, not mixed into it prematurely
- focused JVM, repository, and Compose verification for the approved scope are
  named and later pass

## Milestones

### Milestone 1 - Define the marksman-lane contract and observability map

Scope:
- create the new feature spec and matching test spec for marksman-lane detailed
  metrics and useful suggestions
- define what counts as a marksman-lane eligible match
- define the first-release surfaces:
  - per-match detail first
  - aggregate profile/dashboard only if still justified after the per-match
    contract is stable
- define the first-release metric categories
- define the first-release suggestion categories
- translate the external playbook into a stable local contract rather than
  implementing against a floating upstream repo
- add an explicit rule map with three buckets:
  - directly observable now
  - proxy-observable now
  - not observable from current saved data

Files or components:
- `specs/marksman-lane-insights-and-suggestions.md`
- `specs/marksman-lane-insights-and-suggestions.test.md`
- optional narrow updates to:
  - `specs/metrics-dashboard.md`
  - `specs/record-detail-usability-upgrade.md`
  - `specs/dashboard-clarity-upgrade.md`

Dependencies:
- this plan
- the user-provided marksman playbook source
- current dashboard/detail contracts

Risk:
- the spec promises coaching that current saved data cannot support
- the work becomes “all marksman knowledge” instead of a bounded first feature
- the plan drifts into hero-specific rules before role foundation exists

Validation commands:
- planning/spec-only milestone; no Gradle commands required

Expected observable result:
- the repository has an approved local contract for marksman-lane metrics and
  suggestions
- the contract explicitly states which playbook rules are in scope now and
  which are deferred

### Milestone 2 - Audit current saved inputs and define the analysis model

Scope:
- add a pure core analysis-input layer for marksman-lane matches
- define role eligibility and data-sufficiency checks from existing saved
  record fields
- keep repository/controller shaping aligned so the same saved fields drive the
  same marksman analysis model across the app

Files or components:
- new core files under a bounded marksman-analysis package, likely beneath:
  - `core/src/main/kotlin/com/kingsmetric/`
- existing saved-record types and repository projections when needed:
  - `core/src/main/kotlin/com/kingsmetric/data/local/RoomObservedMatchRepository.kt`
  - `core/src/main/kotlin/com/kingsmetric/history/`
- focused JVM tests for role eligibility and data sufficiency

Dependencies:
- milestone 1 approved contract
- current `SavedMatchEntity` / saved-record field set

Risk:
- the analysis layer quietly depends on fields that are not always saved
- role eligibility becomes ambiguous or too clever
- per-surface shaping drifts into inconsistent criteria

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "*Marksman*"`

Expected observable result:
- the app has one explicit marksman-lane analysis input model
- eligible, insufficient, and unsupported matches are distinguishable in pure
  tested Kotlin logic

### Milestone 3 - Compute bounded detailed metrics for one marksman match

Scope:
- implement the first release of detailed marksman-lane metrics for a single
  saved match
- keep the metric set bounded to categories supported by current saved data,
  such as:
  - economy and farming
  - output and pressure
  - survival and exposure
  - teamfight participation
  - overall match context
- make missing inputs degrade explicitly instead of fabricating a score

Files or components:
- new pure metric-calculation files in core
- focused core tests
- optional narrow binder/state updates if the metric model needs display-ready
  shaping outside UI

Dependencies:
- milestone 2 analysis model

Risk:
- “detailed metrics” turns into a giant opaque score
- weak data is turned into fake precision
- per-match metrics are mixed with multi-match aggregates too early

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "*Marksman*"`

Expected observable result:
- a saved marksman-lane match can produce a bounded role-specific metric set
- missing inputs remain visible as partial/insufficient, not silently filled in

### Milestone 4 - Add deterministic rule-based suggestions

Scope:
- implement the first release of useful suggestions for one marksman match
- suggestions must be action-oriented, bounded, and traceable to playbook rule
  categories plus current metric evidence
- define a stable suggestion payload such as:
  - suggestion title
  - short rationale
  - triggering metrics or insufficiency reason
  - playbook rule category/source label
- keep the base release focused on categories current data can support, such as:
  - economy rhythm
  - risk discipline / survivability
  - follow-team vs isolation
  - front-to-back positioning / output conversion

Files or components:
- new core suggestion-engine files
- focused JVM tests

Dependencies:
- milestone 3 metrics
- milestone 1 suggestion contract

Risk:
- advice becomes generic filler instead of evidence-based guidance
- too many suggestions appear at once and become noisy
- unobservable macro rules are disguised as certainty

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "*Marksman*"`

Expected observable result:
- one saved marksman-lane match can produce a small, explainable suggestion set
- the app can also explain when there is not enough evidence for a suggestion

### Milestone 5 - Surface marksman analysis in the per-match detail flow

Scope:
- add the first user-facing marksman-lane insights surface to the saved-record
  detail flow
- keep the current raw grouped stats accessible while adding a higher-level
  role-analysis layer above or alongside them
- make eligibility, insufficiency, and suggestions readable on a phone-sized
  screen

Files or components:
- `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`
- detail-related Compose tests
- optional shared UI components if the new insight/suggestion sections need a
  reusable treatment

Dependencies:
- milestones 2-4
- existing detail usability contract

Risk:
- the detail screen becomes denser but not more helpful
- the analysis layer visually obscures the raw metrics that still matter
- unsupported or insufficient cases look like app failure instead of honest
  limitation

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Expected observable result:
- after saving and opening a marksman-lane match, the user can see:
  - detailed role-specific metrics
  - useful suggestions
  - honest limitation messaging when analysis is partial or unavailable

### Milestone 6 - Add an optional aggregate marksman-lane profile

Scope:
- only after the per-match foundation is real, decide whether to extend the
  dashboard with a bounded aggregate marksman-lane profile
- if justified, compute role-scoped multi-match trends and suggestions from
  marksman-lane eligible saved matches only
- keep this milestone optional until milestone 5 proves the single-match model
  is stable and useful

Files or components:
- `core/src/main/kotlin/com/kingsmetric/dashboard/MetricsDashboard.kt`
- `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
- dashboard-related Compose tests
- dashboard specs if the contract changes

Dependencies:
- milestones 2-5
- explicit spec approval if the old dashboard contract must change

Risk:
- the aggregate dashboard is expanded before the per-match model is trustworthy
- role-scoped coaching silently conflicts with the old dashboard spec
- the screen becomes crowded and stops being readable

Validation commands:
- `./gradlew.bat --no-daemon :core:test --tests "*Dashboard*" --tests "*Marksman*"`
- `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest,com.kingsmetric.UxRegressionGapFillComposeTest"`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Expected observable result:
- if implemented, the dashboard can summarize marksman-lane trends without
  diluting the per-match detail contract

### Milestone 7 - Verification, edge cases, and release-readiness polish

Scope:
- close the remaining UX and data-quality gaps for marksman-lane analysis
- verify unsupported/non-marksman matches, sparse data, and partial saved-field
  cases
- do a phone-sized review pass for the detail surface and any aggregate surface
  that exists

Files or components:
- affected tests from milestones 2-6
- `docs/plans/2026-04-16-marksman-lane-insights-and-suggestions.md`
- `docs/workflows.md` only if durable workflow guidance changes

Dependencies:
- previous milestones

Risk:
- suggestion quality looks right in happy-path tests but breaks on sparse or
  mixed-quality saved records
- the UI overclaims confidence for a role analysis based on partial data

Validation commands:
- focused JVM scopes for marksman metrics and suggestions
- focused Compose scopes for detail/dashboard presentation
- `./gradlew.bat --no-daemon :app:assembleDebug`

Expected observable result:
- marksman-lane analysis behaves honestly across eligible, partial, and
  unsupported cases
- release-facing verification is explicit instead of implied

## Progress

- [x] 2026-04-16: Reviewed the current dashboard/detail contracts and confirmed
  that coaching features are outside the old dashboard spec.
- [x] 2026-04-16: Read the user-provided marksman playbook source and extracted
  the reusable role-level rule categories from `核心原则.md` and `通用技巧.md`.
- [x] 2026-04-16: Confirmed the current app already saves enough post-match
  fields to support a bounded first phase of role analysis without changing the
  OCR template.
- [x] 2026-04-16: Identified the main planning boundary: many playbook rules
  need timeline/objective/vision data that the current static screenshot model
  does not capture.
- [x] 2026-04-16: Created this base plan for marksman-lane detailed metrics and
  suggestions.
- [x] 2026-04-16: Added the feature spec and locked the first release to
  per-match detail analysis, with explicit direct, proxy, and deferred
  observability buckets.
- [x] 2026-04-16: Added the matching test spec with explicit coverage for
  eligibility states, bounded suggestions, partial-data behavior, and the
  unchanged-dashboard boundary.
- [x] 2026-04-16: Completed milestone 2 with a shared marksman-lane analysis
  input factory for both `SavedMatchHistoryRecord` and `SavedMatchEntity`.
- [x] 2026-04-16: Added focused JVM coverage for marksman-lane eligibility,
  insufficiency, and metric-group coverage from existing saved fields.
- [x] 2026-04-16: Completed milestone 3 with bounded field-backed detailed
  metric groups for one eligible marksman-lane match.
- [x] 2026-04-16: Added focused JVM coverage for full metric-group ordering,
  partial-group degradation, and the no-fabrication boundary.
- [x] 2026-04-16: Completed milestone 4 with deterministic suggestions across
  the approved first-release categories.
- [x] 2026-04-16: Added focused JVM coverage for deterministic suggestion
  output, bounded visible count, missing-evidence gating, and the neutral
  no-high-priority state.
- [x] 2026-04-16: Completed milestone 5 by surfacing marksman-lane insights in
  saved-match detail while keeping the raw grouped record detail visible below
  the new role-analysis layer.
- [x] 2026-04-16: Added focused JVM and Compose coverage for eligible,
  unavailable-for-this-lane, insufficient-data, neutral-suggestion, and
  fail-closed analysis states in the record-detail flow.
- [x] 2026-04-16: Kept milestone 6 explicitly deferred because the first
  release contract still requires the existing aggregate dashboard to remain
  unchanged.
- [x] 2026-04-16: Completed milestone 7 verification for the first-release
  per-match marksman surface with focused marksman JVM scopes, detail Compose
  coverage, and a fresh `:app:assembleDebug`.

## Surprises & Discoveries

- The current app already saves more useful marksman-facing data than the
  existing dashboard uses, especially around gold share, farm gold, last hits,
  participation, control duration, and damage-to-opponents.
- The biggest limitation is not missing UI polish; it is missing observability.
  Several valuable playbook rules depend on information the current screenshot
  model does not retain.
- The current dashboard spec is not the right place to “just add coaching.”
  It explicitly excludes predictive/coaching behavior, so this initiative needs
  a new spec pair rather than a quiet implementation drift.
- The user’s phrasing points to per-match usefulness first, which makes the
  record-detail flow the correct first surface before any aggregate dashboard
  expansion.
- The milestone-2 analysis boundary must stay compatible with older saved
  records: first-release marksman eligibility uses the canonical `发育路` lane,
  but it also needs to normalize the historical saved alias `Farm Lane` at the
  analysis boundary instead of silently degrading those records to
  unavailable-for-this-lane.
- The first useful metric layer did not need a composite “marksman score.”
  Field-backed grouped metrics were enough to satisfy the bounded contract
  while staying explainable for later suggestions.
- Suggestions stayed cleaner once each category was allowed to emit at most one
  item and only when its evidence fields were actually present. That avoided
  partial-data filler and kept the `max 3` rule meaningful.
- Once the detail screen showed both the high-level marksman analysis and the
  existing raw grouped record detail, some labels and values legitimately
  appeared twice. Stable test tags were more durable than exact-text uniqueness
  for detail-surface regressions.

## Decision Log

- Decision: plan marksman lane as a new initiative instead of extending the old
  dashboard contract informally.
  - Rationale: `specs/metrics-dashboard.md` explicitly excludes coaching
    behavior, and the user requested new role-specific suggestions.
  - Date/Author: 2026-04-16 / Codex

- Decision: use the external playbook as a source for the local feature spec,
  not as a floating runtime dependency.
  - Rationale: the repository needs a stable contract even if the upstream
    playbook evolves later.
  - Date/Author: 2026-04-16 / Codex

- Decision: start with per-match analysis before aggregate role analytics.
  - Rationale: the user asked for insights after one match is extracted, and
    the per-match surface also forces the clearest honesty about what current
    saved data can support.
  - Date/Author: 2026-04-16 / Codex

- Decision: require an explicit observability map before implementation.
  - Rationale: many strong marksman-lane rules are not directly inferable from
    one static post-match screenshot, so the spec must freeze what is and is
    not supported now.
  - Date/Author: 2026-04-16 / Codex

- Decision: keep the base plan role-first, not hero-first.
  - Rationale: hero-specific handbooks depend on a correct generic marksman
    analysis foundation; building hero advice first would overfit the design.
  - Date/Author: 2026-04-16 / Codex

- Decision: use one shared marksman-lane analysis-input factory for
  `SavedMatchHistoryRecord` and `SavedMatchEntity`.
  - Rationale: milestone 2 requires repository and controller shaping to stay
    aligned; one shared builder prevents role eligibility and field-coverage
    drift between saved-record paths.
  - Date/Author: 2026-04-16 / Codex

- Decision: keep milestone-3 detailed metrics field-backed and group-based
  instead of introducing a composite score.
  - Rationale: the spec only approved bounded categories from existing saved
    fields, and a synthetic score would add fake precision before the
    suggestion engine even exists.
  - Date/Author: 2026-04-16 / Codex

- Decision: emit at most one suggestion per approved category and cap the final
  list at 3 in a fixed priority order.
  - Rationale: the spec requires bounded readable output; per-category emission
    plus fixed priority keeps the list deterministic and avoids flooding the
    detail screen with near-duplicate advice.
  - Date/Author: 2026-04-16 / Codex

- Decision: require complete category evidence before firing a suggestion in
  that category.
  - Rationale: milestone 4 must not fabricate coaching from partial fields, so
    suggestion triggers should fail closed when their evidence is incomplete.
  - Date/Author: 2026-04-16 / Codex

- Decision: keep marksman insights above the raw grouped record detail and
  degrade failures only inside the analysis layer.
  - Rationale: the spec requires the new role-analysis layer to add usefulness
    without hiding the existing saved-field detail or making a failed analysis
    look like a broken record screen.
  - Date/Author: 2026-04-16 / Codex

- Decision: keep the aggregate dashboard unchanged and treat milestone 6 as
  intentionally deferred for the first release.
  - Rationale: `R6` in the feature spec explicitly keeps the first release
    bounded to one saved match in record detail.
  - Date/Author: 2026-04-16 / Codex

## Validation Notes

- 2026-04-16: Wrote the new milestone-2 JVM tests first in
  `core/src/test/kotlin/com/kingsmetric/marksman/MarksmanLaneAnalysisInputTest.kt`.
- 2026-04-16: The first focused run failed at compile time for the expected
  reason because the marksman analysis types and factory did not exist yet.
- 2026-04-16: Added the new pure core model in
  `core/src/main/kotlin/com/kingsmetric/marksman/MarksmanLaneAnalysis.kt`.
- 2026-04-16: The first implementation run failed once due to a constructor
  wiring bug in the metric-group mapping; fixing that bug made the focused test
  scope pass without widening feature scope.
- 2026-04-16: Milestone-2 validation passed with:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.marksman.MarksmanLaneAnalysisInputTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "*Marksman*"`
- 2026-04-16: Wrote the new milestone-3 JVM tests first in
  `core/src/test/kotlin/com/kingsmetric/marksman/MarksmanLaneDetailedMetricsTest.kt`.
- 2026-04-16: The first milestone-3 focused run failed at compile time for the
  expected reason because the detailed metric calculator and availability
  models did not exist yet.
- 2026-04-16: Added the new pure core metric model in
  `core/src/main/kotlin/com/kingsmetric/marksman/MarksmanLaneDetailedMetrics.kt`.
- 2026-04-16: The first implementation pass failed once because the shared
  metric-group definition symbol was referenced with an inconsistent name in
  `MarksmanLaneAnalysis.kt`; fixing that typo made the milestone scope pass
  without widening behavior.
- 2026-04-16: Milestone-3 validation passed with:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.marksman.MarksmanLaneDetailedMetricsTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "*Marksman*"`
- 2026-04-16: Wrote the new milestone-4 JVM tests first in
  `core/src/test/kotlin/com/kingsmetric/marksman/MarksmanLaneSuggestionsTest.kt`.
- 2026-04-16: The first milestone-4 focused run failed at compile time for the
  expected reason because the suggestion engine and suggestion-state types did
  not exist yet.
- 2026-04-16: Added the new pure core suggestion engine in
  `core/src/main/kotlin/com/kingsmetric/marksman/MarksmanLaneSuggestions.kt`.
- 2026-04-16: Milestone-4 validation passed with:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.marksman.MarksmanLaneSuggestionsTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "*Marksman*"`
- 2026-04-16: Wrote the milestone-5 detail-surface JVM and Compose assertions
  first in:
  - `core/src/test/kotlin/com/kingsmetric/app/HistoryDetailDashboardUxTest.kt`
  - `app/src/androidTest/java/com/kingsmetric/HistoryDetailDashboardUxComposeTest.kt`
- 2026-04-16: The first milestone-5 runs failed at compile time for the
  expected reason because the detail-screen state did not yet expose marksman
  insights and the Compose detail surface had no analysis section.
- 2026-04-16: Added the detail-side marksman insights adapter in
  `core/src/main/kotlin/com/kingsmetric/app/HistoryDashboardScreenBinding.kt`
  and rendered the analysis layer in
  `app/src/main/java/com/kingsmetric/HistoryDashboardScreens.kt`.
- 2026-04-16: The first Compose implementation pass failed once because the new
  analysis layer intentionally reused some labels and values already present in
  the raw grouped detail. Stable section tags plus a prefixed visible rule
  label made the verification durable without hiding the duplicate data from
  users.
- 2026-04-16: Running two Gradle builds against the same workspace in parallel
  produced spurious cache/storage failures during this milestone. Rerunning the
  same scopes sequentially removed that noise without any code change.
- 2026-04-16: Milestone-5 and milestone-7 validation passed with:
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.app.HistoryDetailDashboardUxTest"`
  - `./gradlew.bat --no-daemon :core:test --tests "*Marksman*"`
  - `./gradlew.bat --no-daemon :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.kingsmetric.HistoryDetailDashboardUxComposeTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`

## Validation and Acceptance

Planning-stage acceptance:

- `docs/plan.md` lists this file as an active plan.
- The existing history-page plan remains preserved instead of overwritten.
- This plan names the external marksman playbook source, the current saved-data
  constraints, and concrete milestones for spec-first execution.

Implementation-stage acceptance will include:

- a new marksman-lane feature spec and test spec
- focused JVM verification for:
  - eligibility
  - metric calculation
  - suggestion triggers
  - insufficient-data handling
- focused Compose verification for the per-match detail surface and any
  aggregate surface that is explicitly approved later
