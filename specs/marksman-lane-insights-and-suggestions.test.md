# Marksman Lane Insights And Suggestions Test Spec

## Scope

This test spec covers the first-release marksman-lane analysis feature for one
saved match in the record-detail flow.

It covers:

- marksman-lane eligibility and non-eligibility states
- bounded detailed metric groups for one saved match
- bounded deterministic suggestions
- partial-data degradation
- preservation of existing raw grouped record detail
- the explicit first-release boundary that aggregate dashboard behavior remains
  unchanged

It does not expand the feature into hero-specific coaching, non-marksman lanes,
or replay-style map/timing analysis.

## Test Fixtures And Scenarios

- `F1` Eligible marksman-lane saved match with strong saved coverage
  - `lane = 发育路`
  - includes all first-release metric inputs
- `F2` Eligible marksman-lane saved match with partial optional inputs
  - `lane = 发育路`
  - some optional inputs such as `goldFromFarming`, `lastHits`, or
    `controlDuration` are missing
- `F3` Non-marksman saved match
  - `lane = 中路` or another non-`发育路` lane
- `F4` Saved match with missing or unresolved lane
- `F5` Eligible marksman-lane saved match with no high-priority suggestion
  triggers
- `F6` Eligible marksman-lane saved match with more than 3 suggestion triggers
- `F7` Older saved match with partial fields but enough data to open detail
- `F8` Internal analysis failure injected at the analysis boundary while saved
  raw detail is still available

## Unit Tests

- `T1` `发育路` saved lane is treated as marksman-lane eligible.
- `T2` Non-`发育路` saved lane is treated as `Unavailable For This Lane`.
- `T3` Missing or unresolved lane is treated as `Insufficient Saved Data`.
- `T4` Eligible analysis uses only the approved first-release field set from
  `R8`.
- `T5` Missing optional fields do not fabricate metric values; affected metrics
  degrade to unavailable or partial instead.
- `T6` Independent metric groups remain available when another metric group
  lacks required optional inputs.
- `T7` Deterministic suggestion triggers produce the same result for the same
  saved match input on repeated evaluation.
- `T8` Each suggestion payload includes:
  - title
  - rationale
  - one visible evidence line
  - playbook rule category
- `T9` When more than 3 suggestion triggers fire, the final visible suggestion
  set is capped at 3.
- `T10` When no high-priority suggestion triggers fire for an eligible match,
  the analysis produces the neutral no-high-priority state.
- `T11` Suggestions never claim deferred playbook areas such as anti-gank
  timing, objective timing quality, map rotation quality, or replay-accurate
  positioning.
- `T12` Marksman-lane analysis logic is testable outside Compose UI code.

## Integration Tests

- `IT1` Opening detail for `F1` shows:
  - eligible marksman analysis state
  - approved metric groups
  - bounded suggestions
  - existing raw grouped record detail
- `IT2` Opening detail for `F2` shows:
  - eligible state
  - partial metric availability
  - no fabricated values
  - readable insufficiency messaging
- `IT3` Opening detail for `F3` shows `Unavailable For This Lane` while keeping
  the saved raw detail screen usable.
- `IT4` Opening detail for `F4` shows `Insufficient Saved Data` while keeping
  the saved raw detail screen usable.
- `IT5` Opening detail for `F5` shows the neutral no-high-priority suggestion
  state instead of an empty or broken suggestions surface.
- `IT6` Opening detail for `F6` shows at most 3 visible suggestions even when
  more triggers fire internally.
- `IT7` Injected analysis failure `F8` degrades only the analysis layer and
  does not block access to the saved raw record detail.
- `IT8` Older partial saved match `F7` remains compatible and produces either
  partial analysis or explicit insufficiency, not a crash.
- `IT9` Aggregate dashboard behavior remains on its existing contract in this
  first release and does not silently gain marksman coaching output.

## Compose / End-To-End Tests

- `CT1` Eligible marksman detail screen renders:
  - insights section title
  - approved metric group containers
  - suggestion cards/items
  - existing raw grouped detail below or alongside the analysis layer
- `CT2` Non-marksman detail screen renders the unavailable-for-this-lane state
  clearly and keeps raw detail readable.
- `CT3` Missing-lane detail screen renders the insufficient-data state clearly
  and keeps raw detail readable.
- `CT4` Eligible partial-data detail screen renders partial/unavailable metric
  content without placeholder corruption.
- `CT5` Eligible neutral-state detail screen renders the no-high-priority
  suggestion state instead of an empty gap.
- `CT6` Over-triggered eligible detail screen renders no more than 3 suggestion
  items.
- `CT7` The marksman insights section remains readable on a phone-sized detail
  screen without horizontal scrolling.
- `CT8` Existing grouped raw record detail is still reachable when the
  marksman-lane insights section is present.

## Manual Verification

- `M1` On a phone-sized device, the detail screen reads as one coherent flow:
  summary, marksman insights, then raw grouped detail.
- `M2` The user can distinguish:
  - eligible analysis
  - unavailable-for-this-lane
  - insufficient-data
  - no-high-priority-suggestions
  without developer explanation.
- `M3` The marksman insights section adds usefulness without making the detail
  screen feel like a wall of text.
- `M4` Existing raw grouped detail remains understandable and is not visually
  buried by the new analysis layer.

## Edge Case Coverage

- Eligible marksman match with strong coverage covered by `T1`, `T4`, `IT1`,
  `CT1`
- Eligible match with partial optional fields covered by `T5`, `T6`, `IT2`,
  `CT4`
- Non-marksman lane covered by `T2`, `IT3`, `CT2`
- Missing or unresolved lane covered by `T3`, `IT4`, `CT3`
- No high-priority suggestion triggers covered by `T10`, `IT5`, `CT5`
- More than 3 suggestion triggers covered by `T9`, `IT6`, `CT6`
- Internal analysis failure covered by `IT7`
- Older saved records covered by `IT8`
- Dashboard unchanged in first release covered by `IT9`

## What Not To Test

- OCR extraction or template-recognition logic
- hero-specific playbooks or build advice
- non-marksman lane coaching
- live/in-match coaching behavior
- replay-style claims about vision, anti-gank timing, map movement, or exact
  objective calls
- cloud analytics or remote recommendation behavior

## Coverage Map

- `R1` covered by `IT1`, `CT1`
- `R2` covered by `T1`
- `R3` covered by `T2`, `IT3`, `CT2`
- `R4` covered by `T3`, `IT4`, `CT3`
- `R5` covered by `T4`, `T12`
- `R6` covered by `IT9`
- `R7` covered by `IT1`, `CT1`
- `R8` covered by `T4`
- `R9` covered by `T5`, `IT2`, `CT4`
- `R10` covered by `T7`, `T11`
- `R11` covered by `T7`, `T11`
- `R12` covered by `T8`, `CT1`
- `R13` covered by `T9`, `IT6`, `CT6`
- `R14` covered by `T10`, `IT5`, `CT5`
- `R15` covered by `IT1`, `IT3`, `IT4`, `CT1`, `CT2`, `CT3`, `CT8`
- `R16` covered by `IT1`, `IT3`, `IT4`, `CT1`, `CT2`, `CT3`
- `R17` covered by `T12`

## Gaps

- None at spec time. If implementation later introduces hero-specific or
  aggregate dashboard behavior, that work needs a spec update before test
  expansion.
