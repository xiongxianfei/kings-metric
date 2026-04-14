# Review Sticky Actions And Scroll Safety Test Spec

## Scope

This test spec covers review-screen layout safety on normal phones, including
preview size handling, scroll behavior, visible blocker messaging, reachable
fields, and reachable save actions.

## Unit Tests

- `T1` Review layout state computes a bounded preview presentation that preserves
  room for the form when a screenshot is available.
- `T2` Review layout state exposes whether the save action should remain visible
  or discoverable while the form grows.
- `T3` Missing preview state collapses preview space without hiding the form or
  blocker messaging.

## Integration Tests

- `IT1` Tall portrait preview does not prevent the user from reaching editable
  fields and the primary save action on a normal phone-sized device.
- `IT2` Long grouped form remains scrollable as one coherent task without
  trapping the user in competing scroll regions.
- `IT3` Blocker or error messaging remains visible and reachable when preview,
  summary, and long form content are all present.
- `IT4` Missing preview degrades to a usable review form without empty layout
  gaps.
- `IT5` Editing with the software keyboard open still allows the active field
  and primary save action to remain reachable.

## What Not To Test

- Image zoom or pan behavior.
- Tablet split-pane layouts.
- Save validation rules themselves.

## Coverage Map

- Preview-size safety covered by `T1`, `IT1`
- Save-action reachability covered by `T2`, `IT1`, `IT5`
- Missing-preview layout fallback covered by `T3`, `IT4`
- Scroll and message safety covered by `IT2`, `IT3`, `IT5`

## Not Directly Testable

- Fine-grained visual polish beyond concrete reachability, scrolling, and
  keyboard-safety assertions.
