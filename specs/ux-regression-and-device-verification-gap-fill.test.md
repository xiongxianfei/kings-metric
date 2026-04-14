# UX Regression And Device Verification Gap Fill Test Spec

## Scope

This test spec covers only the remaining cross-screen usability gaps that are
not already owned by the focused UX feature tests.

## Integration Tests

- `IT1` Device or emulator test covers the main import -> review -> save path
  and confirms primary actions remain visible and reachable end to end.
- `IT2` Blocked review flow with several unresolved required fields keeps
  blocker messaging visible and the blocked fields discoverable after the UX
  refactor.
- `IT3` History-to-detail flow with missing screenshot preview keeps both
  screens usable and the fallback messaging understandable.
- `IT4` Unsupported import fallback remains visible and retry-oriented after the
  UX refactor if that cross-screen path is not already fully owned elsewhere.

## What Not To Test

- Replacements for owning-feature regressions that already exist.
- One giant end-to-end suite covering every UX promise redundantly.
- Broad performance benchmarking.

## Coverage Map

- Cross-screen main-path continuity covered by `IT1`
- Cross-screen blocker continuity covered by `IT2`
- Missing-preview fallback continuity covered by `IT3`
- Remaining unsupported-import fallback continuity covered by `IT4`

## Not Directly Testable

- Gap ownership itself is a planning and review concern, not an executable app
  behavior. This spec should stay residual and only cover leftover cross-screen
  runtime checks.
