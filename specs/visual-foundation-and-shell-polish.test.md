# Visual Foundation And Shell Polish Test Spec

## Scope

This test spec covers the shell-level visual foundation for:

- `Import`
- `History`
- `Dashboard`
- shell chrome shared by `Review` and `Record Detail`

It verifies that the app establishes one reusable visual baseline for shell
presentation while preserving navigation semantics, shared copy semantics, and
safe fallback behavior.

## Unit And Static Tests

- `T1` Shared shell foundation exposes one reusable source for:
  - top-level screen padding
  - route title typography
  - spacing scale
  - shared surface/card treatment
  - empty/failure state container treatment
  - primary-action treatment
- `T2` In-scope primary destinations resolve shell presentation using the same
  shared foundation rather than per-screen ad hoc values.
- `T3` Secondary task surfaces (`Review`, `Record Detail`) inherit the same
  typography hierarchy, spacing scale, and primary-action treatment metadata
  even when their layouts differ from primary destinations.
- `T4` Shared shell polish preserves existing route structure and fallback
  semantics; route classification and route titles remain unchanged.
- `T5` Shared shell polish preserves existing shared UX copy semantics; visual
  wrappers or state-block presentation do not change the message meaning
  returned by shared copy mappings.
- `T6` Shell-level visual foundation belongs to the Android app layer and does
  not require moving paint-level styling into `core` business/state classes.
- `T7` The shell visual foundation still uses Compose and Material 3 as the
  base stack and does not introduce a new third-party UI framework dependency.

## Integration And Compose Tests

- `IT1` App launch on each primary destination (`Import`, `History`,
  `Dashboard`) shows:
  - the shared route title treatment
  - readable top-level padding rhythm
  - the existing primary navigation semantics
- `IT2` Primary destinations with a primary action expose one shared primary
  action treatment rather than unrelated per-screen button styling proxies.
- `IT3` Empty or failure shell messages render inside one shared readable
  state-block treatment rather than bare text dropped directly into the layout.
- `IT4` Review route entered from import still looks connected to the same app
  shell through inherited title/spacing/action treatment while remaining a
  secondary task screen.
- `IT5` Record detail route entered from history still looks connected to the
  same app shell through inherited title/spacing/action treatment while
  preserving secondary-screen navigation semantics.
- `IT6` Existing safe fallback behavior for missing secondary-route state
  remains visible and readable inside the polished shell.
- `IT7` App launch with no saved records still shows a complete shell and
  readable empty-state treatment rather than an unfinished or unstyled screen.
- `IT8` A primary route without a business-specific main action still receives
  the shared shell hierarchy and state-block treatment without inventing a new
  action just for visual symmetry.

## Manual Phone-Sized Review

- `M1` On a portrait phone-sized device, `Import`, `History`, and `Dashboard`
  feel like one product through shared title hierarchy, spacing rhythm, and
  navigation presentation.
- `M2` `Review` and `Record Detail` feel visually connected to the same app,
  not like unrelated screens.
- `M3` Primary actions are visually dominant where they already exist, but
  screens without a true primary action do not gain fake actions.
- `M4` Empty and failure states remain obvious, calm, and readable.
- `M5` Larger text on a phone-sized device does not make route hierarchy or
  primary actions ambiguous.

## Requirement Coverage Map

- `R1` covered by `T7`
- `R2` covered by `T1`
- `R3` covered by `T2`, `IT1`, `IT2`, `IT3`
- `R4` covered by `T3`, `IT4`, `IT5`
- `R5` covered by `T4`, `IT6`
- `R6` covered by `T1`, `IT1`, `M1`
- `R7` covered by `T5`, `IT3`, `IT7`, `M4`
- `R8` covered by `IT1`, `M5`
- `R9` covered by `T7`
- `R10` covered by `T5`, `IT6`

## Example Coverage

- Example 1 covered by `IT1`, `IT2`, `M1`
- Example 2 covered by `T3`, `IT4`, `IT5`, `M2`
- Example 3 covered by `IT3`, `IT7`, `M4`

## Edge Case Coverage

- No saved records at app launch covered by `IT7`
- Secondary task screen remains connected to the same app covered by `IT4`,
  `IT5`, `M2`
- User-visible empty or fallback message remains readable covered by `IT3`,
  `IT6`, `M4`
- Larger text on a phone-sized device covered by `M5`
- Primary route with no obvious primary action covered by `IT8`, `M3`

## What Not To Test

- OCR, parsing, validation, or save-rule correctness
- screen-specific review-form grouping or dashboard-metric composition
- pixel-perfect color values, animation taste, or subjective brand appeal
- diagnostics content layout beyond shell-level foundation behavior

## Not Directly Testable

- Pure aesthetic judgment such as whether the shell feels “beautiful” beyond
  the concrete proxies above
- Exact visual taste of spacing or typography beyond shared-token usage,
  readable hierarchy, and cross-screen consistency
