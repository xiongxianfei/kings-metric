# Dashboard Graphs Test Spec

## Scope

This test spec covers the first bounded dashboard graph release defined in
[dashboard-graphs](./dashboard-graphs.md).

It verifies that:

- graph data is derived from locally saved records only
- the first graph release stays limited to `Recent Results` and `Hero Usage`
- graphs stay additive to the current dashboard cards
- empty, sparse, partial, and error states remain explicit
- graph rendering remains readable and bounded on a phone-sized portrait screen

It does not expand the dashboard into interactive analytics, coaching overlays,
or arbitrary filtering.

## Unit Tests

- `T1` Recent-results graph data uses only locally provided saved records and
  keeps at most the five most recent matches with usable `result` values.
- `T2` Recent-results graph data is ordered from oldest to newest within that
  recent five-match slice.
- `T3` Hero-usage graph data uses only locally provided saved records and keeps
  at most the top three heroes by saved match count.
- `T4` Hero-usage graph data keeps stable descending order by count and stable
  readable ordering for ties.
- `T5` Missing `result` values are omitted from recent-results graph inputs
  rather than fabricated into wins or losses.
- `T6` Missing `hero` values are omitted from hero-usage graph inputs rather
  than fabricated into named heroes.
- `T6a` Unreadable placeholder `hero` values such as numeric-only tokens are
  omitted from hero-usage graph inputs rather than rendered as user-facing hero
  labels.
- `T7` Graph availability degrades independently when one graph has enough data
  and the other does not.
- `T8` Dashboard graph state preserves the existing primary summary-card state
  instead of replacing it with chart-only output.
- `T9` Empty dashboard state does not expose blank graph placeholders.
- `T10` Error dashboard state does not expose stale or partial graph state.
- `T11` Sparse-data state remains explicit when one or two saved matches exist.
- `T12` Graph-state labels and graph-unavailable messages remain user-facing
  rather than technical identifiers.
- `T13` The first graph release exposes exactly the approved graph families:
  `Recent Results` and `Hero Usage`.
- `T14` Graph-ready state shaping does not change the meaning or values of the
  existing dashboard summary-card metrics.

## Integration Tests

- `IT1` Room-backed dashboard observation emits graph-ready loaded state when
  saved records exist.
- `IT2` Room-backed dashboard observation emits explicit empty state when no
  saved records exist, without graph placeholders.
- `IT3` Room-backed dashboard observation emits explicit error state when
  dashboard loading fails, without stale graphs.
- `IT4` Repository updates while the dashboard binder is active refresh both
  existing cards and graph-ready state.
- `IT5` Partial saved data where only `result` is usable keeps `Recent Results`
  visible while `Hero Usage` degrades independently.
- `IT6` Partial saved data where only `hero` is usable keeps `Hero Usage`
  visible while `Recent Results` degrades independently.
- `IT7` Existing saved-record schema remains sufficient; graph-ready state does
  not require a migration or additional persistence fields.

## Compose / UI Tests

- `CT1` Loaded dashboard state renders the existing primary cards and a
  separate graph section on the same screen.
- `CT2` Loaded dashboard state renders a visible `Recent Results` graph panel
  with user-facing title and visible result labels.
- `CT3` Loaded dashboard state renders a visible `Hero Usage` graph panel with
  user-facing title and visible hero/count labels.
- `CT4` Sparse-data dashboard state with one or two saved matches stays
  informative and does not look like a blank chart failure.
- `CT5` If only one graph is available, the available graph remains visible and
  the unavailable graph area shows a readable explanation instead of collapsing
  the whole section.
- `CT6` Empty dashboard state remains explicit and does not render blank graph
  containers.
- `CT7` Error dashboard state remains explicit and does not render stale graph
  content.
- `CT8` Graph panels remain bounded within the normal portrait dashboard flow
  and do not require horizontal scrolling.
- `CT9` Graph labels and graph-state text remain user-facing and visible
  without relying on color alone.

## Manual Review

- `M1` On a narrow phone-sized portrait device, primary summary cards are still
  easy to find before or alongside the graph section.
- `M2` No graph requires horizontal scrolling or off-screen reading.
- `M3` With one or two saved matches, the dashboard still reads as sparse data,
  not as a misleading full analytics surface.
- `M4` Recent-results and hero-usage graphs remain understandable at a glance
  without drag gestures, tooltips, or color-only cues.
- `M5` Empty and error states remain explicit instead of showing blank graph
  chrome.

## Example Coverage

- Example 1 from the feature spec is covered by `T1`, `T3`, `T8`, `CT1`,
  `CT2`, and `CT3`.
- Example 2 is covered by `T11`, `CT4`, and `M3`.
- Example 3 is covered by `T9`, `CT6`, and `M5`.
- Example 4 is covered by `T5`, `T6`, `T7`, `IT5`, `IT6`, and `CT5`.

## Edge-Case Coverage

- no saved records -> `T9`, `IT2`, `CT6`
- exactly one saved match -> `T11`, `CT4`, `M3`
- exactly two saved matches -> `T11`, `CT4`, `M3`
- more than five saved matches -> `T1`, `T2`
- all recent matches are victories -> `CT2`
- all recent matches are defeats -> `CT2`
- multiple heroes tied for the same usage count -> `T4`
- some records missing `hero` -> `T6`, `IT5`, `CT5`
- some records contain unreadable placeholder `hero` values -> `T6a`, `CT5`
- some records missing `result` -> `T5`, `IT6`, `CT5`
- all records missing `hero` -> `T7`, `IT5`, `CT5`
- all records missing `result` -> `T7`, `IT6`, `CT5`
- saved records update while the dashboard is visible -> `IT4`

## Requirement Coverage Map

- `R1` -> `T1`, `T3`, `IT1`
- `R2` -> `T13`, `CT1`
- `R3` -> `T8`, `CT1`, `M1`
- `R4` -> `T1`
- `R5` -> `T2`
- `R6` -> `T3`
- `R7` -> `T4`
- `R8` -> `T12`, `CT2`, `CT3`, `CT9`, `M4`
- `R9` -> `CT9`, `M4`
- `R10` -> `CT8`, `M2`
- `R11` -> `T11`, `CT4`, `M3`
- `R12` -> `T9`, `IT2`, `CT6`
- `R13` -> `T10`, `IT3`, `CT7`
- `R14` -> `T7`, `IT5`, `IT6`, `CT5`
- `R15` -> `T5`, `T6`
- `R15a` -> `T6a`, `CT5`
- `R16` -> `T14`, `IT4`

## What Not To Test

- new dashboard formulas beyond the current saved-record-derived graph families
- arbitrary date filtering
- zoom, drag, or tooltip interactions
- export/share of graph images
- coaching overlays or lane-specific dashboard logic
- third-party chart library behavior, because the spec does not require one

## Not Directly Testable

- purely subjective beauty beyond the explicit graph bounds, labels, and
  phone-sized readability checks
- exact drawing technique, because the contract defines observable graph
  behavior rather than a specific rendering implementation
