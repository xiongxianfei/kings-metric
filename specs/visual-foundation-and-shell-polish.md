# Visual Foundation And Shell Polish Spec

## Goal and Context

Define the shared visual foundation for the Android app shell so primary
screens feel cohesive, readable, and intentional on a phone-sized device
without changing route semantics, business logic, or feature-specific review
rules.

This spec follows the active plan:
- [2026-04-15-ui-ux-polish](../docs/plans/2026-04-15-ui-ux-polish.md)

This spec covers the reusable visual baseline for the app and the shell-level
presentation of that baseline. Screen-specific polish for import, review,
history, detail, dashboard, and diagnostics remains in their own specs.

## Concrete Examples

### Example 1: App Launch On A Primary Route

Input:
- The user opens the app on `Import`, `History`, or `Dashboard`.

Expected behavior:
- The top app bar, primary navigation, route title hierarchy, screen padding,
  and primary action treatment feel like one app instead of separate screens
  built with unrelated visual rules.

### Example 2: Entering A Secondary Task Screen

Input:
- The user enters `Review` or `Record Detail`.

Expected behavior:
- The screen still looks like part of the same app.
- Secondary task screens remain visually distinct from the primary shell, but
  they inherit the same typography, spacing, and component language.

### Example 3: Empty Or Failure State On A Primary Screen

Input:
- A primary screen shows empty, loading, or user-message content.

Expected behavior:
- The content uses the same visual language as the rest of the app.
- The user message remains readable and obvious instead of looking like raw
  debug text dropped into the layout.

## Inputs and Outputs

### Inputs

- Existing app-shell routes and screen content
- Existing shared UX labels and state messaging
- Existing Material 3 base primitives

### Outputs

- A reusable app-level visual foundation for:
  - typography hierarchy
  - spacing scale
  - shape/elevation treatment
  - primary action treatment
  - shared surface patterns for sections, cards, and state blocks
- Shell-level screens that apply that foundation consistently

## Scope Boundary

This milestone covers shell-level presentation for:

- `Import`
- `History`
- `Dashboard`
- shell chrome shared by `Review` and `Record Detail`

This milestone does not redesign:

- review-form internals
- dashboard metric composition
- diagnostics content layout
- record-detail field grouping beyond the shared shell-level foundation

## Requirements

- `R1` The app MUST keep Jetpack Compose and Material 3 as the base UI stack
  for this initiative.
- `R2` The app MUST define one shared visual foundation for shell-level UI,
  covering at least:
  - top-level screen padding
  - route title typography
  - spacing scale
  - shared surface/card treatment
  - empty/failure state container treatment
  - primary-action treatment
- `R3` Primary shell destinations MUST apply that shared visual foundation
  consistently instead of each screen inventing unrelated spacing, title, card,
  button, or state-block styling.
- `R4` Secondary task screens such as `Review` and `Record Detail` MUST remain
  visually consistent with the same app-level design language even when their
  layout differs from primary shell destinations. At minimum, they MUST inherit
  the same typography hierarchy, spacing scale, and primary-action treatment.
- `R5` The shell MUST preserve the current route structure, safe fallback
  behavior, and navigation semantics while applying the new visual foundation.
- `R6` The visual foundation MUST keep route titles, section headings, body
  content, and primary actions visually distinguishable through a shared
  hierarchy that remains readable on phone-sized screens.
- `R7` Empty, loading, retryable-failure, and informational shell messages
  MUST remain readable and visually integrated with the app instead of looking
  like raw debug text.
- `R8` The shell polish MUST NOT weaken accessibility-relevant behavior such as
  readable hierarchy, primary-action reachability, or larger-text usability on
  a phone-sized screen.
- `R9` The visual foundation MUST NOT require a new third-party UI framework or
  replace Material 3 primitives unless a later approved spec explicitly changes
  that rule.
- `R10` The visual foundation MUST preserve current shared UX copy semantics.
  This feature may change visual presentation and emphasis, but it MUST NOT
  mutate the meaning of existing user-facing messages.

## Invariants

- Navigation behavior stays governed by the existing shell/navigation specs.
- Business logic, OCR, validation, and persistence behavior stay unchanged.
- Shared visual rules belong to the Android app layer, not the pure business or
  state-mapping layer.

## Interface Expectations

- The app should feel visually consistent before the user evaluates any
  screen-specific polish.
- The user should be able to identify the current route context quickly through
  title, spacing, and layout hierarchy.
- Primary actions should look like primary actions across the app instead of
  relying on ad hoc button styling on each screen.

## Error Handling And Boundary Behavior

- Existing user-visible failure or fallback messages MUST remain visible after
  shell polish; visual cleanup MUST NOT hide or down-rank critical guidance.
- If a route falls back due to missing required state, the fallback message and
  destination context MUST remain clear within the polished shell.
- Shell polish MUST NOT break any currently working in-scope screen behavior
  while the shared visual foundation is being adopted.

## Compatibility Expectations

- This spec is compatible with the existing `app-shell-navigation-ux` and
  `shared-ux-labels-and-state-messaging` contracts.
- This spec is intentionally foundational. Later UI polish specs should consume
  the shared visual foundation rather than redefining typography, spacing, or
  action styling locally.

## Observability Expectations

- A reviewer should be able to identify the same title hierarchy, spacing
  rhythm, shared surface treatment, and primary-action treatment across
  top-level shell screens.
- Compose tests should continue to assert route context and navigation
  semantics without depending on brittle one-off styling details.
- Manual phone-sized review should confirm that primary shell screens look like
  one product, not a collection of mismatched tools.

## Edge Cases

- The app launches with no saved records and the shell still needs to feel
  complete rather than empty or unfinished.
- A secondary screen such as `Review` or `Record Detail` appears after shell
  polish and still feels connected to the same app.
- A user-visible empty or fallback message appears after shell polish and must
  remain readable and obvious.
- Larger text on a phone-sized device must not make the shell hierarchy or main
  actions ambiguous.
- A primary route with no obvious primary action today must still receive a
  clear hierarchy and shared shell treatment without inventing new business
  actions.

## Non-Goals

- Redesigning import, review, history, detail, dashboard, or diagnostics
  screen-specific content flows in this spec
- Changing OCR, validation, save rules, or repository behavior
- Adding new routes, settings surfaces, or theme-mode systems
- Replacing Compose or Material 3 with another UI framework

## Acceptance Criteria

- Primary shell destinations share one top-level padding rhythm.
- Route titles use one shared title style across in-scope shell screens.
- Primary actions use one shared visual treatment across in-scope shell screens
  that expose a primary action.
- Empty and failure messages use one shared readable state-block treatment
  rather than bare text dropped into the layout.
- Secondary task screens still feel visually connected to the same app through
  inherited typography, spacing, and primary-action treatment.
- Shared messages, route semantics, and safe fallback behavior remain intact.
- The milestone produces reusable visual rules and components that later UI
  polish work can consume instead of duplicating local styling.

## Gotchas

- 2026-04-16: Do not treat “shell polish” as only a one-screen restyle. This
  milestone must establish reusable design-system pieces first, or later
  milestones will drift back into screen-by-screen visual inconsistency.
