# App Shell Navigation UX Test Spec

## Scope

This test spec covers mobile-friendly app-shell navigation, destination
context, secondary-screen back behavior, and safe fallback handling for import,
review, history, dashboard, and detail.

## Unit Tests

- `T1` App-shell state or route mapper classifies import, history, and
  dashboard as primary destinations and review/detail as secondary task routes.
- `T2` Shell state exposes destination title or equivalent context for every
  primary and secondary route.
- `T3` Missing draft state on review resolves to a safe fallback destination
  with a visible message instruction.
- `T4` Missing or invalid detail identity resolves to a safe fallback
  destination with a visible message instruction.
- `T5` Post-save navigation still resolves to the expected safe destination.

## Integration Tests

- `IT1` App launch on a phone-sized device shows primary navigation where
  import, history, and dashboard are all directly reachable and the active
  destination is clearly indicated.
- `IT2` User can move among import, history, and dashboard without losing route
  context.
- `IT3` Entering review from import presents review as a focused task screen
  with clear back or close behavior rather than as a peer primary destination.
- `IT4` Opening detail from history presents clear back behavior and returns the
  user to history predictably.
- `IT5` Losing required secondary-route state returns the user to a safe
  destination with a visible fallback message.
- `IT6` No screen shows duplicated competing navigation controls for the same
  primary destination set.

## What Not To Test

- OCR behavior.
- History query correctness.
- Dashboard metric calculations.

## Coverage Map

- Primary vs secondary navigation behavior covered by `T1`, `IT1`, `IT3`, `IT4`
- Destination context covered by `T2`, `IT1`, `IT2`
- Safe fallback handling covered by `T3`, `T4`, `IT5`
- Post-save continuity covered by `T5`, `IT3`
- Redundant-control avoidance covered by `IT6`

## Not Directly Testable

- General aesthetic judgments beyond reachable primary navigation, visible
  active destination, and clear secondary-screen back behavior.
