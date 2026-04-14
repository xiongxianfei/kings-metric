# App Shell Navigation UX Spec

## Goal and Context

Define the mobile-friendly navigation experience for the Android app shell so
users can move between import, review, history, dashboard, and detail without
the current tool-like top-row button experience.

This spec refines the existing navigation shell without changing route
semantics.

## Concrete Examples

### Example 1: Normal App Use

Input:
- User opens the app and wants to move between import, history, and dashboard.

Expected behavior:
- Primary destinations are easy to discover on a phone-sized screen.
- The current destination is visually clear.

### Example 2: Entering Review From Import

Input:
- User imports a supported screenshot and reaches review.

Expected behavior:
- Review feels like a focused task flow rather than just another generic tab.
- The user can return to a safe destination without confusion.

### Example 3: Opening Record Detail

Input:
- User selects a saved record from history.

Expected behavior:
- The shell shows clear title/back behavior.
- Returning to history is predictable and does not feel like leaving the app
  flow.

## Requirements

- The app MUST provide a mobile-friendly primary navigation pattern for import,
  history, and dashboard.
- On phone-sized screens, the primary navigation pattern MUST keep import,
  history, and dashboard reachable without requiring the user to open an
  overflow or secondary menu first.
- The shell MUST make the active destination visually clear.
- The shell MUST provide clear screen titles or equivalent destination context
  for primary and secondary screens.
- The shell MUST preserve the existing route structure and safe fallback rules.
- Review and record detail MUST feel like focused task screens, not peer
  primary destinations.
- The shell MUST keep navigation affordances reachable on normal phones.
- The shell MUST avoid presenting duplicated competing navigation controls for
  the same destination set on the same screen.

## Interface Expectations

- Primary destinations should remain easy to switch between.
- Secondary destinations should have clear back or close semantics.
- The shell should not force the user to infer where they are from content
  alone.

## Error-State Expectations

- If a secondary screen loses required state, the shell MUST return the user to
  a safe destination with a visible message.
- Navigation refinements MUST NOT remove existing safe fallback behavior.

## Edge Cases

- App starts with no saved records.
- Review opens and then loses draft state.
- Detail is requested with missing or invalid record identity.

## Non-Goals

- Deep links or multi-activity navigation.
- Tablet-specific large-screen navigation.
- Rewriting route ownership or business flow order.

## Acceptance Criteria

- Primary navigation feels mobile-friendly and task-oriented.
- Review and detail have clear contextual navigation behavior.
- Existing route safety and post-save navigation behavior remain intact.

## Gotchas

- 2026-04-14: Secondary review/detail actions should pop the existing back
  stack first and only fall back to a safe primary-route navigation when no
  back-stack entry exists. For the normal in-app flow, that keeps review/detail
  feeling like focused task screens instead of full shell resets.
- 2026-04-14: Compose tests for Material 3 bottom navigation are more stable
  when they assert the tagged screen title for active destination context and
  use `useUnmergedTree = true` for nav-item interactions.
