# History Page Detail Expansion Test Spec

## Scope

This test spec covers the bounded richer-summary contract for history rows:

- primary hero/title line
- visible recency
- bounded quick-summary fields in approved order
- graceful fallback when optional quick-summary fields are missing
- preserved tap/navigation behavior
- safe handling of incomplete and older saved records

It also defines the manual phone-sized checks needed for row readability and
visual separability, because those are only partly provable through automated
Compose assertions.

## Unit and State-Mapper Tests

- `T1` Full-summary row mapping keeps one primary hero/title line and visible
  recency while exposing quick-summary items in this order:
  - result
  - lane
  - KDA
  - score
  Covers: `R1`, `R2`, `R3`

- `T2` Row mapping exposes only the approved quick-summary set and does not
  leak additional saved fields such as participation, gold share, screenshot
  path, or internal field identifiers.
  Covers: `R4`, `R11`

- `T3` Hero-missing and result-missing rows use the existing readable fallbacks
  instead of blank or silently dropped summary content.
  Covers: `R5`, `R6`

- `T4` If one or more of `lane`, `KDA`, or `score` are missing, row mapping
  omits those items without rendering `Not entered` placeholders and preserves
  the remaining visible quick-summary items in `R3` relative order.
  Covers: `R7`

- `T5` Preview-unavailable state stays secondary metadata in row state and does
  not replace the main saved-match summary.
  Covers: `R8`

- `T6` Older or incomplete saved records still map into valid history rows
  without migration, including the fully reduced case where `lane`, `KDA`, and
  `score` are all absent.
  Covers: `R10`, `R14`

## Repository and Integration Tests

- `IT1` `RoomObservedMatchRepository.observeHistory()` maps saved entities into
  history items that expose the approved richer-summary fields from existing
  saved columns only.
  Covers: `R3`, `R4`, `R10`

- `IT2` `MatchHistoryController` and `RoomObservedMatchRepository` stay aligned
  on the richer history-row contract for the same saved-record inputs.
  Covers: plan alignment requirement, `R10`

- `IT3` Two adjacent saved records with the same hero but different lane/KDA/
  score remain distinguishable from history state alone.
  Covers: Example 4, observability expectation

## Compose and User-Visible Integration Tests

- `CT1` History screen renders a full-summary row with:
  - hero/title
  - result
  - lane
  - KDA
  - score
  - recency
  in one tappable row/card.
  Covers: Example 1, `R1`, `R2`, `R3`, `R9`

- `CT2` History screen renders a partial-summary row that omits only the
  missing optional quick-summary items while keeping the remaining visible
  items in order.
  Covers: Example 2, `R7`, `R10`

- `CT3` History screen renders a hero-missing row with the existing hero
  fallback and keeps preview-unavailable messaging secondary.
  Covers: Example 3, `R5`, `R8`

- `CT4` History screen renders the fully reduced row using only hero/title
  fallback, result/result fallback, and recency when `lane`, `KDA`, and score
  are all absent.
  Covers: `R6`, `R14`

- `CT5` History screen row content uses human-readable user-facing text only
  and does not display screenshot paths or raw storage-like output.
  Covers: `R11`

- `CT6` Tapping a richer history row still opens the existing detail flow, and
  returning from detail still returns the user to history safely.
  Covers: `R9`, acceptance continuity requirement

## Manual Phone-Sized Review

- `M1` On a narrow portrait phone, the richer history row remains readable
  without horizontal scrolling or hidden off-screen quick-summary content.
  Covers: `R12`

- `M2` Visible quick-summary items remain visually separable from each other
  and from recency/preview metadata.
  Covers: `R13`

- `M3` Multiple adjacent rows with the same hero can be distinguished at a
  glance from the richer quick summary.
  Covers: Example 4, observability expectation

## Concrete Scenarios and Fixtures

- Full-summary fixture:
  - hero: `Sun Shangxiang`
  - result: `victory`
  - lane: `Farm Lane`
  - KDA: `11/1/5`
  - score: `20-10`
  - screenshot available

- Partial-summary fixture:
  - hero: `Sun Shangxiang`
  - result: `defeat`
  - lane: empty
  - KDA: `5/3/7`
  - score: empty

- Hero-missing preview-unavailable fixture:
  - hero: empty
  - result: `victory`
  - lane: `Farm Lane`
  - KDA: `11/1/5`
  - score: `20-10`
  - screenshot unavailable

- Fully reduced fixture:
  - hero: empty or present
  - result: empty or present
  - lane: empty
  - KDA: empty
  - score: empty

- Adjacent disambiguation fixture:
  - two records with same hero
  - different lane and/or KDA and/or score

## Coverage Map

- `R1` -> `T1`, `CT1`
- `R2` -> `T1`, `CT1`
- `R3` -> `T1`, `IT1`, `CT1`
- `R4` -> `T2`, `IT1`
- `R5` -> `T3`, `CT3`
- `R6` -> `T3`, `CT4`
- `R7` -> `T4`, `CT2`
- `R8` -> `T5`, `CT3`
- `R9` -> `CT1`, `CT6`
- `R10` -> `T6`, `IT1`, `IT2`, `CT2`
- `R11` -> `T2`, `CT5`
- `R12` -> `M1`
- `R13` -> `M2`
- `R14` -> `T6`, `CT4`

## What Not To Test

- OCR, parsing, or save-flow behavior.
- New persistence/schema behavior beyond the current saved fields.
- Inline editing, search, filtering, sorting, or thumbnail gallery behavior.
- Full detail-screen grouping beyond history-to-detail continuity.

## Not Directly Testable

- Exact perceived scan speed improvement.
- Visual elegance beyond the concrete proxies in the feature spec.
- `R12` and `R13` are only partially automatable, so the canonical checks are
  manual phone-sized review items `M1` and `M2`.
