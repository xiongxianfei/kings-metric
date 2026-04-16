# History Page Detail Expansion Spec

## Goal and Context

Define a bounded richer-summary contract for the history page so users can
identify saved matches more quickly before opening record detail.

Related plan:
- `docs/plans/2026-04-16-history-page-detail-expansion.md`

This spec expands the information shown directly on the history page. It does
not replace the detail screen, add inline editing, or widen the saved-match
schema by itself.

## Concrete Examples

### Example 1: Saved Match Has Full Summary Fields

Input:
- History contains a saved match with:
  - hero: `Sun Shangxiang`
  - result: `victory`
  - lane: `Farm Lane`
  - KDA: `11/1/5`
  - score: `20-10`
  - screenshot available

Expected behavior:
- The row shows:
  - hero as the primary text
  - bounded quick summary in this order:
    - result
    - lane
    - KDA
    - score
  - saved recency
- The row still reads as one tappable saved match, not as a mini detail page.

### Example 2: Older Record Is Missing Some Summary Fields

Input:
- History contains a saved match with:
  - hero: `Sun Shangxiang`
  - result: `defeat`
  - lane: empty
  - KDA: `5/3/7`
  - score: empty

Expected behavior:
- The row still shows hero, result, KDA, and recency.
- Missing optional quick-summary fields are omitted rather than shown as noisy
  placeholders.
- The row remains selectable and readable.

### Example 3: Hero Is Missing And Preview Link Is Broken

Input:
- History contains a saved match with:
  - hero: empty
  - result: `victory`
  - lane: `Farm Lane`
  - KDA: `11/1/5`
  - score: `20-10`
  - screenshot unavailable

Expected behavior:
- The row uses the existing readable hero fallback.
- Preview-unavailable messaging stays secondary metadata.
- The row is still clearly a valid saved record, not a broken-item warning.

### Example 4: User Needs More Context But Not Full Detail

Input:
- Two adjacent history rows have the same hero but different lane/KDA/score.

Expected behavior:
- The user can distinguish the two rows from the list alone.
- The user does not need to open detail just to separate these basic match
  summaries.

## Inputs and Outputs

### Inputs

- Saved history records that may already contain:
  - `hero`
  - `result`
  - `lane`
  - `kda`
  - `score`
  - `savedAt`
  - screenshot availability state

### Outputs

- One history-row quick summary per saved record, rendered as a single
  tappable row/card
- A bounded summary made from already-saved match data only

## Requirements

- `R1` The history page MUST keep one primary hero/title line for each saved
  match row.
- `R2` The history page MUST keep recency visible for each saved match row.
- `R3` The history page MUST expand the in-row quick summary beyond
  `hero + result + recency` by using this bounded quick-summary set and order:
  1. result
  2. lane
  3. KDA
  4. score
- `R4` The history page MUST NOT add additional in-row quick-summary fields
  beyond `result`, `lane`, `KDA`, and `score` under this feature.
- `R5` If `hero` is empty, the row MUST use the existing readable fallback
  instead of showing a blank or broken-looking title.
- `R6` If `result` is empty, the row MUST use the existing readable result
  fallback instead of dropping the result slot silently.
- `R7` If `lane`, `KDA`, or `score` are empty, those optional quick-summary
  items MUST be omitted instead of rendered as repeated `Not entered`
  placeholders. When one or more of those items are omitted, the remaining
  visible quick-summary items MUST keep the `R3` relative order.
- `R8` Preview-unavailable messaging MUST remain secondary row metadata. It
  MUST NOT replace the main saved-match summary or make the whole row read like
  an error item.
- `R9` The richer history row MUST remain one obvious tap target that opens the
  existing detail flow for that record.
- `R10` The history page MUST continue to work for older or incomplete saved
  records without any required migration under this feature. Rows may show a
  reduced quick summary when optional fields are missing.
- `R11` The history page MUST use user-facing labels and values only. It MUST
  use only human-readable user-facing text. The quick summary may use concise
  values or labeled values, but it MUST NOT expose screenshot paths, raw
  storage-like output, or internal field-key identifiers in the row summary.
- `R12` The richer row MUST fit within the normal vertical history-card flow on
  a phone-sized screen without horizontal scrolling or hidden off-screen
  quick-summary content.
- `R13` When `lane`, `KDA`, or `score` are shown, each visible quick-summary
  item MUST remain visually separable from the other quick-summary items and
  from secondary metadata such as recency or preview-unavailable text.
- `R14` If `lane`, `KDA`, and `score` are all absent, the row MUST still render
  a clean reduced summary using hero/title fallback, result/result fallback,
  and recency only, without empty placeholders.

## Invariants

- The history row continues to represent one saved match, not an expandable
  inline detail view.
- The detail screen remains the full-fidelity surface for grouped field data.
- This feature does not invent, infer, or normalize new match values beyond
  what is already saved.

## Error Handling and Boundary Behavior

- History empty and history load failure behavior remain governed by the
  existing history-screen contract.
- If a saved record lacks `lane`, `KDA`, or `score`, the row must degrade to
  the remaining visible quick-summary items without looking broken.
- If a saved record lacks `lane`, `KDA`, and `score`, the row must degrade to
  hero/title, result, and recency only.
- If screenshot linkage is broken, preview-unavailable messaging stays
  secondary and must not hide the rest of the row summary.
- If a future quick-summary request needs fields not currently stored in saved
  records, that requires a separate approved persistence/schema decision and is
  not part of this spec.

## Compatibility and Migration Expectations

- Existing saved records remain valid inputs.
- No save-flow, OCR-flow, or Room-schema migration is required by this spec.
- Records saved before this feature may show fewer quick-summary items when the
  optional fields are absent.

## Observability Expectations

- A reviewer should be able to look at one history row and identify:
  - which hero the match belongs to
  - the result
  - up to three additional bounded match details from the approved set
  - when the record was saved
- A user should be able to distinguish adjacent saved matches more often from
  the list itself without needing detail as the first disambiguation step.

## Edge Cases

- Hero missing.
- Result missing.
- Lane missing.
- KDA missing.
- Score missing.
- Multiple optional quick-summary fields missing on the same row.
- Lane, KDA, and score all missing on the same row.
- Screenshot preview unavailable.
- Multiple adjacent records with the same hero.
- Existing empty or failed history-load states.

## Non-Goals

- Inline editing on the history page.
- Expanding the row into full grouped record detail.
- Search, filtering, or sorting controls.
- Adding per-row screenshot thumbnails or galleries.
- Adding quick-summary fields beyond `result`, `lane`, `KDA`, and `score`.
- Persistence/schema expansion unless separately approved.

## Acceptance Criteria

- History rows expose more useful saved-match context than the current sparse
  row contract.
- The quick-summary set is bounded to `result`, `lane`, `KDA`, and `score` in
  that order, with graceful omission of missing optional items.
- Rows remain readable, obviously tappable, and phone-friendly.
- Preview-unavailable messaging remains secondary.
- Existing detail navigation and history empty/error behavior remain intact.

## Gotchas

- The goal is not "show every interesting field." The goal is a bounded
  disambiguation summary that still preserves row scanability on a phone.
- Optional quick-summary omissions are preferable to repetitive placeholder
  noise in the history list.
