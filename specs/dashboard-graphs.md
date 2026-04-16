# Dashboard Graphs Spec

## Goal and Context

Define the first dashboard graph release for the app so users can understand
saved-match trends visually instead of relying only on summary cards.

This spec follows:

- [2026-04-16-dashboard-graphs](../docs/plans/2026-04-16-dashboard-graphs.md)

It extends the existing dashboard contract without replacing it:

- [metrics-dashboard](./metrics-dashboard.md)
- [dashboard-clarity-upgrade](./dashboard-clarity-upgrade.md)
- [history-and-dashboard-screen-binding](./history-and-dashboard-screen-binding.md)

The first graph release stays deliberately small. It uses only locally saved
match records, keeps existing dashboard cards visible, and adds a bounded graph
section that remains readable on a phone-sized portrait screen.

## Concrete Examples

### Example 1: Several Saved Matches

Input:

- user opens `Dashboard` after saving several matches with valid `result` and
  `hero` fields

Expected behavior:

- the existing dashboard summary cards remain visible
- the dashboard also shows:
  - a recent-results graph for the most recent saved matches
  - a hero-usage graph for the most-played heroes
- the user can understand the graphs without relying on color alone

### Example 2: Only One Or Two Saved Matches

Input:

- user opens `Dashboard` with only one or two saved matches

Expected behavior:

- the dashboard still renders the available graph data without fabricating a
  fuller trend than the data supports
- sparse-data messaging remains visible
- the graph section does not look blank or broken

### Example 3: No Saved Matches

Input:

- user opens `Dashboard` before any reviewed match has been saved

Expected behavior:

- the explicit dashboard empty state remains
- the app does not show empty graph containers or fake zero-value charts

### Example 4: Partial Graph Inputs

Input:

- saved records exist, but some records are missing `hero` or `result`

Expected behavior:

- graphs that still have usable data remain visible
- a graph that cannot be built from the available saved fields degrades
  independently instead of blanking the whole dashboard
- the app does not invent hero names or result values

## Inputs and Outputs

### Inputs

- locally saved match records already used by the current dashboard
- existing saved fields such as:
  - `result`
  - `hero`
  - `savedAt`

### Outputs

The dashboard may render:

- the existing summary-card section
- a bounded graph section with up to two graph panels:
  - `Recent Results`
  - `Hero Usage`
- existing empty, sparse-data, and error states when those states apply

## Requirements

- `R1` The dashboard graph feature MUST derive its data from locally saved
  match records only.
- `R2` The first graph release MUST stay bounded to exactly these graph
  families:
  - `Recent Results`
  - `Hero Usage`
- `R3` The dashboard MUST keep the existing primary summary-card hierarchy
  visible when graph data is available. Graphs must not replace the current
  primary cards.
- `R4` The `Recent Results` graph MUST represent at most the five most recent
  saved matches with usable `result` data.
- `R5` The `Recent Results` graph MUST order those matches from oldest to
  newest within that recent slice so the visible sequence reads as one recent
  trend rather than a shuffled snapshot.
- `R6` The `Hero Usage` graph MUST show at most the top three heroes by saved
  match count using the same saved-record source semantics as the existing hero
  usage metric.
- `R7` The `Hero Usage` graph MUST keep a stable descending order by match
  count. If two heroes have the same count, their visible order MUST remain
  stable and user-readable rather than arbitrary.
- `R8` Each visible graph panel MUST include enough user-facing text to remain
  understandable without relying on color alone. At minimum, the user must be
  able to identify the graph title and the visible result or hero/count labels.
- `R9` The graph section MUST remain non-interactive in this first release.
  Users must not need drag, tap, zoom, or tooltip gestures to read the chart.
- `R10` The graph section MUST remain readable on a phone-sized portrait screen
  without horizontal scrolling or off-screen graph content.
- `R11` The dashboard MUST preserve explicit sparse-data messaging when the
  current saved-record sample is too small to imply a strong trend.
- `R12` If no saved records exist, the dashboard MUST continue to show the
  existing explicit empty state instead of rendering blank graph slots or
  fabricated chart values.
- `R13` If dashboard loading fails, the dashboard MUST continue to show an
  explicit error state instead of stale or partial graphs.
- `R14` Graph availability MUST degrade independently. If one graph lacks
  enough usable saved-field data while another graph can still render, the
  renderable graph MUST remain visible and the unavailable graph MUST degrade
  with a clear user-facing explanation rather than removing the whole graph
  section silently.
- `R15` The graph feature MUST NOT invent missing `hero` or `result` values to
  make a graph look more complete.
- `R15a` The `Hero Usage` graph MUST ignore unreadable placeholder hero values,
  such as numeric-only tokens, rather than rendering them as user-facing hero
  labels.
- `R16` The first graph release MUST NOT introduce new aggregate formulas that
  materially change the meaning of the existing dashboard cards. It may shape
  graph-ready series from existing saved-record data, but it must not silently
  redefine the current dashboard metric contract.

## Invariants

- The graph feature uses the same local-first saved-record boundary as the rest
  of the dashboard.
- Existing dashboard cards remain part of the screen.
- Missing or partial saved fields are treated as unavailable inputs, not as
  values to infer.
- The first graph release is additive and bounded, not a general analytics
  system.

## Error Handling And Boundary Behavior

- If the dashboard is in its existing `Empty` state, the graph section MUST not
  appear as a blank placeholder.
- If the dashboard is in its existing `Error` state, the graph section MUST not
  appear with stale data.
- If all recent matches lack usable `result` values, the `Recent Results`
  graph MUST degrade with a clear user-facing unavailable message.
- If all saved matches lack usable `hero` values, the `Hero Usage` graph MUST
  degrade with a clear user-facing unavailable message.
- Saved hero values that are present but unreadable, such as numeric-only
  placeholders, MUST be treated as unusable `hero` inputs for graph shaping.
- If only one graph can be built from the currently saved data, the dashboard
  MUST still show that graph instead of collapsing the entire graph feature.

## Compatibility And Migration Expectations

- This feature MUST work from the current saved-record schema.
- This feature MUST NOT require a Room migration for the first release.
- Existing saved records remain valid inputs even if some graph-relevant fields
  are missing.
- Existing dashboard cards and current dashboard routes remain supported.

## Observability Expectations

- The graph section should be identifiable as its own dashboard area, not mixed
  invisibly into the primary card block.
- Graph labels and graph-state messages must use user-facing wording rather
  than technical identifiers.
- Sparse-data, empty, and graph-unavailable states should remain distinguishable
  from each other.

## Edge Cases

- no saved records
- exactly one saved match
- exactly two saved matches
- more than five saved matches
- all recent matches are victories
- all recent matches are defeats
- multiple heroes tied for the same usage count
- some records missing `hero`
- some records contain unreadable placeholder `hero` values
- some records missing `result`
- all records missing `hero`
- all records missing `result`
- saved records update while the dashboard is visible

## Non-Goals

- arbitrary date-range filtering
- pinch/zoom or drag interactions
- tooltip-based drill-down behavior
- export/share of graph images or data
- lane-specific dashboards or coaching overlays in this first graph release
- replacing the existing dashboard cards with a chart-only dashboard
- adding remote analytics, cloud sync, or server-side trend computation

## Acceptance Criteria

- The dashboard can show a bounded graph section derived from saved local
  records.
- The first graph release stays limited to `Recent Results` and `Hero Usage`.
- Existing primary dashboard cards remain visible and understandable.
- Empty, sparse, partial, and error states remain explicit and honest.
- Graphs remain readable on a phone-sized portrait screen without horizontal
  scrolling.
- The graph feature does not invent missing inputs or silently redefine the
  existing dashboard metrics contract.

## Gotchas

- 2026-04-16: The current dashboard specs still treat advanced charts as a
  non-goal, so graph work must stay explicitly bounded. This first release is a
  small additive graph section, not a generic analytics expansion.
